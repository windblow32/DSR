package sim_func;

import org.apache.commons.math3.util.CombinatoricsUtils;
import tools.CheckString;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static tools.CheckString.*;

public class SimilarityFunction implements Serializable {
    /*
    字符串粒度还是集合粒度
     */
    public double jaccardForString(String str1, String str2) {
        // check string, 不符合要求则返回NaN
        if (!check(str1, str2)) {
//            System.out.print(0);
            return 0.0;
        }

        Set<Character> s1 = new HashSet<>();//set元素不可重复
        Set<Character> s2 = new HashSet<>();
        // fixme: 在参数中加上分词方法，此处经过分词处理后加入hashset
        for (int i = 0; i < str1.length(); i++) {
            s1.add(str1.charAt(i));//将string里面的元素一个一个按索引放进set集合
        }
        for (int j = 0; j < str2.length(); j++) {
            s2.add(str2.charAt(j));
        }

        float mergeNum = 0;//并集元素个数
        float commonNum = 0;//相同元素个数（交集）

        for (Character ch1 : s1) {
            for (Character ch2 : s2) {
                if (ch1.equals(ch2)) {
                    commonNum++;
                }
            }
        }

        mergeNum = s1.size() + s2.size() - commonNum;
        double res = commonNum / mergeNum;
//        System.out.print(res);
        return res;
    }

    public double jaccardForSet(List<String> list1, List<String> list2) {
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        double res = (double) (intersection.size()) / union.size();
//        System.out.print(res);
        return res;
    }

    public double overlap_coefficient(List<String> list1, List<String> list2) {
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        double res = (double) (intersection.size()) / Math.min(s1.size(), s2.size());
//        System.out.print(res);
        return res;
    }

    public double jaccardUsingSpace(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        List<String> list1 = Arrays.asList(str1.split(" "));
        List<String> list2 = Arrays.asList(str2.split(" "));
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        double res = (double) (intersection.size()) / union.size();
//        System.out.print(res);
        return res;
    }

    /**
     * 3-gram分词的jaccard算法(Magellan)
     *
     * @param str1
     * @param str2
     * @return
     */
    public double jaccardUsing3Gram(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        List<String> list1 = ngram(str1, 3);
        List<String> list2 = ngram(str2, 3);
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        double res = (double) (intersection.size()) / union.size();
//        System.out.print(res);
        return res;
    }

    /**
     * HanLP分词的jaccard算法
     *
     * @param str1
     * @param str2
     * @return
     */
    public double jaccardUsingHanLP(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        if(!check(str1,str2)){
           return 0.0;
        }
        WordBag bag = new WordBag();
        bag.NlpAnalysisTextSplit(str1, str2);
        List<String> list1 = bag.words1;
        List<String> list2 = bag.words2;
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        double res = (double) (intersection.size()) / union.size();
//        System.out.print(res);
        return res;
    }


    public double jaccardForSet(Set<String> s1, Set<String> s2) {
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        double res = (double) (intersection.size()) / union.size();
//        System.out.print(res);
        return res;
    }

    public double kendallTauSimilarity(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        // 转换字符串为小写，以便不区分大小写
        str1 = str1.replaceAll("[^a-zA-Z]", "");
        str2 = str2.replaceAll("[^a-zA-Z]", "");
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        // 初始化一个集合来存储字符出现的情况
        // 使用布尔数组，以便检查字符是否存在
        int[] charSet1 = new int[26]; // 假设只考虑小写字母a-z
        int[] charSet2 = new int[26];

        // 计算字符串1中字符的情况
        for (char c : str1.toCharArray()) {
            if (Character.isLowerCase(c)) {
                charSet1[c - 'a']++;
            }
        }
        // 计算字符串2中字符的情况
        for (char c : str2.toCharArray()) {
            if (Character.isLowerCase(c)) {
                charSet2[c - 'a']++;
            }
        }
        double discordantPairs = getDiscordantPairs(charSet1, charSet2);
        return discordantPairs;
    }

