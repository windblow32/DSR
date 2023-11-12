package Similarity;

import org.junit.jupiter.api.Test;
import tools.GenerateFeatureWithSim;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalculateSimilarity {
    /**
     * 计算computer数据相似度
     */
    @Test
    public void calculateSimilarity(){
        String simName = "in_com_dblp_out_dblp_8.6";
        // dblp
        GenerateFeatureWithSim generateDblp = new GenerateFeatureWithSim();
        generateDblp.filePath = "data/dblp-scholar/chinese/similarity/chinese_dblp-unlabeled.csv";
        generateDblp.outputPath = "data/dblp-scholar/chinese/similarity/"+simName+".csv";
        List<Integer> list2 = new ArrayList<>();

        generateDblp.longTextIndexList = list2;
        List<Integer> numericalList2 = new ArrayList<>();
//        numericalList.add(6);
//        numericalList.add(7);
        generateDblp.numericalIndexList = numericalList2;
        generateDblp.calcSim();
    }

    @Test
    public void chineseInsertCSV(){
        File f = new File("data/computer/chinese/test.csv");
        File output = new File("data/computer/chinese/output.csv");
        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String[] data;
            int i = 0;
            while((str = br.readLine())!=null){
                System.out.print(str);
                if(i%4==3){
                    System.out.println();
                }else {
                    System.out.print("=");
                }
                i++;
            }
            fr.close();
            br.close();
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
