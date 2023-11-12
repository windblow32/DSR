package RuleMining;

import dataMining.DSR;
import dataMining.Point;
import dataMining.TrialMeta;
import dataMining.single.SingleDSRM;
import org.junit.Test;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DPmining {
    // todo：参数实验1
    public double xita = 0.6;
    public int allOBS = 0;
    public int allEVI = 0;
    // todo：参数2
    public double sup = 0.6;
    public List<TrialMeta> indexSortByX1;
    public List<TrialMeta> indexSortByX2;
    public PrintStream stdout = System.out;
    public String savePath = "data/huawei/rules/ruleDB.txt";

    /**
     * 制定属性刻画index
     *
     * @param xPath:属性刻画路径
     * @param yPath：F1          score路径
     * @param X1Index：第一个属性刻画下标
     * @param X2Index：第二个属性刻画下标
     * @param yIndex：相似度函数ID
     * @return
     */
    private List<TrialMeta> initTrialMeta(String xPath, String yPath, int X1Index, int X2Index, int yIndex) {

        File x = new File(xPath);
        File y = new File(yPath);
        List<TrialMeta> res = new ArrayList<>();
        try {
            BufferedReader brx = new BufferedReader(new FileReader(x));
            BufferedReader bry = new BufferedReader(new FileReader(y));
            String str;
            String[] data;
            int num = 0;
            while ((str = brx.readLine()) != null) {
                List<Double> statisticsList = new ArrayList<>();
                TrialMeta trialMeta = new TrialMeta();
                data = str.split(",", -1);
                for (String s : data) {
                    statisticsList.add(Double.parseDouble(s));
                }
                trialMeta.statistics = statisticsList;
                trialMeta.X1Index = X1Index;
                trialMeta.X2Index = X2Index;
                trialMeta.yIndex = yIndex;
                // Y
                str = bry.readLine();
                data = str.split(",", -1);
                List<Double> yList = new ArrayList<>();
                for (String s : data) {
                    yList.add(Double.parseDouble(s));
                }
                trialMeta.y = yList;
                // todo : y1 > xita, change Yj
                if (yList.get(yIndex) > xita) {
                    allOBS++;
                }
                trialMeta.index = num;
                res.add(trialMeta);
                num++;
            }
            allEVI = num;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 不指定属性刻画index
     *
     * @param xPath:属性刻画路径
     * @param yPath：F1       score路径
     * @param yIndex：相似度函数ID
     * @return
     */
    private List<TrialMeta> initTrialMeta(String xPath, String yPath, int yIndex) {

        File x = new File(xPath);
        File y = new File(yPath);
        List<TrialMeta> res = new ArrayList<>();
        try {
            BufferedReader brx = new BufferedReader(new FileReader(x));
            BufferedReader bry = new BufferedReader(new FileReader(y));
            String str;
            String[] data;
            int num = 0;
            while ((str = brx.readLine()) != null) {
                List<Double> statisticsList = new ArrayList<>();
                TrialMeta trialMeta = new TrialMeta();
                data = str.split(",", -1);
                for (String s : data) {
                    statisticsList.add(Double.parseDouble(s));
                }
                trialMeta.statistics = statisticsList;
                trialMeta.yIndex = yIndex;
                // Y
                str = bry.readLine();
                data = str.split(",", -1);
                List<Double> yList = new ArrayList<>();
                for (String s : data) {
                    yList.add(Double.parseDouble(s));
                }
                trialMeta.y = yList;
                // todo : y1 > xita, change Yj
                if (yList.get(yIndex) > xita) {
                    allOBS++;
                }
                trialMeta.index = num;
                res.add(trialMeta);
                num++;
            }
            allEVI = num;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 离线构建规则库
     *
     */
    @Test
    public void testDP() {
        // 数据集路径
        String xPath = "data/huawei/DSRM/trialX.csv";
        String yPath = "data/huawei/DSRM/trialY.csv";
        List<DSR> ruleList = new ArrayList<>();
        // todo：参数实验3 属性刻画数量（k）
        int assNum = 5;
        // 相似度函数数量
        int funcNum = 57;
        for(int k = assNum;k>0;k--){
            if (k > 1) {
                for (int i = 0; i < funcNum; i++) {
                    ruleList.addAll(maDSRM(xPath, yPath, i, k));
                    if(ruleList.size()>1){
                        int a = 32;
                    }
                }
            } else{
                // singleDSRM
                SingleDSRM singleDSRM = new SingleDSRM();
                ruleList.addAll(singleDSRM.mining(xPath, yPath));
            }
        }
        saveRules(ruleList, "data/huawei/rules/"+"sup="+sup+",xita="+xita+",k="+assNum+".txt");

    }


    /**
     * 多前件
     *
     * @param xPath:属性刻画路径
     * @param yPath：F1       score路径
     * @param yIndex：相似度函数ID
     * @param ssNum：前件个数
     * @return： 挖掘出的规则集合
     */
    public List<DSR> maDSRM(String xPath, String yPath, int yIndex, int ssNum) {
        List<DSR> ruleList = new ArrayList<>();

        // 给路径和yIndex进行标记和初始化
        List<TrialMeta> dataList = initTrialMeta(xPath, yPath, yIndex);
        // 直接暴力搜索：
        double max = 0;
        // 总属性刻画个数
        int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7};
        List<List<Integer>> combinations = generateCombinations(numbers, ssNum);

        if (ssNum == 2) {
            for (List<Integer> combination : combinations) {
                ruleList.addAll(mainDP(xPath, yPath, combination.get(0), combination.get(1), yIndex));
            }
            return ruleList;
        } else {

            for (List<Integer> combination : combinations) {
                // 标志是否找到best rules
                int evi = 0;
                int obs = 0;
                max = 0;
                TrialMeta best = new TrialMeta();
                for (TrialMeta d : dataList) {
                    // 对新点再次判断
                    double temp = 0;
                    // 如何映射k个属性刻画为坐标，然后判断高维空间中的关系
                    for (int index : combination) {
                        temp += Math.pow(d.statistics.get(index),2);
                    }
                    for (TrialMeta d2 : dataList) {
                        double temp2 = 0;
                        for (int index : combination) {
                            temp2 += Math.pow(d2.statistics.get(index),2);
                        }

                        if (temp2 > temp) {
                            obs++;
                            if (d2.getY() > xita) {
                                evi++;
                            }
                        }
                    }
                    double now_sup = (double) evi / obs;

                    if (!Double.isNaN(now_sup)&&now_sup >= sup) {
                        if (now_sup > max) {
                            max = now_sup;
                            // 只留一个
                            best = d;
                        }
                    }
                }
                if(best.statistics==null){
                    continue;
                }
                DSR rule = new DSR();
                rule.indexList = combination;
                List<Double> ss = new ArrayList<>();
                for(int index : combination){
                    ss.add(best.statistics.get(index));
                }
                rule.ss_ind = ss;
                rule.yIndex = yIndex;
                ruleList.add(rule);
            }
        }
        return ruleList;
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


    /**
     * 双前件
     * 只要bestrule
     * @param xPath 属性刻画前件
     * @param yPath F1 score
     * @param X1Index 属性刻画下标1
     * @param X2Index 属性刻画下标2
     * @param yIndex 相似度函数
     * @return 挖掘出的最优规则集合
     */
    public List<DSR> mainDP(String xPath, String yPath, int X1Index, int X2Index, int yIndex) {
        long startTime = System.currentTimeMillis();
        List<DSR> ruleList = new ArrayList<>();
        int success = 0;
        // 给路径和yIndex进行标记和初始化
        List<TrialMeta> dataList = initTrialMeta(xPath, yPath, X1Index, X2Index, yIndex);
        // 按照x1和x2分别排序,按照index映射到网格的i，j
        indexSortByX1 = new ArrayList<>(dataList);
        Collections.sort(indexSortByX1, Comparator.comparingDouble(TrialMeta::getX1));
        indexSortByX2 = new ArrayList<>(dataList);
        Collections.sort(indexSortByX2, Comparator.comparingDouble(TrialMeta::getX2));
        //通过状态转移方程建立递归算法，求解
        // 从0，0开始
        // todo : 如何调用动态规划算法？
        int size = indexSortByX1.size();
        // 初始化存储空间
        Point[][] pointSpace = new Point[size][size];
        int pn = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pointSpace[i][j] = new Point();
                pointSpace[i][j].obs = 0;
                pointSpace[i][j].evi = 0;
                if (indexSortByX1.get(i).getX1() == indexSortByX2.get(j).getX1()
                        && indexSortByX1.get(i).getX2() == indexSortByX2.get(j).getX2()) {
                    pointSpace[i][j].exist = 1;
                    pn++;
                } else pointSpace[i][j].exist = 0;
            }
        }
        double maxObs = Float.NEGATIVE_INFINITY;
        DSR bestRule = new DSR();
        // attention : 横坐标是i,纵坐标是j
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // fixme : init
                int obs = 0;
                int evi = 0;
                if (i == 0 && j == 0) {
                    pointSpace[0][0].obs = allOBS;
                    pointSpace[0][0].evi = allEVI;
                }
                // fixme : 如果i,j不存在，怎么处理到下一个
                if (i == 0 && j != 0) {
                    evi = j;
                    // attention : 查找所有index(X2)<j的点，求出obs and evi
                    // attention : 首先判断是否存在与当前下标i数值相同的点，因为列表排序中相同点的index不同，但是在计算obs和evi时需要考虑这些点
                    // 在X2中向上寻找index附近点，并记录点的信息，因为向下的肯定会参与obs计算
                    double nowX2 = indexSortByX2.get(i).getX2();
                    // attention : 保证不是最后一个
                    if (j < size - 1) {
                        for (int up = 0; up < size; up++) {
                            TrialMeta waitjudgeX2 = indexSortByX2.get(j + up);
                            if (waitjudgeX2.getX2() == nowX2) {
                                evi++;
                                if (waitjudgeX2.getY() > xita) {
                                    obs++;
                                }
                            } else {
                                // 没找到重叠点，直接break
                                break;
                            }
                        }
                    }

                    for (int k = 0; k < j; k++) {
                        if (indexSortByX2.get(k).getY() > xita) {
                            obs++;
                        }
                    }
                    pointSpace[i][j].obs = allOBS - obs;
                    pointSpace[i][j].evi = allEVI - evi;
                }
                if (j == 0 && i != 0) {
                    evi = i;
                    // attention : 查找所有index(X1)<i的点，求出obs and evi
                    // attention : 首先判断是否存在与当前下标i数值相同的点，因为列表排序中相同点的index不同，但是在计算obs和evi时需要考虑这些点
                    // 在X1中寻找index附近水平点，并记录点的信息
                    double nowX1 = indexSortByX1.get(i).getX1();
                    //保证不是最后一个，因为最后一个不需要判断，否则越界
                    if (i < size - 1) {
                        for (int right = 0; right < size; right++) {
                            TrialMeta waitjudgeX1 = indexSortByX1.get(i + right);
                            if (waitjudgeX1.getX1() == nowX1) {
                                // attention ：需要写在xita计算后面，保证evi包括了上述的obs
                                evi++;
                                if (waitjudgeX1.getY() > xita) {
                                    obs++;
                                }
                            } else {
                                // 没找到重叠点，直接break;
                                break;
                            }
                        }
                    }
                    for (int k = 0; k < i; k++) {
                        if (indexSortByX1.get(k).getY() > xita) {
                            obs++;
                        }
                    }
                    pointSpace[i][j].obs = allOBS - obs;
                    pointSpace[i][j].evi = allEVI - evi;
                } else if (i != 0 && j != 0) {
                    // 加减法重新定义,用对象pointSpace存储
                    // fixme : (i-1,j-1)是否满足
                    int sat = 0;
                    // 已知calcF(i-1,j-1)
                    if ((double) pointSpace[i - 1][j - 1].obs / pointSpace[i - 1][j - 1].evi >= sup) {
                        sat = 1;
                    }
                    // 判断点是否存在,想办法调用或计算需要的数值
                    pointSpace[i][j].obs = pointSpace[i - 1][j].obs + pointSpace[i][j - 1].obs - pointSpace[i - 1][j - 1].obs;
                    pointSpace[i][j].evi = pointSpace[i - 1][j].evi + pointSpace[i][j - 1].evi - pointSpace[i - 1][j - 1].evi;
                    Point p = pointSpace[i][j];

                    if (p.obs != 0 && p.evi != 0) {
                        // attention :判断点是否存在，就是对应的trialmeta是否是一个
                        if (p.exist == 1) {
                            if ((double) (p.obs + sat) / (p.evi + 1) >= sup) {

                                DSR rule = new DSR();
                                rule.X1 = indexSortByX1.get(i).getX1();
                                rule.X2 = indexSortByX2.get(j).getX2();
                                rule.X1Index = X1Index;
                                rule.X2Index = X2Index;
                                rule.yIndex = yIndex;
                                List<Integer> indlist = new ArrayList<>();
                                indlist.add(X1Index);
                                indlist.add(X2Index);
                                // 指定rule中的属性刻画下标
                                rule.indexList = indlist;
                                List<Double> sslist = new ArrayList<>();
                                sslist.add(indexSortByX1.get(i).getX1());
                                sslist.add(indexSortByX2.get(j).getX2());
                                // 指定属性刻画中的统计量数值
                                rule.ss_ind = sslist;
                                int chongfu = 0;
                                if (!ruleList.isEmpty()) {
                                    for (DSR r : ruleList) {
                                        if (r.X1 == rule.X1 && r.X2 == rule.X2) {
                                            chongfu = 1;
                                            break;
                                        }
                                    }
                                    if (chongfu == 0) {
                                        // 没有重复rule，则添加
                                        success++;
                                        //ruleList.add(rule);
                                        // 此外，记录最佳rule
                                        if ((float) p.obs > maxObs) {
                                            maxObs = p.obs;
                                            bestRule = rule;
                                        }
//                                        System.out.print("rule " + success + ": ");
//                                        System.out.println("X1 is " + rule.X1 + ", X2 is " + rule.X2);
                                    }
                                } else {
                                    success++;
                                    ruleList.add(rule);
                                    if ((float) p.obs > maxObs) {
                                        maxObs = p.obs;
                                        bestRule = rule;
                                    }
//                                    System.out.print("rule " + success + ": ");
//                                    System.out.println("X1 is " + rule.X1 + ", X2 is " + rule.X2);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("bestRule : " + "X1 is " + bestRule.X1 + ", X2 is " + bestRule.X2);
        // 执行需要统计时间的代码
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
//        System.out.println("Elapsed time: " + elapsedTime + " milliseconds");
//        System.out.println(success + " rules");
        // 最后统一储存
        ruleList.clear();
        ruleList.add(bestRule);
        return ruleList;

    }

    public List<DSR> loadRules(String loadPath) {
        List<DSR> ruleList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(loadPath));
            String str;
            String[] data;
            while ((str = br.readLine()) != null) {
                data = str.split(",", -1);
                DSR rule = new DSR();
                int size = Integer.parseInt(data[0]);
                List<Integer> indexList = new ArrayList<>();
                for(int k = 0;k<size;k++){
                    // 第一个是size，后面就是index
                    indexList.add(Integer.parseInt(data[k+1]));
                }
                rule.indexList = indexList;
                List<Double> ssList = new ArrayList<>();
                for(int k = 0;k<size;k++){
                    // 后面是属性刻画数值，与indexList一一对应
                    ssList.add(Double.parseDouble(data[k+1+size]));
                }
                rule.ss_ind = ssList;
                rule.yIndex = Integer.parseInt(data[size+size+1]);
                ruleList.add(rule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ruleList;
    }

    public void saveRules(List<DSR> ruleList, String savePath) {
        File ruleDB = new File(savePath);
        try {
            PrintStream ps = new PrintStream(ruleDB);
            System.setOut(ps);
            for (DSR r : ruleList) {
                // check rules，先判断是否为空，在判断元素为空，否则空指针异常
                if(r.ss_ind==null||r.indexList==null||r.ss_ind.isEmpty()||r.indexList.isEmpty()){
                    continue;
                }
                int flag = 1;
                for(double num : r.ss_ind){
                    if (num <= 0) {
                        flag = 0;
                        break;
                    }
                }
                if(flag == 0){
                    continue;
                }
                // 每一行是一个规则，读取时用逗号分割
                int size = r.indexList.size();
                System.out.print(size);
                System.out.print(",");
                for(int index : r.indexList){
                    System.out.print(index);
                    System.out.print(",");
                }
                for(double ss : r.ss_ind){
                    System.out.print(ss);
                    System.out.print(",");
                }
                System.out.println(r.yIndex);
            }
            System.setOut(stdout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void mergeRule(String sourceFolderPath, String targetFilePath) {


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