    private static double getDiscordantPairs(int[] x, int[] y) {
        int concordant = 0;
        int discordant = 0;

        for (int i = 0; i < x.length - 1; i++) {
            for (int j = i + 1; j < x.length; j++) {
                int comparisonX = Integer.compare(x[i], x[j]);
                int comparisonY = Integer.compare(y[i], y[j]);

                if (comparisonX == comparisonY) {
                    concordant++;
                } else {
                    discordant++;
                }
            }
        }

        int totalPairs = (int) CombinatoricsUtils.binomialCoefficient(x.length, 2);
        double kendallTau = (concordant - discordant) / (double) totalPairs;
        return kendallTau;
    }

    /*
    overlap coefficient 或Szymkiewicz–Simpson coefficient（SCC）
    是两个数据集相似度的度量，是两个数据集的交集和最小的那个数据集的比值，是Jaccard index 的一种延伸。
     */
    public double simpsonSimilarity(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        if (!check(s1, s2)) {
//            System.out.print(0f);
            return 0.0;
        }
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        int lcs = dp[len1][len2];
        double res = (double) lcs / Math.min(len1, len2);
//        System.out.print(res);
        return res;
    }

    private static List<Integer> stringToEncodingSequence(String str) {
        List<Integer> encodingSequence = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int encoding = (int) c;
            encodingSequence.add(encoding);
        }
        return encodingSequence;
    }

    private static double calculateEuclideanDistance(List<Integer> seq1, List<Integer> seq2) {
        int n = Math.min(seq1.size(), seq2.size());
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            int diff = seq1.get(i) - seq2.get(i);
            sum += Math.pow(diff, 2);
        }

        for (int i = n; i < seq1.size(); i++) {
            sum += Math.pow(seq1.get(i), 2);
        }

        for (int i = n; i < seq2.size(); i++) {
            sum += Math.pow(seq2.get(i), 2);
        }

        return Math.sqrt(sum);
    }

    public double exactMatch(String s1, String s2){
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        if(!check(s1,s2)){
            return 0.0;
        }
        if (s1.equals(s2)){
            return 1.0;
        }else return 0.0;
    }

    public double euclideanSimilarity(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        List<Integer> seq1 = stringToEncodingSequence(str1);
        List<Integer> seq2 = stringToEncodingSequence(str2);

        return 1-calculateEuclideanDistance(seq1, seq2);
    }

    // 多属性联合,坐标
    public double manhattan(Double n1, Double n2) {
        return 0;
    }

    public double hamming(String s1, String s2) {
        if (!check(s1, s2)) {
            return 0;
        }
        int length = s1.length();
        assert length == s2.length() : "hamming similarity undefined for sequences of unequal length ";
//        if (length!=s2.length()){
//            log.println("hamming similarity undefined for sequences of unequal length ");
//            return 0;
//        }
        int sum = 0;
        for (int i = 0; i < length; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                sum++;
            }
        }
        return sum;
    }

    /*
     用来计算两个字串之间，通过替换、插入、删除等操作将字符串str1转换成str2所需要操作的最少次数
     */
    public double LevenshteinSimilarity(String s, String t) {
        if(s==null&&t==null){
            return 1.0;
        } else if (s==null||t==null) {
            return 0.0;
        }
        if (!check(s, t)) {
//            System.out.print(0);
            return 0;
        }
        //构建一个(s_len+1)*(t_len+1)的矩阵d，d[i][j]表示字符串s的前i字符和t的前j个字符的莱文斯坦距离
        int s_len = s.length();
        int t_len = t.length();
        int[][] d = new int[s_len + 1][t_len + 1];
        int i, j;

        //源字符串s到空字符串t只要删除每个字符
        for (i = 0; i <= s_len; i++)
            d[i][0] = i;
        //从空字符s到目标字符t只要添加每个字符
        for (j = 1; j <= t_len; j++)
            d[0][j] = j;
        for (j = 0; j < t_len; j++)
            for (i = 0; i < s_len; i++)
                if (s.charAt(i) == t.charAt(j)) d[i + 1][j + 1] = d[i][j]; //不进行任何操作
                else d[i + 1][j + 1] = Min(d[i][j + 1] + 1,  //删除操作
                        d[i + 1][j] + 1,  //添加操作
                        d[i][j] + 1 //替换操作
                );

        double res = d[s_len][t_len];
//        System.out.print(res);
        return 1/(1+res);
    }

    public double LevenshteinDistance(String s, String t) {
        if(s==null&&t==null){
            return 1.0;
        } else if (s==null||t==null) {
            return 0.0;
        }
        if (!check(s, t)) {
//            System.out.print(0);
            return 0;
        }
        //构建一个(s_len+1)*(t_len+1)的矩阵d，d[i][j]表示字符串s的前i字符和t的前j个字符的莱文斯坦距离
        int s_len = s.length();
        int t_len = t.length();
        int[][] d = new int[s_len + 1][t_len + 1];
        int i, j;

        //源字符串s到空字符串t只要删除每个字符
        for (i = 0; i <= s_len; i++)
            d[i][0] = i;
        //从空字符s到目标字符t只要添加每个字符
        for (j = 1; j <= t_len; j++)
            d[0][j] = j;
        for (j = 0; j < t_len; j++)
            for (i = 0; i < s_len; i++)
                if (s.charAt(i) == t.charAt(j)) d[i + 1][j + 1] = d[i][j]; //不进行任何操作
                else d[i + 1][j + 1] = Min(d[i][j + 1] + 1,  //删除操作
                        d[i + 1][j] + 1,  //添加操作
                        d[i][j] + 1 //替换操作
                );

        return (double)d[s_len][t_len];
    }

    public double LevenshteinDisForSet(List<String> list1, List<String> list2) {
        int[][] distance = new int[list1.size() + 1][list2.size() + 1];

        for (int i = 0; i <= list1.size(); i++) {
            distance[i][0] = i;
        }

        for (int j = 0; j <= list2.size(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= list1.size(); i++) {
            for (int j = 1; j <= list2.size(); j++) {
                int cost = list1.get(i - 1).equals(list2.get(j - 1)) ? 0 : 1;
                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
            }
        }
        double res = (double) distance[list1.size()][list2.size()];
//        System.out.print(res);
        return res;
    }

    private int Min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /*
    This function computes the Jaro measure between the two input
    strings.

    Args:
        s1,s2 (string): The input strings for which the similarity measure should
            be computed.

    Returns:
        The Jaro measure if both the strings are not missing (i.e NaN),
        else  returns NaN.

    Examples:
        >>> import py_entitymatching as em
        >>> em.jaro('MARTHA', 'MARHTA')
        0.9444444444444445
        >>> em.jaro(None, 'MARTHA')
        nan
     code refer to csdn-字符串相似度比较算法：Jaro–Winkler similarity的原理及实现
     l表示两个字符串的共同前缀字符的个数，最大不超过4个。
    p是缩放因子常量，它描述的是共同前缀对于相似度的贡献，p越大，表示共同前缀权重越大，最大不超过0.25。p默认取值是0.1
    用户可以修改p参数，以提高共同前缀的权重
     */
    public double Jaro(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        if (!check(s1, s2)) {
//            System.out.print(0f);
            return 0.0;
        }
        int[] result = matchesInJaro(s1, s2);
        float m = result[0];
        if (m == 0f) {
//            System.out.print(0f);
            return 0f;
        }
        double res = ((m / s1.length() + m / s2.length() + (m - result[1]) / m)) / 3;
//        System.out.print(res);
        return res;
    }

    public double JaroWinklerDistance(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        if (!check(s1, s2)) {
//            System.out.print(0);
            return 0.0;
        }
        float p = 0.1f;
        float MAX_P = 0.25f;
        int MAX_L = 4;
        if (s1 == null || s2 == null) return 0f;
        int[] result = matchesInJaro(s1, s2);

        float m = result[0];
        if (m == 0f) {
//            System.out.print(0f);
            return 0f;
        }
        float j = ((m / s1.length() + m / s2.length() + (m - result[1]) / m)) / 3;
        double res = j + p * result[2] * (1 - j);
//        System.out.print(res);
        return res;


    }

    /*
    jaro distance use only
     */
    private int[] matchesInJaro(String s1, String s2) {
        float p = 0.1f;
        float MAX_P = 0.25f;
        int MAX_L = 4;
        //用max来保存较长的字符串，min保存较短的字符串
        //这是为了以短字符串为行元素遍历，长字符串为列元素遍历。
        CharSequence max, min;
        if (s1.length() > s2.length()) {
            max = s1;
            min = s2;
        } else {
            max = s2;
            min = s1;
        }

        //匹配窗口的大小，对于每一行i，列j只在(i-matchedwindow,i+matchedwindow)内移动，
        //在该范围内遇到相等的字符，表示匹配成功
        int matchedWindow = Math.max(max.length() / 2 - 1, 0);
        //记录字符串的匹配状态，true表示已经匹配成功
        boolean[] minMatchFlag = new boolean[min.length()];
        boolean[] maxMatchFlag = new boolean[max.length()];
        int matches = 0;

        for (int i = 0; i < min.length(); i++) {
            char minChar = min.charAt(i);
            //列元素的搜索：j的变化包括i往前搜索窗口长度和i往后搜索窗口长度。
            for (int j = Math.max(i - matchedWindow, 0); j < Math.min(i + matchedWindow + 1, max.length()); j++) {
                if (!maxMatchFlag[j] && minChar == max.charAt(j)) {
                    maxMatchFlag[j] = true;
                    minMatchFlag[i] = true;
                    matches++;
                    break;
                }
            }
        }
        //求转换次数和相同前缀长度
        int transpositions = 0;
        int prefix = 0;

        int j = 0;
        for (int i = 0; i < min.length(); i++) {
            if (minMatchFlag[i]) {
                while (!maxMatchFlag[j]) j++;

                if (min.charAt(i) != max.charAt(j)) {
                    transpositions++;
                }
                j++;
            }
        }

        for (int i = 0; i < min.length(); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }

        return new int[]{matches, transpositions / 2, Math.min(prefix, MAX_L)};
    }

    public double pearsonCorrelationCoefficient(List<Double> list1, List<Double> list2) {
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        int n = list1.size();
        for (int i = 0; i < n; i++) {
            double x = list1.get(i), y = list2.get(i);
            sum1 += x;
            sum2 += y;
            sum1Sq += Math.pow(x, 2);
            sum2Sq += Math.pow(y, 2);
            pSum += x * y;
        }
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / n) * (sum2Sq - Math.pow(sum2, 2) / n));
        if (den == 0) return 0;
        return num / den;
    }

    /*
    This function computes the Needleman-Wunsch measure between the two input
    strings.

    Args:
        s1,s2 (string): The input strings for which the similarity measure should
            be computed.

    Returns:
        The Needleman-Wunsch measure if both the strings are not missing (i.e
        NaN), else  returns NaN.

    Examples:
        >>> import py_entitymatching as em
        >>> em.needleman_wunsch('dva', 'deeva')
        1.0
        >>> em.needleman_wunsch('dva', None)
        nan
        这是一种两序列局部比对算法，把两条未知的序列进行排列，通过字母的匹配，删除和插入操作，
        使得两条序列达到同样长度，在操作的过程中，尽可能保持相同的字母对应在同一个位置。
        当两条序列进行比对时，找出待比对序列中的某一子片段的最优比对。
        这种比对方法可能会揭示一些匹配的序列段，而本来这些序列段是被一些完全不相关的残基所淹没的
     */
    public double needleman_wunsch(String sequence1, String sequence2) {
        if(sequence1==null&&sequence2==null){
            return 1.0;
        } else if (sequence1==null||sequence2==null) {
            return 0.0;
        }
        if (!check(sequence1, sequence2)) {
//            System.out.print(0);
            return 0.0;
        }
        // 公共最长子序列
        int matchScore = 1;
        int mismatchScore = -1;
        int gapScore = -2;

        int[][] scoreMatrix = calculateAlignmentMatrix(sequence1, sequence2, matchScore, mismatchScore, gapScore);
        int alignmentScore = scoreMatrix[sequence1.length()][sequence2.length()];
        return (double)alignmentScore;

    }
    public static int[][] calculateAlignmentMatrix(String sequence1, String sequence2, int matchScore, int mismatchScore, int gapScore) {
        int m = sequence1.length();
        int n = sequence2.length();

        int[][] scoreMatrix = new int[m + 1][n + 1];

        // Initialize the first row and first column with gap scores
        for (int i = 0; i <= m; i++) {
            scoreMatrix[i][0] = i * gapScore;
        }
        for (int j = 0; j <= n; j++) {
            scoreMatrix[0][j] = j * gapScore;
        }

        // Fill in the score matrix
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int match = scoreMatrix[i - 1][j - 1] + (sequence1.charAt(i - 1) == sequence2.charAt(j - 1) ? matchScore : mismatchScore);
                int delete = scoreMatrix[i - 1][j] + gapScore;
                int insert = scoreMatrix[i][j - 1] + gapScore;
                scoreMatrix[i][j] = Math.max(Math.max(match, delete), insert);
            }
        }

        return scoreMatrix;
    }
    /*
        This function computes the Dice score between the two input
    lists/sets.

    Args:
        arr1,arr2 (list or set): The input list or sets for which the Dice
            score should be computed.

    Returns:
        The Dice score if both the lists/set are not None and do not
        have any missing tokens (i.e NaN), else  returns NaN.

    Examples:
        >>> import py_entitymatching as em
        >>> em.dice(['data', 'science'], ['data'])
        0.6666666666666666
        >>> em.dice(['data', 'science'], None)
        nan
     */
    public double diceForSet(List<String> list1, List<String> list2) {
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        // similarity
        Set<String> result = new HashSet<>(s1);
        result.retainAll(s2);
        double res = (result.size() * 2.0) / (s1.size() + s2.size());
//        System.out.print(res);
        return res;
    }

    /*
    This function computes the Monge-Elkan measure between the two input
    lists/sets. Specifically, this function uses Jaro-Winkler measure as the
    secondary function to compute the similarity score.
    attention : 默认使用jaro

    Args:
        arr1,arr2 (list or set): The input list or sets for which the
            Monge-Elkan measure should be computed.

    Returns:
        The Monge-Elkan measure if both the lists/set are not None and do not
        have any missing tokens (i.e NaN), else  returns NaN.

    Examples:
        >>> import py_entitymatching as em
        >>> em.monge_elkan(['Niall'], ['Neal'])
        0.8049999999999999
        >>> em.monge_elkan(['Niall'], None)
        nan
     */
    public double monge_elkanForSet(List<String> list1, List<String> list2) {
        Set<String> s1 = new HashSet<>(list1);
        Set<String> s2 = new HashSet<>(list2);
        if (s1 == null || s2 == null) {
            return 0;
        }
        double sum_of_maxes = 0;
        double max_sim;
        double sim;
        for (String s : s1) {
            max_sim = Double.NEGATIVE_INFINITY;
            for (String t : s2) {
                max_sim = Math.max(max_sim, Jaro(s, t));
            }
            sum_of_maxes += max_sim;
        }
        sim = sum_of_maxes / s1.size();
//        System.out.print(sim);
        return sim;
    }

    public double monge_elkan(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        Set<String> s1 = Collections.singleton(str1);
        Set<String> s2 = Collections.singleton(str2);
        if (s1 == null || s2 == null) {
            return 0;
        }
        double sum_of_maxes = 0;
        double max_sim;
        double sim;
        for (String s : s1) {
            max_sim = Double.NEGATIVE_INFINITY;
            for (String t : s2) {
                max_sim = Math.max(max_sim, Jaro(s, t));
            }
            sum_of_maxes += max_sim;
        }
        sim = sum_of_maxes / s1.size();
//        System.out.print(sim);
        return sim;
    }

    /*
    This function computes the relative difference between two numbers

    Args:
        d1,d2 (float): The input numbers for which the relative difference
         must be computed.

    Returns:
        A float value of relative difference between the input numbers (if
        they are valid). Further if one of the input objects is NaN or None,
        it returns NaN.

    Examples:
        >>> import py_entitymatching as em
        >>> em.rel_diff(100, 200)
        0.6666666666666666
        >>> em.rel_diff(100, 100)
        0.0
        >>> em.rel_diff(100, None)
        nan
     */
    public double rel_diff(String value1, String value2) {
        if (!(CheckString.isNumber(value1) && CheckString.isNumber(value2))) {
            return 0.0;
        }
        double n1 = Double.parseDouble(value1);
        double n2 = Double.parseDouble(value2);
        // similarity
        double x = (Math.abs(n1 - n2) * 2) / (Math.abs(n1) + Math.abs(n2));
        if (x <= 10e-5) {
            x = 0;
        }
        double res = 1 - x;
//        System.out.print(res);
        return res;
    }

    public double smith_Waterman(String v1, String v2){
        if(v1==null&&v2==null){
            return 1.0;
        } else if (v1==null||v2==null) {
            return 0.0;
        }
        if (!check(v1, v2)) {
            return 0.0;
        }
        SWSq sw = new SWSq();
        return sw.smith_waterman(v1,v2);
    }

    public double abs_norm(String value1, String value2) {
        if (!(CheckString.isNumber(value1) && CheckString.isNumber(value2))) {
            return 0.0;
        }
        double d1 = Double.parseDouble(value1);
        double d2 = Double.parseDouble(value2);
        // similarity
        double x = (Math.abs(d1 - d2) / Math.max(Math.abs(d1), Math.abs(d2)));
        // System.out.println(1 - x);
        double res = 1 - x;
//        System.out.print(res);
        return res;
    }

    public double cosineForSet(List<Double> vec1, List<Double> vec2) {
        double sumTotal = 0;
        double sumVec1 = 0;
        double sumVec2 = 0;
        int length = vec1.size();
        for (int i = 0; i < length; i++) {
            double value1 = vec1.get(i);
            double value2 = vec2.get(i);
            sumTotal += value1 * value2;
            sumVec1 += value1 * value1;
            sumVec2 += value2 * value2;
        }
        double res = sumTotal / Math.sqrt(sumVec1 * sumVec2);
//        System.out.print(res);
        return res;
    }

    public float cosineForFloat(List<Float> vec1, List<Float> vec2) {
        float sumTotal = 0;
        float sumVec1 = 0;
        float sumVec2 = 0;
        int length = vec1.size();
        for (int i = 0; i < length; i++) {
            float value1 = vec1.get(i);
            float value2 = vec2.get(i);
            sumTotal += value1 * value2;
            sumVec1 += value1 * value1;
            sumVec2 += value2 * value2;
        }
        float res = sumTotal / (float) Math.sqrt(sumVec1 * sumVec2);
//        System.out.print(res);
        return res;
    }

    // 计算单词的tf值
    private double tf(String word, List<String> document) {
        int count = 0;
        for (String w : document) {
            if (w.equalsIgnoreCase(word)) {
                count++;
            }
        }
        return (double) count / document.size();
    }

    // 计算单词的idf值
    private double idf(String word, List<List<String>> documents) {
        int n = documents.size();
        int count = 0;
        for (List<String> doc : documents) {
            for (String w : doc) {
                if (w.equalsIgnoreCase(word)) {
                    count++;
                    break;
                }
            }
        }
        return Math.log((double) n / count);
    }

    // 计算tf-idf值
    @Deprecated
    private double tfIdf(String word, List<String> document, List<List<String>> documents) {
        return tf(word, document) * idf(word, documents);
    }

    // 将字符串转换为单词列表
    private List<String> toWordsList(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group().toLowerCase());
        }
        return words;
    }

    // 计算余弦相似度
    public static double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public double cosine_Num(String value1, String value2) {
        if (!(CheckString.isNumber(value1) && CheckString.isNumber(value2))) {
            return 0.0;
        }
        double a = Double.parseDouble(value1);
        double b = Double.parseDouble(value2);
        double res = (a * b) / (Math.sqrt(a * a) * Math.sqrt(b * b));
//        System.out.print(res);
        return res;
    }

    /**
     * 利用空格分词，计算cosine相似度(Magellan)
     *
     * @param str1
     * @param str2
     * @return
     */
    public double cosineForWord(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        if (!check(str1, str2)) {
            return 0.0;
        }
        List<String> words1 = Arrays.asList(str1.split(" "));
        List<String> words2 = Arrays.asList(str2.split(" "));
        Set<String> set = new HashSet<>();
        set.addAll(words1);
        set.addAll(words2);
        int[] vector1 = new int[set.size()];
        int[] vector2 = new int[set.size()];
        int index = 0;
        for (String word : set) {
            vector1[index] = Collections.frequency(words1, word);
            vector2[index] = Collections.frequency(words2, word);
            index++;
        }
        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            magnitude1 += Math.pow(vector1[i], 2);
            magnitude2 += Math.pow(vector2[i], 2);
        }
        double res = dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
