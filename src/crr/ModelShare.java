package crr;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import tools.ReadCsv;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.RandomTree.Tree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ModelShare {
    Comparator<Rule> comparable = Comparator.comparing(Rule::getIndex);
    /**
     * 存放规则的优先队列，按照index排序
     */
    PriorityQueue<Rule> priorityQueue = new PriorityQueue<>(comparable);
    Set<Model> modelSet = new HashSet<>();
    private double deltr;
    public double RouMax;

    private String[] attrName;
    // block数量
    private int blockNum = 0;
    // 累计误差
    private double totalError = 0;
    // 控制每个block中实体数
    private int numInBlock = 0;
    public String outputPath;
    public int classIndex;

    /**
     * 设置为每个block至少输出100个实体，便于计算F1 score时模型训练
     * @param dataPath
     */
    public void crr(String dataPath) {

        priorityQueue.add(new Rule(0, new HashSet<>()));
        // attention : data path
        // 全局数据
        Instances allData = readData(dataPath, classIndex);
        File f = new File(outputPath);
        try {
            PrintStream ps = new PrintStream(f);
            System.setOut(ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 每轮的数据，从allData中筛选
        while (!priorityQueue.isEmpty()) {
            Rule rule = priorityQueue.poll();
            // 用规则rule筛选数据集, 得到小规模数据dataset, 对应data
            Instances data = selectByRule(allData, rule);
            if(data.numInstances()<=1){
                continue;
            }
            int flag = 0;
            double shareRou = 0;
            Model sharedModel = new Model(data);
            for (Model model : modelSet) {
                //  如果error不能被容忍，flag = 1
                flag = 0;
                deltr = calcDeltr(data,model);
                for (Instance ins : data) {
                    // line 7
                    double rou = Math.abs(ins.value(classIndex) - (model.predict(ins) + deltr));
                    // RouMax此时未更新
                    if (rou >= RouMax) {
                        flag = 1;
                    }
                    if(rou>shareRou){
                        shareRou = rou;
                    }
                }
                if (flag == 0) {
                    sharedModel = model;
                    break;
                }
            }
            if (flag == 0 && !modelSet.isEmpty()) {
                // fixme : label
                rule.addConstraint(new Predicate("label", "=", deltr));
                totalError += shareRou;
                // save block
                if(numInBlock==0){
                    // first block
                    System.out.println("modelID : " + sharedModel.getModelID());
                }
                numInBlock++;
                if(numInBlock>100){
                    blockNum++;
                    numInBlock = 0;
                }
                // output data
                output(data);
                for (int i = 0; i < allData.numInstances(); i++) {
                    Instance instance1 = allData.instance(i);

                    // 在instances2中查找是否存在相同的元组
                    boolean found = false;
                    for (int j = 0; j < data.numInstances(); j++) {
                        Instance instance2 = data.instance(j);
                        if (equalInstance(instance1, instance2)) {
                            found = true;
                            break;
                        }
                    }
                    // 如果存在相同的元组，则从instances1中删除该Instance对象
                    if (found) {
                        allData.delete(i);
                        i--; // 由于删除了一个Instance对象，需要将i减1
                    }
                }
            } else {
                double index;
                double maxIndex = 0;
                for (Model model : modelSet) {
                    int sum = 0;
                    for (Instance ins : data) {
                        if (Math.abs(ins.value(classIndex) - (model.predict(ins) + deltr)) <= RouMax) {
                            sum++;
                        }
                    }
                    index = (double) sum / data.numInstances();
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                }
                rule.setIndex(maxIndex);
                // attention : rule
                // System.out.print("规则 : ");
                // System.out.println(rule);
                // train dataset
                Model newModel = new Model(data);
                newModel.train();
                // attention : 用new方法声明新的instance变量，否则相同地址会覆盖
                Instances badInstances = new Instances(data, 0);
                double maxError = 0;
                // calculate delta for new model
                deltr = calcDeltr(data, newModel);
                for (Instance ins : data) {
                    double cha = ins.value(classIndex) - newModel.predict(ins);
                    if (Math.abs(cha - deltr) > RouMax) {
                        // 对bad record提取谓词
                        badInstances.add(ins);
                    }
                    double error = Math.abs(cha);
                    if (error > maxError) {
                        maxError = error;
                    }
                }
                // attention : info maxError <= RouMax
                if ((totalError + maxError)<=RouMax*(blockNum + 1)) {
                    newModel.setModelID(blockNum);
                    modelSet.add(newModel);
                    totalError += maxError;

                    // save block
                    if(numInBlock==0){
                        // first block
                        System.out.println("modelID : " + sharedModel.getModelID());
                    }
                    numInBlock++;
                    if(numInBlock>100){
                        blockNum++;
                        numInBlock = 0;
                    }
                    // output data
                    output(data);

                    // 遍历instances1中的所有Instance对象
                    for (int i = 0; i < allData.numInstances(); i++) {
                        Instance instance1 = allData.instance(i);

                        // 在instances2中查找是否存在相同的元组
                        boolean found = false;
                        for (int j = 0; j < data.numInstances(); j++) {
                            Instance instance2 = data.instance(j);
                            if (equalInstance(instance1, instance2)) {
                                found = true;
                                break;
                            }
                        }
                        // 如果存在相同的元组，则从instances1中删除该Instance对象
                        if (found) {
                            allData.delete(i);
                            i--; // 由于删除了一个Instance对象，需要将i减1
                        }
                    }

                } else {
                    // train bad records using random tree
                    Set<Predicate> predSet = producePred(newModel.getModel());
                    for (Predicate pred : predSet) {
                        Rule r = new Rule(rule.getIndex(), addPred(rule, pred));
                        int count = 0;
                        for (Rule itor : priorityQueue) {
                            if (itor.getContext().equals(r.getContext())) {
                                count = 1;
                                itor.setIndex(r.getIndex());
                                break;
                            }
                        }
                        if (count == 0) {
                            priorityQueue.add(r);
                        }
                    }
                }
            }
        }

    }

    private void output(Instances data) {
        for (int i = 0; i < data.numInstances(); i++) {
            for (int j = 0; j < attrName.length; j++) {
                if (j != attrName.length - 1) {
                    System.out.print(data.get(i).value(j));
                    System.out.print(",");
                } else {
                    // 最后一列是标签
                    System.out.print((int)data.get(i).value(j));
                    System.out.println();
                }
            }
        }
    }

    private Set<Predicate> producePred(RandomForest rf) {
        Set<Predicate> predicateSet = new HashSet<>();
        int size = Math.min(rf.m_Classifiers.length, 100);
        // treeNum
        for (int i = 0; i < size; i++) {
            Tree mytree = ((RandomTree) rf.m_Classifiers[i]).m_Tree;
            Queue<Tree> queue = new LinkedBlockingQueue<>();
            queue.add(mytree);
            while (!queue.isEmpty()) {
                Tree first = queue.poll();
                int mark = first.m_Attribute;
                double domain = first.m_SplitPoint;
                Tree[] children = first.m_Successors;
                if (children != null && children.length > 0) {
                    // 只要分支节点，在这里添加到set中，如果保留叶子和分支，需要在if外面添加
                    predicateSet.add(new Predicate(attrName[mark], ">=", domain));
                    predicateSet.add(new Predicate(attrName[mark], "<", domain));
                    queue.addAll(Arrays.asList(children));
                    if(predicateSet.size()>50){
                        return predicateSet;
                    }
                }
            }
        }
        return predicateSet;
    }

    // fixme : 添加谓词
    public Set<Predicate> addPred(Rule rule, Predicate pred) {
        Set<Predicate> temp = rule.getContext();
        Set<Predicate> res = new HashSet<>(temp);
        res.add(pred);
        return res;
    }

    private double calcDeltr(Instances data, Model model){
        double max = 0;
        double min = 1;
        for (Instance ins : data) {
            // line 7
            double minus = Math.abs(ins.value(classIndex) - (model.predict(ins)));
            // RouMax此时未更新
            if(minus<min){
                min = minus;
            }
            if(minus>max){
                max = minus;
            }
        }
        return (min + max)/2;
    }

    private Instances readData(String path, int classIndex) {
        Instances allData = null;
        CSVLoader loader = new CSVLoader();
        try {
            loader.setSource(new File(path));
            allData = loader.getDataSet();
            allData.setClassIndex(classIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 不用返回了
        File f = new File(path);
        try {
            BufferedReader br = ReadCsv.getBr(path);
            // read attr;
            attrName = br.readLine().split(",", -1);
            // return CSVFormat.DEFAULT.parse(br).getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("read data error");
//        return null;
        return allData;
    }
    @Deprecated
    private List<CSVRecord> instancesToCsvRecords(Instances instances) {
        List<CSVRecord> csvRecords = new ArrayList<>();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(',');

        for (Instance instance : instances) {
            List<String> attributeValues = new ArrayList<>();
            for (int i = 0; i < instance.numAttributes(); i++) {
                String str;
                try{
                    str = instance.stringValue(i);
                }catch (IllegalArgumentException e2){
                    str = String.valueOf(instance.value(i));
                }
                attributeValues.add(str);
                // attributeValues.add(instance.attribute(i).value((int) instance.value(i)));
            }

            try {
                CSVParser parser = CSVParser.parse(valuesToCsvString(attributeValues), csvFormat);
                csvRecords.add(parser.getRecords().get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvRecords;
    }

    private static String valuesToCsvString(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value).append(",");
        }
        sb.deleteCharAt(sb.length() - 1); // 删除最后一个逗号
        return sb.toString();
    }

    /**
     * fixme : 利用rule筛选数据
     *
     * @param rule : 规则
     * @return 符合rule的数据集合
     */
    private Instances selectByRule(Instances allData, Rule rule) {
        Instances data = new Instances(allData, 0);
        // List<CSVRecord> records = instancesToCsvRecords(allData);
        // fixme : 先筛选，选完了生成规则，保证每次的值域不同
        Set<Predicate> predList = rule.getContext();
        if (predList.size() != 0) {
            for (Instance ins : allData) {
                // flag一直为0说明一直遵守rule，违反的不被存储。
                int flag = 0;
                for (Predicate pred : predList) {
                    // attr下标
                    int loc = 0;
                    for (String attr : attrName) {
                        // fixme : 只能针对数值型制定规则
                        if (attr.equals(pred.getFirst())) {
                            // 被约束的属性匹配
                            switch (pred.getSecond()) {
                                case "<=":
                                    if (!(ins.value(loc) <= pred.getThird())) {
                                        // 不满足约束
                                        flag = 1;
                                    }
                                    break;
                                case ">=":
                                    if (!(ins.value(loc) >= pred.getThird())) {
                                        // 不满足约束
                                        flag = 1;
                                    }
                                    break;
                                case ">":
                                    if (!(ins.value(loc) > pred.getThird())) {
                                        // 不满足约束
                                        flag = 1;
                                    }
                                    break;
                                case "<":
                                    if (!(ins.value(loc) < pred.getThird())) {
                                        // 不满足约束
                                        flag = 1;
                                    }
                                    break;
                                case "=":
                                    if (!(ins.value(loc) == pred.getThird())) {
                                        // 不满足约束
                                        flag = 1;
                                    }
                                    break;
                            }
                            break;
                        }
                        loc++;
                    }
                }
                if (flag == 0) {
                    // res.add(record);
                    data.add(ins);
                }
            }
        } else {
            // 无约束，全部读入
            // res = records;
            data = allData;
        }
        data.setClassIndex(classIndex);
        return data;
    }

    private boolean equalInstance(Instance ins1, Instance ins2){
        int numAttributes = ins1.numAttributes(); // 获取属性数量

        for (int i = 0; i < numAttributes; i++) {
            String str1;
            try{
                str1 = ins1.stringValue(i);
            }catch (IllegalArgumentException e3){
                str1 = String.valueOf(ins1.value(i));
            }
            String str2;
            try{
                str2 = ins2.stringValue(i);
            }catch (IllegalArgumentException e3){
                str2 = String.valueOf(ins2.value(i));
            }
            if(!str1.equals(str2)){
                return false;
            }
        }
        return true;
    }

}
