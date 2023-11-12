package tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReadCsv {
    public void csv2arff() {
        // load csv
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File("data/modelshare test data/birdmap-samples.csv"));
            Instances data = loader.getDataSet();
            data.setClassIndex(2);

            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("data/modelshare test data/birdmap-samples.arff"));
            //saver.setDestination(new File(args[1]));
            saver.writeBatch();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CSVRecord> readData(String path) {
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

    public void unionTables() {
        List<CSVRecord> tableA = readData("data/abt-buy/tableA.csv");
        List<CSVRecord> tableB = readData("data/abt-buy/tableB.csv");
        List<CSVRecord> labelTable = readData("data/abt-buy/train.csv");
        String filePath = "data/abt-buy/output.csv";

        // 定义CSV文件格式
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("ltable_id", "rtable_id", "ltable_name", "rtable_name",
                "ltable_description", "rtable_description", "ltable_price", "rtable_price"
        );
        try {
            // 创建CSVPrinter对象
            CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(new File(filePath)), csvFormat);
            // 输出含有逗号的内容
            for (CSVRecord record : labelTable) {
                String idA = record.get(0);
                String idB = record.get(1);
                String label = record.get(2);
                CSVRecord rA = null;
                CSVRecord rB = null;
                for (CSVRecord a : tableA) {
                    if (a.get(0).equals(idA)) {
                        rA = a;
                        break;
                    }
                }
                for (CSVRecord b : tableB) {
                    if (b.get(0).equals(idB)) {
                        rB = b;
                        break;
                    }
                }
                if (rA != null && rB != null) {
                    csvPrinter.printRecord(combineRecords(rA, rB, csvFormat));
                } else {
                    System.exit(-150);
                }
            }
            // 关闭CSVPrinter
            csvPrinter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String valuesToCsvString(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value).append(",");
        }
        sb.deleteCharAt(sb.length() - 1); // 删除最后一个逗号
        return sb.toString();
    }

    private CSVRecord combineRecords(CSVRecord record1, CSVRecord record2, CSVFormat csvFormat) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < record1.size(); i++) {
            values.add(record1.get(i));
            values.add(record2.get(i));
        }
        // 从 CSVParser 对象中获取第一行 CSVRecord 对象
        CSVRecord record = null;
        try {
            // 将 CSV 格式字符串解析为 CSVParser 对象
            CSVParser parser = CSVParser.parse(valuesToCsvString(values), csvFormat);
            record = parser.getRecords().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return record;
    }

    public static String getFileCharsetName(String fileName) throws IOException {
        InputStream inputStream = new FileInputStream(fileName);
        byte[] head = new byte[3];
        inputStream.read(head);

        String charsetName = "GBK";//或GB2312，即ANSI
        if (head[0] == -1 && head[1] == -2) //0xFFFE
            charsetName = "UTF-16";
        else if (head[0] == -2 && head[1] == -1) //0xFEFF
            charsetName = "Unicode";//包含两种编码格式：UCS2-Big-Endian和UCS2-Little-Endian
        else if (head[0] == -27 && head[1] == -101 && head[2] == -98)
            charsetName = "UTF-8"; //UTF-8(不含BOM)
        else if (head[0] == -17 && head[1] == -69 && head[2] == -65)
            charsetName = "UTF-8"; //UTF-8-BOM

        inputStream.close();

        //System.out.println(code);
        return charsetName;
    }

    public static BufferedReader getBr(String filePath) {
        File f = new File(filePath);
        String encode = null;
        BufferedReader br;
        try {
            encode = getFileCharsetName(filePath);

            if (encode.equals("GBK")) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("GBK")));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            }
            return br;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("no br");
        System.exit(-200);
        return null;
    }
}

