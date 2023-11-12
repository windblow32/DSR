package tools_test;



import dataMining.TrialMeta;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class ProcessData {
    @Test
    public void sort(){
        List<Integer>indexSortByX1 = new ArrayList<>();
        indexSortByX1.add(0);
        indexSortByX1.add(1);
        indexSortByX1.add(1);
        indexSortByX1.add(1);
        indexSortByX1.add(2);
        Collections.sort(indexSortByX1);
        System.out.print(indexSortByX1.get(4));
    }

    @Test
    public void addLabel(){
        File goldenStandard = new File("/Users/ovoniko/Documents/GitHub/EntityMatching/data/huawei/dataset/songs/matches_msd_msd.csv");
        File msd = new File("/Users/ovoniko/Documents/GitHub/EntityMatching/data/huawei/dataset/songs/data_discoverysongs_x.csv");
        try {
            PrintStream ps = new PrintStream("data/huawei/dataset/songs/labeled_songs.csv");
            System.setOut(ps);
            BufferedReader br = new BufferedReader(new FileReader(goldenStandard));
            BufferedReader msdBr = new BufferedReader(new FileReader(msd));
            String str;
            String[] data;

            List<String[]> arrayList = new ArrayList<>();
            br.readLine();
            while((str = br.readLine())!=null){
                String[] array = new String[2];
                str = str.replaceAll("\"","");
                data = str.split(",",-1);
                array[0] = data[0];
                array[1] = data[1];
                arrayList.add(array);
            }
            while((str = msdBr.readLine())!=null){
                int flag = 0;
                str = str.replaceAll("\"","");
                data = str.split(",",-1);
                for(String[] arr: arrayList){
                    if((data[0].equals(arr[0])&&data[1].equals(arr[1]))||
                            (data[0].equals(arr[1])&&data[1].equals(arr[0]))){
                        System.out.println("1");
                        flag=1;
                        break;
                    }
                }
                if(flag==0){
                    System.out.println("0");
                }

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void delete(){
        File output = new File("data/computer/large/B.csv");

        File f = new File("data/computer/large/tableB.csv");
        try {
            output.createNewFile();
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);

            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String[] data;
            Set<String> idSet = new HashSet<>();
            while((str = br.readLine())!=null){
                data = str.split(",",-1);
                if(!idSet.contains(data[0])){
                    System.out.println(str);
                }
                idSet.add(data[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test(){

        File outputFile = new File("data/dblp_trialX.csv");
        File inputFile = new File("data/trail/dblp_trialX.csv");

        try {
            System.setOut(new PrintStream(outputFile));
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String str;
            String[] data;
            while((str = br.readLine())!=null){
                str = str.replace("15959.3423.5","15959.34");
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void join(){
        int colNum = 8;
        String inputFile1 = "data/huawei/dataset/songs/tableA.csv";
        String inputFile2 = "data/huawei/dataset/songs/tableB.csv";
        String labelFile = "data/huawei/dataset/songs/matches_msd_msd.csv";
        String outputFile = "data/huawei/dataset/songs/merged.csv";
        try {
            BufferedReader labelbr = new BufferedReader(new FileReader(labelFile));
            PrintStream ps = new PrintStream(outputFile);
            System.setOut(ps);
            BufferedReader br1 = new BufferedReader(new FileReader(inputFile1));
            BufferedReader br2 = new BufferedReader(new FileReader(inputFile2));
            String str1;
            String str2;
            String[] data1;
            String[] data2;
            while((str1 = br1.readLine())!=null){
                // 第二个文件所有的行都匹配下
                str2 = br2.readLine();
                data1 = str1.split(",",-1);
                data2 = str2.split(",",-1);
                for(int i = 0;i<colNum;i++){
                    System.out.print(data1[i]);
                    System.out.print(",");
                    System.out.print(data2[i]);
                    System.out.print(",");
                }
                String[] data = labelbr.readLine().split(",",-1);


            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Test
    public void combine(){
        int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7};
        int num = 6; // 上限num，表示每次从数字中选出1到num个数字

        List<List<Integer>> combinations = generateCombinations(numbers, num);

        // 打印所有组合
        for (List<Integer> combination : combinations) {
            System.out.println(combination);
        }
    }
    private static List<List<Integer>> generateCombinations(int[] numbers, int num) {
        List<List<Integer>> result = new ArrayList<>();
        generateCombinationsHelper(numbers, num, 0, new ArrayList<>(), result);
        return result;
    }

    private static void generateCombinationsHelper(int[] numbers, int num, int start, List<Integer> current, List<List<Integer>> result) {
        if (num == 0) {
            // 当达到选择的数字个数时，将当前组合加入结果集
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < numbers.length; i++) {
            current.add(numbers[i]);
            generateCombinationsHelper(numbers, num - 1, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}
