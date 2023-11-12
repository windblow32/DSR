package Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvgNumCalc {

    private List<String> analyzeString(String input) {
        List<String> numList = new ArrayList<>();
        // 定义匹配数字的正则表达式
        String regex = "\\d+";

        // 创建Pattern对象
        Pattern pattern = Pattern.compile(regex);

        // 创建Matcher对象
        Matcher matcher = pattern.matcher(input);

        // 循环匹配并输出结果
        while (matcher.find()) {
            String number = matcher.group();
            numList.add(number);
        }
        return numList;
    }

    /**
     * 只算字符串中数字特征
     *
     * @param str
     * @return
     */
    public double cal_avg(String str) {
        if(str.equals("")){
            return 0.0;
        }
        // fixme : 只要字符串中数字
        List<String> numbers = analyzeString(str);
        int sum = 0;
        int count = 0;

        for (String number : numbers) {
            if (!number.isEmpty()) {
                int num = 0;
                try{
                    num = Integer.parseInt(number);
                }catch (Exception e){
                    continue;
                }
                sum += num;
                count++;
            }
        }

        if (count == 0) {
            return 0;
        }
        return (double) sum / count;
    }

    /**
     * 特征向量
     *
     * @param str
     * @return
     */
    public double cal_fv(String str) {
        // 切分字符
        String[] words = str.split(" ");
        int max_length = 0;
        for (String word : words) {
            if (word.length() > max_length)
                max_length = word.length();
        }

        // 计算特征向量
        int[][] fv = new int[str.length()][max_length];
        int i = 0;
        for (String word : words) {
            fv[i] = calculateLPSArray(word);
            i = i + 1;
        }

        // 降维，把矩阵降维成一个数（？
        return 0;
    }

    public int[] calculateLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int i = 0; // index for pattern

        for (int j = 1; j < pattern.length(); ) {
            if (pattern.charAt(i) == pattern.charAt(j)) {
                lps[j] = i + 1;
                i++;
                j++;
            } else {
                if (i != 0) {
                    i = lps[i - 1];
                } else {
                    lps[j] = 0;
                    j++;
                }
            }
        }

        return lps;
    }

    /**
     * 词频-逆词频
     *
     * @param
     * @return
     */
    private int calculateTF(List<String> documents) {
        Map<String, Integer> tfMap = new HashMap<>();

        for (String document : documents) {
            String[] words = document.toLowerCase().split("\\W+");

            for (String word : words) {
                tfMap.put(word, tfMap.getOrDefault(word, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : tfMap.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            // print freq
            System.out.println(word + ": " + frequency);
        }
        // fixme : TF 返回什么？
        return 0;
    }
}
