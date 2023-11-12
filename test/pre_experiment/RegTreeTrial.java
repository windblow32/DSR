package pre_experiment;

import org.junit.Test;

import java.io.*;


import static java.lang.System.exit;

public class RegTreeTrial {
    @Test
    /**
     * 生成trial中X的属性刻画部分，一共6*8
     */
    public void generateX(){
        // 读取SS.csv文件，attrlength=10为单位读取10行，成为X中一行
        // 同时在前面加上元向量,数值参考python,如下：
//        MF_Abt_buy = [4, 4, 0.5, 8, 38451, 521, 0.024, 0.0002, 983, 0.006, 0.0001, 236.4384, 583.2711, 291.4855, 667.2714,
//                161.9914,
//                899.4379]
//        MF_Computer = [0, 16, 0, 16, 17600, 6, 0.9618, 0.0045, 968, 0.0036, 0.0009, 0, 0, 0, 0, 0, 0]
//        MF_DBLP = [4, 6, 0.4, 10, 165067, 6, 0.3301, 0.0002, 7207, 0.0151, 0.0001, 1361.712, 31946.38, 2.972923, 18321.36,
//                15083.52, 15959.34]
        // attention : 处理block文件时，每个block生成了四个属性上的8个刻画，存储为四行。这里再次组装一下
        int attrLength = 8;
        File output = new File("data/computer/chinese/SSnew.csv");
        File ss = new File("data/computer/chinese/trialX_new.csv");
        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            BufferedReader br = new BufferedReader(new FileReader(ss));
            String str;
            String[] data;
            int line = 0;

            while((str = br.readLine())!=null){
                if(line%4==0){
                    System.out.print("0,16,0,16,17600,6,0.9618,0.0045,968,0.0036,0.0009,0,0,0,0,0,0");
                }

                System.out.print(str);
                // 每个属性的left和right的属性刻画在一行，所以需要组合SS中的结果
                if(line%4!=3){
                    System.out.print(",");
                }else {
                    System.out.println();
                }
                line++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    /**
     * 读取之前的trialX文件，每行在后添加5元组，
     * 原来是29个，为了加快实验，先用14个（另外15个是hash和ascii，gbk编码，一人5个）
     */
    public void generate8tuple(){
        File trail = new File("data/computer/chinese/trialX_new.csv");
        File output = new File("data/computer/chinese/trial_addTuple_new.csv");
        try {
            BufferedReader br = new BufferedReader(new FileReader(trail));
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            String str;
            int line = 0;
            while((str = br.readLine())!=null){
                for(int i = 0;i<14;i++){
                    for(int j = 0;j<14;j++){
                        for(int k = 0;k<14;k++){
                            for(int p = 0;p<14;p++){
                                for(int q = 0;q<14;q++){
                                    System.out.print(str);
                                    System.out.print(",");
                                    System.out.print(i);
                                    System.out.print(",");
                                    System.out.print(j);
                                    System.out.print(",");
                                    System.out.print(k);
                                    System.out.print(",");
                                    System.out.print(p);
                                    System.out.print(",");
                                    System.out.println(q);
                                    line++;
                                    if(line==82000){
                                        exit(10);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
