package pre_experiment;

import Statistics.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StringStatics {
    public double[] arrayIC;
    public double[] arrayEntropy;
    public double[] arrayLength;
    public double[] arrayAvg;
    public double[] arrayAvgWord;
    public double[] arrayCharRatio;
    public double[] arrayLong;
    public double[] arrayShort;
    public int attrNum = 10;
    public String savePath;

    // attention : 计算属性刻画
    public void calculateStatistic_Spark(String path) {
        Logger.getLogger("org").setLevel(Level.ERROR);


        SparkSession spark = SparkSession.builder()
                .appName("SparkSessionExample")
                .master("local[*]")
                .getOrCreate();
        Dataset<Row> dataset = spark.read()
                .option("header", "true") // 指定CSV文件的第一行为列名
                .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
                .csv(path);

        attrNum = dataset.columns().length;
        int length = attrNum;
        arrayIC = new double[length];
        arrayEntropy = new double[length];
        arrayLength = new double[length];
        arrayAvg = new double[length];
        // 将 Dataset<Row> 转换为 JavaRDD<Row>
        // 将 Dataset<Row> 转换为 JavaRDD<Row>
        JavaRDD<Row> rdd = dataset.javaRDD();
        long lines = rdd.count();
        // fixme : attrNum赋值
        for(int i = 0;i<attrNum;i++){
            int finalI = i;
            JavaRDD<Double> calcICRDD = rdd.map(row -> {
                IC ic = new IC();
                String t = row.getString(finalI);
                if(t!=null){
                    return ic.calculate(t);
                }else return 0.0;
            });
            JavaRDD<Double> calcEntropyRDD = rdd.map(row -> {
                Entropy tool = new Entropy();
                String t = row.getString(finalI);
                if(t!=null){
                    return tool.entropy(t);
                }else return 0.0;
            });
            JavaRDD<Double> calcLengthRDD = rdd.map(row -> {
                String s = row.getString(finalI);
                String t = row.getString(finalI);
                if(t!=null){
                    return (double)s.length();
                }else return 0.0;
            });
            JavaRDD<Double> calcAvgNumRDD = rdd.map(row -> {
                AvgNumCalc csy = new AvgNumCalc();
                String t = row.getString(finalI);
                if(t!=null){
                    return csy.cal_avg(t);
                }else return 0.0;
            });
            JavaRDD<Double> calcAvgWordRDD = rdd.map(row -> {
                AvgWordLenCalc csy = new AvgWordLenCalc();
                String t = row.getString(finalI);
                if(t!=null){
                    return csy.avgWordLen(t);
                }else return 0.0;
            });
            JavaRDD<Double> charWordRatioRDD = rdd.map(row -> {
                CharacterWordRatio csy = new CharacterWordRatio();
                String t = row.getString(finalI);
                if(t!=null){
                    return csy.characterRatio(t);
                }else return 0.0;
            });
            JavaRDD<Double> shortestRDD = rdd.map(row -> {
                ShortestWordCalc csy = new ShortestWordCalc();
                String t = row.getString(finalI);
                if(t!=null){
                    return csy.shortestWordLen(t);
                }else return 0.0;
            });
            JavaRDD<Double> longestRDD = rdd.map(row -> {
                LongestWordCalc csy = new LongestWordCalc();
                String t = row.getString(finalI);
                if(t!=null){
                    return csy.longestWordLen(t);
                }else return 0.0;
            });
            // 使用 reduce 对计算结果进行叠加
            arrayIC[i] = calcICRDD.reduce((a, b) -> a + b)/lines;
            arrayEntropy[i] = calcEntropyRDD.reduce((a, b) -> a + b)/lines;
            arrayLength[i] = calcLengthRDD.reduce((a, b) -> a + b)/lines;
            arrayAvg[i] = calcAvgNumRDD.reduce((a, b) -> a + b)/lines;
            // 扩展规则库
//            arrayAvgWord[i] = calcAvgWordRDD.reduce((a, b) -> a + b)/lines;
//            arrayCharRatio[i] = charWordRatioRDD.reduce((a, b) -> a + b)/lines;
//            arrayLong[i] = longestRDD.reduce((a, b) -> a + b)/lines;
//            arrayShort[i] = shortestRDD.reduce((a, b) -> a + b)/lines;
        }
        spark.stop();
    }


    public void calcSSFromIndexList(List<Integer> indexList, int attrLength, String unlabelPath) {
        String str;
        String[] data;
        List<String> originUnlabel = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(unlabelPath));
            br.readLine();
            while ((str = br.readLine()) != null) {
                originUnlabel.add(str);
            }
            IC ic = new IC();
            Entropy tool = new Entropy();
            AvgNumCalc csy = new AvgNumCalc();
            AvgWordLenCalc avgWord = new AvgWordLenCalc();
            CharacterWordRatio characterWordRatio = new CharacterWordRatio();
            LongestWordCalc longestWordCalc = new LongestWordCalc();
            ShortestWordCalc shortestWordCalc = new ShortestWordCalc();

            arrayIC = new double[attrLength];
            arrayEntropy = new double[attrLength];
            arrayLength = new double[attrLength];
            arrayAvg = new double[attrLength];
            arrayAvgWord = new double[attrLength];
            arrayCharRatio = new double[attrLength];
            arrayLong = new double[attrLength];
            arrayShort = new double[attrLength];
            int line = 0;
            for (int index : indexList) {
                str = originUnlabel.get(index);
                if (str.equals("")) {
                    int das = 312;
                }
                data = str.split(",", -1);
                for (int i = 0; i < data.length; i++) {
                    arrayIC[i] += ic.calculate(data[i]);
                    arrayEntropy[i] += tool.entropy(data[i]);
                    arrayLength[i] += (double) data[i].length();
                    arrayAvg[i] += csy.cal_avg(data[i]);
                    arrayAvgWord[i] += avgWord.avgWordLen(data[i]);
                    arrayCharRatio[i] += characterWordRatio.characterRatio(data[i]);
                    arrayLong[i] += longestWordCalc.longestWordLen(data[i]);
                    arrayShort[i] +=shortestWordCalc.shortestWordLen(data[i]);
                }
                line++;
            }
            for (int i = 0; i < attrLength; i++) {
                arrayIC[i] = arrayIC[i] / line;
                arrayEntropy[i] = arrayEntropy[i] / line;
                arrayLength[i] = arrayLength[i] / line;
                arrayAvg[i] = arrayAvg[i] / line;
                arrayAvgWord[i] += arrayAvgWord[i] / line;
                arrayCharRatio[i] += arrayCharRatio[i] / line;
                arrayLong[i] += arrayLong[i] / line;
                arrayShort[i] += arrayShort[i] / line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param subsets 子集划分结果
     * @param allCSV  存储所有simFunc计算结果的文件，包括label
     * @param output  结果输出位置，SS文件
     */
    public void statisticsFromSubset(String subsets, String allCSV, String output, String unlabelPath, int attrLength) {
        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);

            FileReader frSub = new FileReader(subsets);
            BufferedReader brSub = new BufferedReader(frSub);

            BufferedReader brAllCSV = new BufferedReader(new FileReader(allCSV));
            String str;
            String[] data;
            brAllCSV.readLine();
            List<String> sim = new ArrayList<>();
            while ((str = brAllCSV.readLine()) != null) {
                sim.add(str);
            }
            List<Integer> indexList = new ArrayList<>();
            while ((str = brSub.readLine()) != null) {
                if (str.contains("modelID")) {
                    // fixme : 最后一个id的无法被预测
                    if (!indexList.isEmpty()) {
                        calcSSFromIndexList(indexList, attrLength, unlabelPath);
                        indexList.clear();
                        // fixme : attrNum
                        for (int i = 0; i < attrLength; i = i + 2) {
                            System.out.print((arrayLength[i]+arrayLength[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayEntropy[i]+arrayEntropy[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayIC[i]+arrayIC[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayAvg[i]+arrayAvg[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayAvgWord[i]+arrayAvgWord[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayCharRatio[i]+arrayCharRatio[i + 1])/2);
                            System.out.print(",");
                            System.out.print((arrayLong[i]+arrayLong[i + 1])/2);
                            System.out.print(",");
                            System.out.println((arrayShort[i]+arrayShort[i + 1])/2);
                        }
                    }
                }
                for (String s : sim) {
                    // s是str的子串即可,因为str来自子集划分结果
                    if (compareNumberStrings(s, str)) {
                        indexList.add(sim.indexOf(s));
                        break;
                    }
                }
            }
            // 最后一个block的信息存在indexList没被读出
            if (!indexList.isEmpty()) {
                calcSSFromIndexList(indexList, attrLength, unlabelPath);
                indexList.clear();
                // fixme : attrNum
                for (int i = 0; i < attrLength; i = i + 2) {
                    System.out.print((arrayLength[i]+arrayLength[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayEntropy[i]+arrayEntropy[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayIC[i]+arrayIC[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayAvg[i]+arrayAvg[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayAvgWord[i]+arrayAvgWord[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayCharRatio[i]+arrayCharRatio[i + 1])/2);
                    System.out.print(",");
                    System.out.print((arrayLong[i]+arrayLong[i + 1])/2);
                    System.out.print(",");
                    System.out.println((arrayShort[i]+arrayShort[i + 1])/2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fixme
    public boolean compareNumberStrings(String str1, String str2) {
        String[] tokens1 = str1.split(",", -1);
        String[] tokens2 = str2.split(",", -1);

        if (tokens1.length != tokens2.length) {
            return false;
        }

        for (int i = 0; i < tokens1.length; i++) {
            String num1 = tokens1[i].trim();
            String num2 = tokens2[i].trim();
            if ((num1.equals("") && num2.equals("NaN")) || num1.equals("NaN") && num2.equals("")) {
                continue;
            }
            if (!num1.equals(num2) && !num1.equals(num2 + ".0") && !num2.equals(num1 + ".0")) {
                return false;
            }
        }

        return true;
    }


}