//        System.out.print(res);
        return res;
    }

    public double cosineUsingWordBag(List<String> words1, List<String> words2) {
        Set<String> set = new HashSet<>();
        set.addAll(words1);
        set.addAll(words2);
        int[] vector1 = new int[set.size()];
        int[] vector2 = new int[set.size()];
        int index = 0;
        for (String word : set) {
            vector1[index] = Collections.frequency(words1, word);
            vector2[index] = Collections.frequency(words2, word);
            index++;
        }
        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            magnitude1 += Math.pow(vector1[i], 2);
            magnitude2 += Math.pow(vector2[i], 2);
        }
        double res = dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
//        System.out.print(res);
        return res;
    }

    public List<String> ngram(String text, int n) {
        List<String> result = new ArrayList<>();
        if (text == null || text.length() == 0 || n <= 0) {
            return result;
        }
        int len = text.length();
        if (len < n) {
            result.add(text);
            return result;
        }
        for (int i = 0; i <= len - n; i++) {
            result.add(text.substring(i, i + n));
        }
        return result;
    }

    public double simHashSimilarity(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        if (!check(str1, str2)) {
//            System.out.print(0f);
            return 0.0;
        }
        SimHash sim1 = new SimHash(str1);
        SimHash sim2 = new SimHash(str2);
        sim1.hash = sim1.simHash();
        sim2.hash = sim2.simHash();
        BigInteger x = sim1.hash.xor(sim2.hash);
        int distance = 0;
        while (x.signum() != 0) {
            distance++;
            x = x.and(x.subtract(BigInteger.ONE));
        }
//        System.out.print((double)distance);
        return 1/(1+(double) distance);
    }

    public double birnbaumSimilarity(String str1, String str2) {
        if(str1==null&&str2==null){
            return 1.0;
        } else if (str1==null||str2==null) {
            return 0.0;
        }
        // 转换字符串为小写，以便不区分大小写
        str1 = str1.replaceAll("[^a-zA-Z]", "");
        str2 = str2.replaceAll("[^a-zA-Z]", "");
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        // 初始化一个集合来存储字符出现的情况
        // 使用布尔数组，以便检查字符是否存在
        boolean[] charSet1 = new boolean[26]; // 假设只考虑小写字母a-z
        boolean[] charSet2 = new boolean[26];

        // 计算字符串1中字符的情况
        for (char c : str1.toCharArray()) {
            if (Character.isLowerCase(c)) {
                charSet1[c - 'a'] = true;
            }
        }
        // 计算字符串2中字符的情况
        for (char c : str2.toCharArray()) {
            if (Character.isLowerCase(c)) {
                charSet2[c - 'a'] = true;
            }
        }

        // 计算相同字符的数量
        int commonChars = 0;
        int totalChars = 0;
        for (int i = 0; i < 26; i++) {
            if (charSet1[i] && charSet2[i]) {
                commonChars++;
            }
            if (charSet1[i] || charSet2[i]) {
                totalChars++;
            }
        }

        // 计算Birnbaum相似度
        if (totalChars == 0) {
            return 0.0; // 避免除以0的情况
        } else {
            return (double) commonChars / totalChars;
        }
    }

    public double mmcwpaSimilarity(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        if (!check(s1, s2)) {
//            System.out.print(0f);
            return 0.0;
        }
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + costOfSubstitution(s1.charAt(i - 1), s2.charAt(j - 1)), Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
//        System.out.print((double)dp[s1.length()][s2.length()]);
        return (double) 1/(1+dp[s1.length()][s2.length()]);
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public double bulkDeleteSimilarity(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        int n = s1.length();
        int m = s2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        int lcs = dp[n][m];
        int bulkDelete = Math.abs(n - m);
        double res = (double) lcs / (Math.max(n, m) - bulkDelete);
//        System.out.print(res);
        return res;
    }

    public double affineSimilarity(String s1, String s2) {
        if(s1==null&&s2==null){
            return 1.0;
        } else if (s1==null||s2==null) {
            return 0.0;
        }
        int matchScore = 4;
        int mismatchScore = -2;
        int gapOpen = -2;
        int gapExtend = -1;
        int n = s1.length();
        int m = s2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            dp[i][0] = -gapOpen - (i - 1) * gapExtend;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = -gapOpen - (j - 1) * gapExtend;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int match = dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : -mismatchScore);
                int delete = dp[i - 1][j] - gapExtend;
                int insert = dp[i][j - 1] - gapExtend;
                dp[i][j] = Math.max(match, Math.max(delete, insert));
            }
        }
        int score = dp[n][m];
        double similarity = (double) score / Math.max(s1.length(), s2.length());
