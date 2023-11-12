package tools_test;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import sim_func.SimilarityFunction;
import sim_func.WordBag;
import tools.TranslateChinese;
import java.io.*;
import java.security.NoSuchAlgorithmException;

public class TransChinese {
    @Test
    public void run(int start, int end){
        TranslateChinese tool = new TranslateChinese();
        try {
            File f = new File("data/computer/large/label data.csv");
            File output = new File("data/computer/chinese/test.csv");
//            PrintStream ps = new PrintStream(output);
//            System.setOut(ps);
            FileOutputStream fs = new FileOutputStream(output, true);
            OutputStreamWriter osw = new OutputStreamWriter(fs, "UTF-8");

            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            // read attr;
            br.readLine();
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(br);
            int i = 0;
            // attention : 每次运行 + 1
            // int index = 101;
            for (CSVRecord record : records) {
                if(i >= start&& i < end){
//                    System.out.println(tool.trans(record.get(13)));
//                    System.out.println(tool.trans(record.get(14)));
                    osw.write(tool.trans(record.get(9)));
                    osw.write("\n");
                    osw.write(tool.trans(record.get(10)));
                    osw.write("\n");
                    osw.write(tool.trans(record.get(13)));
                    osw.write("\n");
                    osw.write(tool.trans(record.get(14)));
                    osw.write("\n");
                }
                if(i>=end){
                    break;
                }
                i++;
            }
            osw.close();
            // ps.close();
            fr.close();
            br.close();
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void test(){
        String str1 = "我和妹妹清明节时候去哈工大去踏青";
        String str2 = "梁铮和孙毅清明节时候去哈工大唱歌";
        SimilarityFunction func = new SimilarityFunction();
        WordBag bag = new WordBag();
        bag.NlpAnalysisTextSplit(str1, str2);
        double sim = func.jaccardForSet(bag.words1, bag.words2);
        System.out.println(sim);
    }

    @Test
    public void api(){
        try {
            for(int i = 0;i<1000;i++){
                run(i,i+1);
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

