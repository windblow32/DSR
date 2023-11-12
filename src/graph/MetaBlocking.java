package graph;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crr.Model;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import graph.structure.*;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * https://github.com/Gaglia88/sparker
 * Generalized Supervised Meta-blocking
 */
public class MetaBlocking {
    private String dataPath = "";
    private Instances allInstances;

    // private Map<CSVRecord,Set<CSVRecord>> candidate = new HashMap<>();
    // private final Graph<CSVRecord> graph = new ConcreteEdgesGraph<>();
    private final Map<List<CSVRecord>,Integer> map = new HashMap<>();
    private double avg = 0;

    public void setDataPath(String path){
        this.dataPath = path;
    }


    public void meta(){
        List<CSVRecord> all_records = new ArrayList<>();
        all_records = readData(dataPath,2);
        List<Set<CSVRecord>> blocks = tokenBlocking(all_records);
        buildGraph(blocks, all_records);
        // candidate = createCandidate(blocks);
        // 简单版本，边权 < 0.5删除
        pruning();
        // System.out.println(graph.toString());

        // 实体对数据,
        File f = new File("data/computer/large/label data.csv");
        String[][] table = new String[1100][];
        String attr = "";
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String[] data;
            attr = br.readLine();
            int index = 0;
            while((str = br.readLine())!=null){
                data = str.split(",",-1);
                table[index] = data;
                index++;
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // block信息添加
        for(List<CSVRecord> r : map.keySet()){
            String e1 = r.get(0).get(0);
            String e2 = r.get(1).get(0);
            for(int i = 0;i<1100;i++){
                if((table[i][1].equals(e1)&&table[i][2].equals(e2))
                        ||(table[i][1].equals(e2)&&table[i][2].equals(e1))){
                    table[i][21] = String.valueOf(map.get(r));
                }
            }
        }

        // 输出文件
        File output = new File("data/computer/large/output.csv");
        try {
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
            System.out.println(attr);
            for(int i = 0;i<1100;i++){
                for(int j = 0;j<22;j++){
                    System.out.print(table[i][j]);
                    if(j != 21){
                        System.out.print(",");
                    }else {
                        System.out.println("");
                    }
                }
            }
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        File w = new File("data/computer/large/weight.csv");
//        try {
//            PrintStream ps = new PrintStream(w);
//            System.setOut(ps);
//            String leftID;
//            String rightID;
//            for(List<CSVRecord> r : map.keySet()){
//                leftID = r.get(0).get(0);
//                rightID = r.get(1).get(0);
//                String str = leftID + "," + rightID + "," + map.get(r);
//                System.out.println(str);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    private Map<CSVRecord,Set<CSVRecord>> createCandidate(List<Set<CSVRecord>> blocks, List<CSVRecord> all_records){
        Map<CSVRecord,Set<CSVRecord>> cand = new HashMap<>();
        for(CSVRecord record : all_records){
            Set<CSVRecord> set = new HashSet<>();
            for(Set<CSVRecord> block : blocks){
                int flag = 0;
                for(CSVRecord r : block){
                    // 如果当前block中有record，那么这个block中的实体都要加入record的代表集合中
                    if(r.get(0).equals(record.get(0))){
                        flag = 1;
                    }
                    if(flag == 1){
                        set.addAll(block);
                    }
                }
            }
            cand.put(record,set);
        }
        return null;
    }

    private void pruning(){
//        Set<CSVRecord> vexSet = graph.vertices();
//        for(CSVRecord r1 : vexSet){
//            Map<CSVRecord, Integer> map1 = graph.targets(r1);
//            Map<CSVRecord, Integer> map2 = graph.sources(r1);
//            for(CSVRecord r2  : map1.keySet()){
//                if(map1.get(r2) < avg){
//                    graph.set(r1,r2,0);
//                }
//            }
//            for(CSVRecord r2  : map2.keySet()){
//                if(map2.get(r2) < avg){
//                    graph.set(r1,r2,0);
//                }
//            }
//        }
        map.keySet().removeIf(list -> map.get(list) < avg);
    }

    private void buildGraph(List<Set<CSVRecord>> blocks, List<CSVRecord> all_records){
        int sumWeight = 0;
        int pairNum = 0;
        for(int i = 0;i<all_records.size() - 1;i++){
            for(int j = i + 1; j<all_records.size();j++){
                int weight = 0;
                // 两条记录同时出现的block数量
                for(Set<CSVRecord> block : blocks){
                    int flag1 = 0;
                    int flag2 = 0;
                    for(CSVRecord r : block){
                        // id在index = 0
                        if(r.get(0).equals(all_records.get(i).get(0))){
                            flag1 = 1;
                        }
                        if(r.get(0).equals(all_records.get(j).get(0))){
                            flag2 = 1;
                        }
                    }
                    if(flag1==1&&flag2==1){
                        weight++;
                    }
                }
                sumWeight += weight;
                if(weight > 0){
                    pairNum++;
                    // weight是common block数量
                    // graph.set(all_records.get(i),all_records.get(j),weight);
                    List<CSVRecord> list = new ArrayList<>();
                    list.add(all_records.get(i));
                    list.add(all_records.get(j));
                    map.put(list,weight);
                }
            }
        }
        avg = (double)sumWeight/pairNum;
        // return graph;
    }

    /**
     * 一种基本的分块方法.block中存放的是实体
     * block用Set表示
     * @param records : data as CSVRecord
     * @return : blocks
     */
    private List<Set<CSVRecord>> tokenBlocking(List<CSVRecord> records){
        // fixme : attrNum
        int attrNum = 10;
        Set<String> keyword = new HashSet<>();
        String[] data;
        for(CSVRecord record : records){
            for(int i = 0;i<attrNum;i++){
                data = record.get(i).split(" ",-1);
                for(String s : data){
                    if(s.length()>=3){
                        keyword.add(s);
                    }
                }
            }
        }
        List<Set<CSVRecord>> blockList = new ArrayList<>();
        for(String str : keyword){
            Set<CSVRecord> block = new HashSet<>();
            for(CSVRecord record : records){
                for(int i = 0;i<attrNum;i++){
                    if(record.get(i).contains(str)){
                        block.add(record);
                        break;
                    }

                }
            }
            if(block.size()>1){
                blockList.add(block);
            }
        }
        return blockList;
    }

    private List<CSVRecord> readData(String path, int classIndex) {
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File(path));
//            allInstances = loader.getDataSet();
//            allInstances.setClassIndex(classIndex);
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

}
