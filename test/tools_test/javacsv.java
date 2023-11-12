package tools_test;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import tools.ReadCsv;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class javacsv {
    // 生成entity或者csv转arff
    /*
    可以容忍单元格内涵盖分隔符的情况
     */
    @Test
    public void test(){
        try {
            File f = new File("data/schema mapping/user.csv");
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            // read attr;
            br.readLine();
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(br);
            for (CSVRecord record : records) {
                System.out.println("Record #: " + record.getRecordNumber());
                System.out.println("ID: " + record.get(0));
                System.out.println("address: " + record.get(1));
                System.out.println("num: " + record.get(2));

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @Test
    public void transformArff(){
        // load csv
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File("data/computer/large/label data.csv"));
            Instances data = loader.getDataSet();
            // data.setClassIndex(21);

            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("data/arff for weka/computer.arff"));
            //saver.setDestination(new File(args[1]));
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInstance(){
        ArffLoader loader = new ArffLoader();
        try {
            loader.setSource(new File("data/arff for weka/computer.arff"));
            Instances data = loader.getDataSet();
            data.deleteAttributeAt(1);
            data.renameAttribute(1,"attr"+ "1" + "_jaccard");
            data.get(1).setValue(1,0.8);
            data.deleteAttributeAt(2);
            data.renameAttribute(2,"attr"+ "2" + "_jaccard");
            System.out.println(data.get(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<CSVRecord> readData(String path){
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = new File(path);
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            // read attr;
            br.readLine();
            return CSVFormat.DEFAULT.parse(br).getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("read data error");
        return null;
    }
    @Test
    public void testMonkey(){
        List<CSVRecord> records = readData("data/sys_monkeypox.csv");
        for(CSVRecord record : records){
            System.out.println(record);
        }
    }

    @Test
    public void computerEntity(){
        try {
            File f = new File("data/computer/large/label data.csv");
            File s = new File("data/computer/large/entity.csv");
            PrintStream ps = new PrintStream(s);
            System.setOut(ps);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            // read attr;
            br.readLine();
            String str;
            String[] data;
            Set<String> set = new HashSet<>();
            while((str = br.readLine())!=null){
                data = str.split(",",-1);
                int left = 0;
                int right = 0;
                if(!set.contains(data[1])){
                    left = 1;
                }
                if(!set.contains(data[2])){
                    right = 1;
                }
                if(left == 1 || right == 1){
                    String newStr1 = "";
                    String newStr2 = "";
                    for(int i = 1;i< data.length -1 ;i++){
                        if(i%2==1&&left == 1){
                            newStr1 += data[i];
                            if(i != data.length - 3){
                                newStr1 += ",";
                            }
                        }else if(i%2==0&&right == 1){
                            newStr2 += data[i];
                            if(i != data.length - 2){
                                newStr2 += ",";
                            }
                        }
                    }
                    if(!newStr1.equals("")){
                        System.out.println(newStr1);
                    }
                    if(!newStr2.equals("")){
                        System.out.println(newStr2);
                    }
                }
                set.add(data[1]);
                set.add(data[2]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testUnion(){
        ReadCsv read = new ReadCsv();
        read.unionTables();
    }

    @Test
    public void unionColumn(){
        String path = "data/computer/chinese/label data.csv";
        File column = new File(path);
        try {
            String encode = ReadCsv.getFileCharsetName(path);
            if(encode.equals("GBK")){
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(column), Charset.forName("GBK")));
                String str;
                while((str = br.readLine())!=null){
                    int a = 1;
                }
                br.close();
            }else {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(column), StandardCharsets.UTF_8));
                String str;
                while((str = br.readLine())!=null){
                    int a = 1;
                }
                br.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncode(){
        File column = new File("data/dblp-scholar/chinese/dblp_d.txt.out");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(column), Charset.forName("GBK")));

            String str;
            while((str = br.readLine())!=null){
                int a = 1;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String deletezero(String input){
        String pattern = "\\.0$";
        String result = "";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);

        if (matcher.find()) {
            result = input.substring(0, input.length() - 2);
        } else {
            result = input;
        }
        return result;
    }

    @Test
    /**
     * 如果all,Rou中的文件存储的相似度是以.0结尾的，需要删除，和all.csv中存储的一致，否则找不到
     */
    public void dealRou(){
        String path = "data/dblp-scholar/chinese/block/all,ROU=0.1block.csv";
        File f = new File(path);
        File output = new File("data/dblp-scholar/chinese/block/output.csv");

        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str;
            String[] data;
            while((str = br.readLine())!=null){
                data = str.split(",",-1);
                for(int i = 0;i< data.length;i++){
                    System.out.print(deletezero(data[i]));
                    if(i != data.length-1){
                        System.out.print(",");
                    }else {
                        System.out.println();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void merge() {
        String sourceFolderPath = "";
        String targetFilePath = "";

        // 创建目标文件
        File targetFile = new File(targetFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            // 获取源文件夹下的所有文件
            File sourceFolder = new File(sourceFolderPath);
            File[] files = sourceFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        // 读取txt文件内容并写入目标文件
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    }
                }
            }

            System.out.println("所有txt文件已合并到目标文件：" + targetFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
