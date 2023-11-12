package tools;

import dataMining.DSR;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import sim_func.MinHashSimilarity;
import sim_func.SimilarityFunction;
import sim_func.WordBag;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static tools.CheckString.check;

public class GenerateFeatureWithSim implements Serializable {
    private static final long serialVersionUID = -3175162002464574460L;
    public String filePath;
    public String outputPath;
    public List<Integer> longTextIndexList;
    public List<Integer> numericalIndexList;
    public PrintStream stdout = System.out;
    public int lineLength = 0;
    private int attrSize;

    public String map_savePath;
    //不用的属性刻画下标（0-7）
    public int ignoreStatistics = -1;

    private Map<String, float[]> readEmbdiTxt() {
        try {
            String embeddingFile = "E:\\GitHub\\EntityMatching\\data\\dblp-scholar\\chinese\\embedding\\fastTextEmbdi2.txt";
            Map<String, float[]> wordEmbeddings = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(embeddingFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String word = parts[0];
                float[] embedding = new float[parts.length - 1];
                int length = parts.length;
                for (int i = 1; i < length; i++) {
                    embedding[i - 1] = Float.parseFloat(parts[i]);
                }
                wordEmbeddings.put(word, embedding);
            }
            reader.close();
            return wordEmbeddings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(-200);
        return null;
    }

    public void calcSim() {
        // attention : fastText, autoEM中不使用
        // Map<String, float[]> embdi = readEmbdiTxt();
        File f = new File(filePath);
        File output = new File(outputPath);
        SimilarityFunction func = new SimilarityFunction();
        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            // read file
            BufferedReader br = ReadCsv.getBr(filePath);
            // read attr;
            String[] attr = br.readLine().split(",", -1);
            attrSize = attr.length;
            // attention : 输出特征名字
            printAttr(attr);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(br);
            for (CSVRecord record : records) {
                // 获取属性对
                // 比较相似度
                for (int index = 0; index < attrSize; index = index + 2) {
                    String value1 = record.get(index);
                    String value2 = record.get(index + 1);
                    // judge
                    //print(func.cosineUsingEmbedding(value1, value2,0.75f,embdi));
                    // String
                    System.out.print(func.affineSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.birnbaumSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.bulkDeleteSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.euclideanSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.exactMatch(value1, value2));
                    System.out.print(",");
                    System.out.print(func.jaccardForString(value1, value2));
                    System.out.print(",");
                    System.out.print(func.Jaro(value1, value2));
                    System.out.print(",");
                    System.out.print(func.JaroWinklerDistance(value1, value2));
                    System.out.print(",");
                    System.out.print(func.kendallTauSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.LevenshteinSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.needleman_wunsch(value1, value2));
                    System.out.print(",");
                    System.out.print(func.mmcwpaSimilarity(value1, value2));
                    System.out.print(",");
                    System.out.print(func.simpsonSimilarity(value1, value2));
                    System.out.print(",");
                    // simHash,应用sdbm hash，可更换哈希函数
                    System.out.print(func.simHashSimilarity(value1, value2));
                    System.out.print(",");
                    // numerical
                    System.out.print(func.abs_norm(value1, value2));
                    System.out.print(",");
                    System.out.print(func.rel_diff(value1, value2));
                    System.out.print(",");
                    System.out.print(func.cosine_Num(value1, value2));
                    System.out.print(",");
                    hashPrint(value1, value2);
                    // ascii
                    asciiPrint(value1, value2);
                    // 长文本
                    // ngram分词
                    ngramPrint(value1, value2, 3);
                    // WordBag中四种分词方法
                    indexAnaly(value1, value2);
                    nlpAnaly(value1, value2);
                    baseAnaly(value1, value2);
                    toAnaly(value1, value2);
//                        // attention : 长文本，分词再计算
//                        if (longTextIndexList.contains(index)) {
//                            if (value1.length() > 512) {
//                                value1 = value1.substring(0, 512);
//                            }
//                            if (value2.length() > 512) {
//                                value2 = value2.substring(0, 512);
//                            }
//                            // ngram分词
//                            ngramPrint(value1, value2, 3);
//                            // WordBag中四种分词方法
//                            indexAnaly(value1, value2);
//                            nlpAnaly(value1, value2);
//                            baseAnaly(value1, value2);
//                            toAnaly(value1, value2);
//                        }
                }
                System.out.println();
            }
            System.setOut(stdout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AutoEM提供的规则库
     **/
    public void generateAutoEM_FeatureUseSpark(List<Integer> numericalIndexList) {
        // 依据ruleList分析X,ruleList表明了属性刻画和相似度函数的关系
        File output = new File(outputPath);
        try {

            Logger.getLogger("org").setLevel(Level.ERROR);
            SparkSession spark = SparkSession.builder()
                    .appName("generateFeatureUseRules")
                    .master("local")
                    .getOrCreate();

            Dataset<Row> dataset = spark.read()
                    .format("csv")
                    .option("header", "true")
                    .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
                    .load(filePath);
            attrSize = dataset.columns().length;
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            // 应用spark进行实体对间相似度计算
            // attention : 根据funcEnum生成特征
            JavaRDD<Row> rdd = dataset.javaRDD();
            int size = attrSize;
            JavaRDD<List<Double>> resRDD = rdd.map(row -> {
                List<Double> simList = new ArrayList<>();
                for (int index = 0; index < size; index += 2) {
                    String v1 = row.getString(index);
                    String v2 = row.getString(index + 1);
                    double s;
                    SimilarityFunction function = new SimilarityFunction();
                    s = function.LevenshteinDistance(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.LevenshteinSimilarity(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.Jaro(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.JaroWinklerDistance(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.exactMatch(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.needleman_wunsch(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    // smith-waterman
                    s = function.smith_Waterman(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.monge_elkan(v1, v2);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);
                    // 一次8个
                    if(v1==null&&v2==null){
                        for(int t = 0;t<9;t++){
                            s = 1.0;
                            System.out.print(s);
                            System.out.print(",");
                            simList.add(s);
                        }
                        continue;
                    } else if (v1==null||v2==null) {
                        for(int t = 0;t<9;t++){
                            s = 0.0;
                            System.out.print(s);
                            System.out.print(",");
                            simList.add(s);
                        }
                        continue;
                    }else if (!check(v1, v2)) {
                        for(int t = 0;t<9;t++){
                            s = 0.0;
                            System.out.print(s);
                            System.out.print(",");
                            simList.add(s);
                        }
                        continue;
                    }

                    // space分词
                    List<String> list1_space = new ArrayList<>();
                    List<String> list2_space = new ArrayList<>();
                    list1_space = Arrays.asList(v1.split(" "));
                    list2_space = Arrays.asList(v2.split(" "));

                    // overlap coefficient
                    s = function.overlap_coefficient(list1_space, list2_space);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);


                    s = function.diceForSet(list1_space, list2_space);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.cosineUsingWordBag(list1_space, list2_space);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.jaccardForSet(list1_space, list2_space);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    // 3-gram分词
                    List<String> list1_3gram = new ArrayList<>();
                    List<String> list2_3gram = new ArrayList<>();
                    list1_3gram = Arrays.asList(v1.split(" "));
                    list2_3gram = Arrays.asList(v2.split(" "));


                    // overlap coefficient
                    s = function.overlap_coefficient(list1_3gram, list2_3gram);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);


                    s = function.diceForSet(list1_3gram, list2_3gram);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.cosineUsingWordBag(list1_3gram, list2_3gram);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);

                    s = function.jaccardForSet(list1_3gram, list2_3gram);
                    System.out.print(s);
                    System.out.print(",");
                    simList.add(s);
                    if(numericalIndexList.contains(index)){
                        s = function.abs_norm(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);
                    }
                    else {
                        s = 0.0;
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);
                    }

                }
                System.out.println();
                return simList;
            });
            resRDD.count();
            spark.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setOut(stdout);
    }


    public void generateFeatureUseRules_Spark(List<DSR> ruleList, double[][] X) {
        // 依据ruleList分析X,ruleList表明了属性刻画和相似度函数的关系
        File output = new File(outputPath);
        try {

            Logger.getLogger("org").setLevel(Level.ERROR);
            SparkSession spark = SparkSession.builder()
                    .appName("generateFeatureUseRules")
                    .master("local")
                    .getOrCreate();

            Dataset<Row> dataset = spark.read()
                    .format("csv")
                    .option("header", "true")
                    .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
                    .load(filePath);
            attrSize = dataset.columns().length;



            // attention : 输出特征名字
            // printAttr(attr);
            // attention : 属性下标与应用的相似度函数进行映射,一个属性推荐一个相似度（可以通过把int改成list类型实现多个推荐）
            // 首先建立属性与index的映射关系
            Map<Integer, Set<Integer>> attrMap = new HashMap<>();
            // 这里对每种属性推荐，需要把attrSize/2
            for (int i = 0; i < attrSize / 2; i++) {
                Set<Integer> funcEnum = new HashSet<>();
                for (DSR rule : ruleList) {
                    // 判断第i个属性需要的相似度函数
                    // 判断是否添加规则中的相似度函数，任意一个属性刻画不满足，flag变为0
                    int flag = 1;
                    List<Integer> indexList = rule.indexList;
                    int assNum = indexList.size();
                    for (int k = 0; k < assNum; k++) {
                        int index = indexList.get(k);
                        if (index == ignoreStatistics) {
                            continue;
                        }
                        double ss = rule.ss_ind.get(k);
                        if (X[i][index] < ss) {
                            flag = 0;
                            break;
                        }
                    }
                    if (flag == 1) {
                        funcEnum.add(rule.yIndex);
                    }
                }
                attrMap.put(i, funcEnum);
            }
            // 保存map文件用于相似度函数推荐展示
            saveMap(attrMap,map_savePath);

            PrintStream ps = new PrintStream(output);
            System.setOut(ps);

            // 应用spark进行实体对间相似度计算
            // attention : 根据funcEnum生成特征
            JavaRDD<Row> rdd = dataset.javaRDD();
            int size = attrSize;
            JavaRDD<List<Double>> resRDD = rdd.map(row -> {
                List<Double> simList = new ArrayList<>();
                for (int index = 0; index < size; index += 2) {
                    String v1 = row.getString(index);
                    String v2 = row.getString(index + 1);
                    Set<Integer> funcIDList = attrMap.get(index / 2);
                    for (int ind : funcIDList) {
                        double s;
                        SimilarityFunction function = new SimilarityFunction();
                        if (v1 == null && v2 == null) {
                            s = 1.0;
                        } else if (v1 == null || v2 == null) {
                            s = 0.0;
                        } else if (ind == 0) {
                            // fixme similarity class 1
                            s = function.affineSimilarity(v1, v2);
                        } else if (ind == 1) {
                            s = function.birnbaumSimilarity(v1, v2);
                        } else if (ind == 2) {
                            s = function.bulkDeleteSimilarity(v1, v2);
                        } else if (ind == 3) {
                            s = function.euclideanSimilarity(v1, v2);
                        } else if (ind == 4) {
                            s = function.exactMatch(v1, v2);
                        } else if (ind == 5) {
                            s = function.jaccardUsingHanLP(v1, v2);
                        } else if (ind == 6) {
                            s = function.Jaro(v1, v2);
                        } else if (ind == 7) {
                            s = function.JaroWinklerDistance(v1, v2);
                        } else if (ind == 8) {
                            s = function.kendallTauSimilarity(v1, v2);
                        } else if (ind == 9) {
                            s = function.LevenshteinSimilarity(v1, v2);
                        } else if (ind == 10) {
                            s = function.needleman_wunsch(v1, v2);
                        } else if (ind == 11) {
                            s = function.mmcwpaSimilarity(v1, v2);
                        } else if (ind == 12) {
                            s = function.simpsonSimilarity(v1, v2);
                        } else if (ind == 13) {
                            s = function.simHashSimilarity(v1, v2);
                        } else if (ind == 14) {
                            s = function.abs_norm(v1, v2);
                        } else if (ind == 15) {
                            s = function.rel_diff(v1, v2);
                        } else if (ind == 16) {
                            s = function.cosine_Num(v1, v2);
                        } else if (ind >= 17 && ind <= 21) {
                            // fixme similarity class 2
                            MinHashSimilarity hash = new MinHashSimilarity();
                            hash.calculateSimilarity(v1, v2);
                            if (ind - 17 == 0) {
                                s = function.cosineUsingWordBag(hash.sig1, hash.sig2);
                            } else if (ind - 17 == 1) {
                                s = function.diceForSet(hash.sig1, hash.sig2);
                            } else if (ind - 17 == 2) {
                                s = function.jaccardForSet(hash.sig1, hash.sig2);
                            } else if (ind - 17 == 3) {
                                s = function.LevenshteinDisForSet(hash.sig1, hash.sig2);
                            } else {
                                s = function.monge_elkanForSet(hash.sig1, hash.sig2);
                            }
                        } else if (ind >= 22 && ind <= 26) {
                            // fixme similarity class 3
                            List<String> list1 = strEncoding(v1, "UTF-8");
                            List<String> list2 = strEncoding(v2, "UTF-8");
                            if (ind - 22 == 0) {
                                s = function.cosineUsingWordBag(list1, list2);
                            } else if (ind - 22 == 1) {
                                s = function.diceForSet(list1, list2);
                            } else if (ind - 22 == 2) {
                                s = function.jaccardForSet(list1, list2);
                            } else if (ind - 22 == 3) {
                                s = function.LevenshteinDisForSet(list1, list2);
                            } else {
                                s = function.monge_elkanForSet(list1, list2);
                            }
                        } else if (ind >= 27 && ind <= 31) {
                            // GBK
                            List<String> list1 = strEncoding(v1, "GBK");
                            List<String> list2 = strEncoding(v2, "GBK");
                            if (ind - 27 == 0) {
                                s = function.cosineUsingWordBag(list1, list2);
                            } else if (ind - 27 == 1) {
                                s = function.diceForSet(list1, list2);
                            } else if (ind - 27 == 2) {
                                s = function.jaccardForSet(list1, list2);
                            } else if (ind - 27 == 3) {
                                s = function.LevenshteinDisForSet(list1, list2);
                            } else {
                                s = function.monge_elkanForSet(list1, list2);
                            }
                        } else if (ind >= 32 && ind <= 36) {
                            // fixme similarity class 4
                            List<String> list1 = function.ngram(v1, 3);
                            List<String> list2 = function.ngram(v2, 3);
                            if (ind - 32 == 0) {
                                s = function.cosineUsingWordBag(list1, list2);
                            } else if (ind - 32 == 1) {
                                s = function.diceForSet(list1, list2);
                            } else if (ind - 32 == 2) {
                                s = function.jaccardForSet(list1, list2);
                            } else if (ind - 32 == 3) {
                                s = function.LevenshteinDisForSet(list1, list2);
                            } else {
                                s = function.monge_elkanForSet(list1, list2);
                            }
                        }
                        else if(ind >=37){
                            SimilarityFunction sf = new SimilarityFunction();
                            List<String> list1 = Arrays.asList(v1.split(" "));
                            List<String> list2 = Arrays.asList(v2.split(" "));
                            s= sf.jaccardForSet(list1, list2);
                        }
//                        else if (ind >= 37 && ind <= 41) {
//                            // fixme similarity class 5
//                            WordBag bag = new WordBag();
//                            bag.IndexAnalysisTextSplit(v1, v2);
//                            if (ind - 37 == 0) {
//                                s = function.cosineUsingWordBag(bag.words1, bag.words2);
//                            } else if (ind - 37 == 1) {
//                                s = function.diceForSet(bag.words1, bag.words2);
//                            } else if (ind - 37 == 2) {
//                                s = function.jaccardForSet(bag.words1, bag.words2);
//                            } else if (ind - 37 == 3) {
//                                s = function.LevenshteinDisForSet(bag.words1, bag.words2);
//                            } else {
//                                s = function.monge_elkanForSet(bag.words1, bag.words2);
//                            }
//                        } else if (ind >= 42 && ind <= 46) {
//                            // fixme similarity class 6
//                            WordBag bag = new WordBag();
//                            bag.NlpAnalysisTextSplit(v1, v2);
//                            if (ind - 42 == 0) {
//                                s = function.cosineUsingWordBag(bag.words1, bag.words2);
//                            } else if (ind - 42 == 1) {
//                                s = function.diceForSet(bag.words1, bag.words2);
//                            } else if (ind - 42 == 2) {
//                                s = function.jaccardForSet(bag.words1, bag.words2);
//                            } else if (ind - 42 == 3) {
//                                s = function.LevenshteinDisForSet(bag.words1, bag.words2);
//                            } else {
//                                s = function.monge_elkanForSet(bag.words1, bag.words2);
//                            }
//                        } else if (ind >= 47 && ind <= 51) {
//                            // fixme similarity class 7
//                            WordBag bag = new WordBag();
//                            bag.BaseAnalysisTextSplit(v1, v2);
//                            if (ind - 47 == 0) {
//                                s = function.cosineUsingWordBag(bag.words1, bag.words2);
//                            } else if (ind - 47 == 1) {
//                                s = function.diceForSet(bag.words1, bag.words2);
//                            } else if (ind - 47 == 2) {
//                                s = function.jaccardForSet(bag.words1, bag.words2);
//                            } else if (ind - 47 == 3) {
//                                s = function.LevenshteinDisForSet(bag.words1, bag.words2);
//                            } else {
//                                s = function.monge_elkanForSet(bag.words1, bag.words2);
//                            }
//                        } else if (ind >= 52 && ind <= 56) {
//                            // fixme similarity class 8
//                            WordBag bag = new WordBag();
//                            bag.ToAnalysisTextSplit(v1, v2);
//                            if (ind - 52 == 0) {
//                                s = function.cosineUsingWordBag(bag.words1, bag.words2);
//                            } else if (ind - 52 == 1) {
//                                s = function.diceForSet(bag.words1, bag.words2);
//                            } else if (ind - 52 == 2) {
//                                s = function.jaccardForSet(bag.words1, bag.words2);
//                            } else if (ind - 52 == 3) {
//                                s = function.LevenshteinDisForSet(bag.words1, bag.words2);
//                            } else {
//                                s = function.monge_elkanForSet(bag.words1, bag.words2);
//                            }
//                        }
                        else return null;
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);
                    }
                    // compare autoEM
//                    if(v1==null||v2==null){
//                        if(v1==null&&v2==null){
//                            for(int i = 0;i<4;i++){
//                                System.out.print(1.0);
//                                System.out.print(",");
//                                simList.add(1.0);
//                            }
//                        }else {
//                            for(int i = 0;i<4;i++){
//                                System.out.print(0.0);
//                                System.out.print(",");
//                                simList.add(0.0);
//                            }
//                        }
//                    }else {
//                        double s1;
//                        SimilarityFunction sf = new SimilarityFunction();
//                        List<String> list1 = Arrays.asList(v1.split(" "));
//                        List<String> list2 = Arrays.asList(v2.split(" "));
//                        s1= sf.cosineUsingWordBag(list1, list2);
//                        System.out.print(s1);
//                        System.out.print(",");
//                        simList.add(s1);
//                        s1= sf.diceForSet(list1, list2);
//                        System.out.print(s1);
//                        System.out.print(",");
//                        simList.add(s1);
//                        s1= sf.LevenshteinDisForSet(list1, list2);
//                        System.out.print(s1);
//                        System.out.print(",");
//                        simList.add(s1);
//                        s1= sf.monge_elkanForSet(list1, list2);
//                        System.out.print(s1);
//                        System.out.print(",");
//                        simList.add(s1);
//                    }
                }
                System.out.println();
                return simList;
            });
            resRDD.count();
            spark.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setOut(stdout);
    }

    private void saveMap(Map<Integer, Set<Integer>> attrMap, String map_savePath) {
        try {
            FileOutputStream fos = new FileOutputStream(map_savePath);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(attrMap);
            out.close();
            fos.close();
            System.out.println("save Attribute-similarityFunction Map successfully!");
        } catch (IOException e) {
            System.out.println("cannot save Attribute-similarityFunction Map!");
            throw new RuntimeException(e);
        }

    }

    public void generateMagellanFeatureUseSpark(double[][] X) {
        // 依据ruleList分析X,ruleList表明了属性刻画和相似度函数的关系
        File output = new File(outputPath);
        try {

            Logger.getLogger("org").setLevel(Level.ERROR);
            SparkSession spark = SparkSession.builder()
                    .appName("generateFeatureUseRules")
                    .master("local")
                    .getOrCreate();

            Dataset<Row> dataset = spark.read()
                    .format("csv")
                    .option("header", "true")
                    .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
                    .load(filePath);
            attrSize = dataset.columns().length;
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            // 应用spark进行实体对间相似度计算
            // attention : 根据funcEnum生成特征
            JavaRDD<Row> rdd = dataset.javaRDD();
            int size = attrSize;
            JavaRDD<List<Double>> resRDD = rdd.map(row -> {
                List<Double> simList = new ArrayList<>();
                for (int index = 0; index < size; index += 2) {
                    String v1 = row.getString(index);
                    String v2 = row.getString(index + 1);
                    // 只考虑长度
                    double avgLength = (X[index / 2][0] + X[index / 2][4]) / 2;
                    double s;
                    SimilarityFunction function = new SimilarityFunction();
                    if (avgLength <= 1) {
                        s = function.LevenshteinSimilarity(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.Jaro(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.JaroWinklerDistance(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.exactMatch(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                    } else if (avgLength > 1 && avgLength <= 5) {
                        s = function.LevenshteinSimilarity(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.needleman_wunsch(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.monge_elkan(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.cosineForWord(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.jaccardUsingSpace(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.jaccardUsing3Gram(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                    } else if (avgLength > 5 && avgLength <= 10) {
                        s = function.LevenshteinSimilarity(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.monge_elkan(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.cosineForWord(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.jaccardUsing3Gram(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                    } else if (avgLength > 10) {
                        s = function.cosineForWord(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);

                        s = function.jaccardUsing3Gram(v1, v2);
                        System.out.print(s);
                        System.out.print(",");
                        simList.add(s);
                    }
                }
                System.out.println();
                return simList;
            });
            resRDD.count();
            spark.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setOut(stdout);
    }


    private void printAttr(String[] attr) {
        for (int index = 0; index < attrSize; index = index + 2) {
            String name = attr[index];
            //print(name + "_embedding");
            print(name + "_affine");
            print(name + "_birnbaum");
            print(name + "_bulkDelete");
            print(name + "_euclidean");
            print(name + "_exactMatch");
            print(name + "_jaccard");
            print(name + "_jaro");
            print(name + "_jaroWinkler");
            print(name + "_kendallTau");
            print(name + "_levenshtein");
            print(name + "_needleman");
            print(name + "_mmcwpa");
            print(name + "_simpson");
            print(name + "_simHash");
            print(name + "abs_norm");
            print(name + "rel_diff");
            print(name + "cosine_Num");

            print(name + "_minHash1");
            print(name + "_minHash2");
            print(name + "_minHash3");
            print(name + "_minHash4");
            print(name + "_minHash5");

            print(name + "ascii1");
            print(name + "ascii2");
            print(name + "ascii3");
            print(name + "ascii4");
            print(name + "ascii5");

            print(name + "asciiGBK1");
            print(name + "asciiGBK2");
            print(name + "asciiGBK3");
            print(name + "asciiGBK4");
            print(name + "asciiGBK5");
            print(name + "NGRAM1");
            print(name + "NGRAM2");
            print(name + "NGRAM3");
            print(name + "NGRAM4");
            print(name + "NGRAM5");
            // 长文本默认使用
            print(name + "indexAnaly1");
            print(name + "indexAnaly2");
            print(name + "indexAnaly3");
            print(name + "indexAnaly4");
            print(name + "indexAnaly5");

            print(name + "NLPAnaly1");
            print(name + "NLPAnaly2");
            print(name + "NLPAnaly3");
            print(name + "NLPAnaly4");
            print(name + "NLPAnaly5");

            print(name + "baseAnaly1");
            print(name + "baseAnaly2");
            print(name + "baseAnaly3");
            print(name + "baseAnaly4");
            print(name + "baseAnaly5");

            print(name + "toAnaly1");
            print(name + "toAnaly2");
            print(name + "toAnaly3");
            print(name + "toAnaly4");
            print(name + "toAnaly5");

        }
        System.out.println();
    }

    private void asciiPrint(String value1, String value2) {
        // utf-8
        List<String> list1 = strEncoding(value1, "UTF-8");
        List<String> list2 = strEncoding(value2, "UTF-8");
        listSimilarity(list1, list2);
        // GBK
        List<String> gbkList1 = strEncoding(value1, "GBK");
        List<String> gbkList2 = strEncoding(value2, "GBK");
        listSimilarity(gbkList1, gbkList2);
    }

    /**
     * use 10 kinds of hash
     *
     * @param value1
     * @param value2
     */
    private void hashPrint(String value1, String value2) {
        MinHashSimilarity hash = new MinHashSimilarity();
        hash.calculateSimilarity(value1, value2);
        listSimilarity(hash.sig1, hash.sig2);
    }

    private static List<String> strEncoding(String str, String encode) {
        List<String> list = new ArrayList<>();
        if (encode.equals("UTF-8")) {
            // Convert to ASCII code sequence
            byte[] asciiBytes = str.getBytes(StandardCharsets.US_ASCII);
            // System.out.print("ASCII code sequence: ");
            for (byte b : asciiBytes) {
                list.add("0x" + String.format("%02X", b));
            }
        } else if (encode.equals("GBK")) {
            // Convert to GBK code sequence
            byte[] gbkBytes = str.getBytes(Charset.forName("GBK"));
            // System.out.print("GBK code sequence: ");
            for (byte b : gbkBytes) {
                list.add("0x" + String.format("%02X", b));
            }
        }
        return list;
    }

    private void print(double n) {
        System.out.print(n);
        System.out.print(",");
    }

    private void print(String name, double n) {
        System.out.print(name + "_" + n);
        System.out.print(",");
    }

    private void print(String n) {
        System.out.print(n);
        System.out.print(",");
        lineLength++;
    }


    private void indexAnaly(String value1, String value2) {
        WordBag bag = new WordBag();
        bag.IndexAnalysisTextSplit(value1, value2);
        WordBagPrint(bag);
    }

    private void nlpAnaly(String value1, String value2) {
        WordBag bag = new WordBag();
        bag.NlpAnalysisTextSplit(value1, value2);
        WordBagPrint(bag);
    }

    private void toAnaly(String value1, String value2) {
        WordBag bag = new WordBag();
        bag.ToAnalysisTextSplit(value1, value2);
        WordBagPrint(bag);
    }

    private void baseAnaly(String value1, String value2) {
        WordBag bag = new WordBag();
        bag.BaseAnalysisTextSplit(value1, value2);
        WordBagPrint(bag);
    }

    private void WordBagPrint(WordBag bag) {
        listSimilarity(bag.words1, bag.words2);
    }

    private void listSimilarity(List<String> list1, List<String> list2) {
        SimilarityFunction func = new SimilarityFunction();
        System.out.print(func.cosineUsingWordBag(list1, list2));
        System.out.print(",");
        System.out.print(func.diceForSet(list1, list2));
        System.out.print(",");
        System.out.print(func.jaccardForSet(list1, list2));
        System.out.print(",");
        System.out.print(func.LevenshteinDisForSet(list1, list2));
        System.out.print(",");
        System.out.print(func.monge_elkanForSet(list1, list2));
        System.out.print(",");
    }

    private void ngramPrint(String str1, String str2, int n) {
        SimilarityFunction func = new SimilarityFunction();
        List<String> list1 = func.ngram(str1, n);
        List<String> list2 = func.ngram(str2, n);
        listSimilarity(list1, list2);
    }

    public void addLabel(String labelPath, String labeledSimPath, boolean withHeader) {
        try {
            // 读取unlabeled sim data
            BufferedReader brUnlabelSim = new BufferedReader(new FileReader(outputPath));
            // 读取label data
            BufferedReader brLabel = new BufferedReader(new FileReader(labelPath));
            // 制定输出路径
            File labeledSim = new File(labeledSimPath);
            PrintStream ps = new PrintStream(labeledSim);
            System.setOut(ps);
            String str;
            int length = 0;
            if (!withHeader) {
                brLabel.readLine();
            }
            while ((str = brUnlabelSim.readLine()) != null) {
                System.out.print(str);
                length = str.split(",", -1).length;
                str = brLabel.readLine();
                System.out.println(str);
            }
            System.setOut(stdout);
            System.out.println(length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