//        System.out.print(similarity);
        return similarity;
    }

    public static List<Float> averagePooling(List<Float> vector, int targetDim) {
        int currentDim = vector.size();
        int reduceDim = currentDim - targetDim;
        List<Float> pooledVector = new ArrayList<>();

        for (int i = 0; i < reduceDim; i++) {
            int startIndex = i * (currentDim / reduceDim);
            int endIndex = (i + 1) * (currentDim / reduceDim);
            float sum = 0;

            for (int j = startIndex; j < endIndex; j++) {
                sum += vector.get(j);
            }

            float average = sum / (endIndex - startIndex);
            pooledVector.add(average);
        }

        return pooledVector;
    }


    public double cosineUsingEmbedding(String str1, String str2, float rouMax, Map<String, float[]> wordEmbeddings) {

        String[] data1 = str1.split(" ");
        String[] data2 = str2.split(" ");
        float max = 0.0f;
        for (String s1 : data1) {
            List<Float> embdi1 = array2list(wordEmbeddings.get(s1));
            for (String s2 : data2) {
                List<Float> embdi2 = array2list(wordEmbeddings.get(s2));
                if (!(embdi1.size() == embdi2.size())) {
                    int size1 = embdi1.size();
                    int size2 = embdi2.size();
                    if (size1 < size2) {
                        embdi2 = averagePooling(embdi2, size1);
                    } else {
                        embdi1 = averagePooling(embdi1, size2);
                    }
                }
                float sim = cosineForFloat(embdi1, embdi2);
                if (sim > max) max = sim;
            }
        }
        if (max > rouMax) {
//            System.out.print(max);
            return max;
        } else {
            return jaccardForString(str1, str2);
        }
    }

    private List<Float> array2list(float[] array) {
        int embdi_length = 300;
        List<Float> res = new ArrayList<>();
        try {
            for (float n : array) {
                res.add(n);
            }
            return res;
        } catch (NullPointerException e) {
            for (int i = 0; i < embdi_length; i++) {
                res.add(+0.0f);
            }
            return res;
        }
    }


}
