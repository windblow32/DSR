package tools_test;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class UnionTableUtil {
    /**
     * outputJoin：输出的拼接表
     * label_output：label文件ß
     */
    @Test
    public void prepare(){
        String datasetName = "Amazon-Google";
        String tableA = "/Users/ovoniko/Downloads/label_a_b/dataset/entity_matching/structured/"+datasetName+"/tableA.csv";
        String tableB = "/Users/ovoniko/Downloads/label_a_b/dataset/entity_matching/structured/"+datasetName+"/tableB.csv";
        String labelPath = "/Users/ovoniko/Downloads/label_a_b/dataset/entity_matching/structured/"+datasetName+"/train.csv";
        String outputJoin = "/Users/ovoniko/Downloads/label_a_b/dataset/entity_matching/structured/"+datasetName+"/"+datasetName+".csv";
        String label_output = "/Users/ovoniko/Downloads/label_a_b/dataset/entity_matching/structured/"+datasetName+"/"+datasetName+"-label.csv";
        try {
            PrintStream ps = new PrintStream(outputJoin);
            System.setOut(ps);
            FileReader labelReader = new FileReader(labelPath);
            CSVParser labelParser = new CSVParser(labelReader, CSVFormat.DEFAULT);
            List<CSVRecord> labelRecords = labelParser.getRecords();
            labelRecords.remove(0);
            FileReader tableAReader = new FileReader(tableA);
            CSVParser tableAParser = CSVFormat.DEFAULT.parse(tableAReader);
            List<CSVRecord> tableA_Record = tableAParser.getRecords();
            CSVRecord headers = tableA_Record.get(0);
            tableA_Record.remove(0);
            List<CSVRecord> tableB_Record = CSVFormat.DEFAULT.parse(new FileReader(tableB)).getRecords();
            tableB_Record.remove(0);
            // output headers
            for(int i = 1;i<headers.size();i++){
                System.out.print("ltable_"+headers.get(i));
                System.out.print(",");
                System.out.print("rtable_"+headers.get(i));
                if(i<headers.size()-1){
                    System.out.print(",");
                }
            }
            System.out.println();
            for(CSVRecord record : labelRecords){
                String left_id = record.get(0);
                String right_id = record.get(1);
                String label = record.get(2);
                CSVRecord left_record = null;
                CSVRecord right_record = null;
                // 在两个表格中查找label
                for(CSVRecord l : tableA_Record){
                    if(l.get(0).equals(left_id)){
                        left_record = l;
                        break;
                    }
                }
                for(CSVRecord r : tableB_Record){
                    if(r.get(0).equals(right_id)){
                        right_record = r;
                        break;
                    }
                }
                // 拼接左右record并添加标签，i=1为id，舍去
                String unionStr = "";
                for(int i = 1;i<headers.size();i++){
                    unionStr += left_record.get(i)+","+right_record.get(i);
                    if(i<headers.size()-1){
                        unionStr += ",";
                    }
                }
                System.out.println(unionStr);
            }
            PrintStream ps_label = new PrintStream(label_output);
            System.setOut(ps_label);
            for(CSVRecord record : labelRecords){
                System.out.println(record.get(2));
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
