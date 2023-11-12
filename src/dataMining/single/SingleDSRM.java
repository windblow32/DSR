package dataMining.single;

import dataMining.DSR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class SingleDSRM {
    static double sup = 0.6;
    static double c = 0.6;
    static int M = 5;
    public int ssNum = 8;

    public long[][] naiveTime = new long[8][57];
    public long[][] prunedTime = new long[8][57];
    // fixme
    public int simFuncNum = 57;

    public List<DSR> mining(String xPath, String yPath) {
        List<DSR> ruleList = new ArrayList<>();
        for (int i = 0; i < ssNum; i++) {
            for (int j = 0; j < simFuncNum; j++) {
                List<double[]> ds = readData(xPath, yPath, i, j);
                double theta = readDataTheta(yPath, i, j);

                System.out.println();
                long startTime1 = System.currentTimeMillis();
                double result1 = naive(ds, theta);
                long endTime1 = System.currentTimeMillis();
                double dsMin = getMin(ds);
                double dsMax = getMax(ds);
                naiveTime[i][j] = endTime1 - startTime1;

                List<Integer> pruned = new ArrayList<>();
                long startTime2 = System.currentTimeMillis();
                double result2 = randomSampling(0, ds, sup, pruned, dsMin, dsMax, theta);
                long endTime2 = System.currentTimeMillis();
                prunedTime[i][j] = endTime2 - startTime2;
                DSR rule = new DSR();
                if (result1 != result2) {
                    List<Integer> list = new ArrayList<>();
                    list.add(i);
                    rule.indexList =list;
                    List<Double> ss = new ArrayList<>();
//                    ss.add(result1);
                    ss.add(result2);
                    rule.ss_ind = ss;
                    rule.yIndex = j;
                    ruleList.add(rule);
                }
            }
        }
        return ruleList;
    }

    /**
     *
     * @param xPath 属性刻画路径
     * @param yPath 加权F1 score路径
     * @param X_i 待挖掘的属性刻画下标
     * @param y_j 相似度函数
     * @return
     */
    public static List<double[]> readData(String xPath, String yPath, int X_i, int y_j) {
        List<double[]> data = new ArrayList<>();
        List<double[]> X = new ArrayList<>();
        List<double[]> y = new ArrayList<>();
        try {
            BufferedReader fx = new BufferedReader(new FileReader(xPath));
            BufferedReader fy = new BufferedReader(new FileReader(yPath));

            String line;
            while ((line = fx.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                X.add(row);
            }

            while ((line = fy.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                y.add(row);
            }

            fx.close();
            fy.close();

            for (int i = 0; i < X.size(); i++) {
                double[] rowData = new double[2];
                rowData[0] = X.get(i)[X_i];
                rowData[1] = y.get(i)[y_j];
                data.add(rowData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

//    public static double readDataTheta(int X_i, int y_j) {
//        List<double[]> y = new ArrayList<>();
//        try {
//            BufferedReader fy = new BufferedReader(new FileReader("D:\\try\\src\\y.csv"));
//
//            String line;
//            while ((line = fy.readLine()) != null) {
//                String[] values = line.split(",");
//                double[] row = new double[values.length];
//                for (int i = 0; i < values.length; i++) {
//                    row[i] = Double.parseDouble(values[i]);
//                }
//                y.add(row);
//            }
//
//            fy.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        List<Double> tempY = new ArrayList<>();
//        for (int i = 0; i < y.size(); i++) {
//            tempY.add(y.get(i)[y_j]);
//        }
//
//        int tempPSize = (int) (tempY.size() * c);
//        double[] tempP = new double[tempPSize];
//        for (int i = 0; i < tempPSize; i++) {
//            tempP[i] = tempY.get(tempY.size() - 1 - i);
//        }
//
//        return tempP[tempPSize - 1];
//    }

    public static double readDataTheta(String yPath, int X_i, int y_j) {
        List<double[]> y = new ArrayList<>();
        try {
            BufferedReader fy = new BufferedReader(new FileReader(yPath));

            String line;
            while ((line = fy.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                y.add(row);
            }

            fy.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Double> tempY = new ArrayList<>();
        for (int i = 0; i < y.size(); i++) {
            tempY.add(y.get(i)[y_j]);
        }

        int tempPSize = (int) (tempY.size() * c);
        double[] tempP = new double[tempPSize];
        PriorityQueue<Double> tempPQueue = new PriorityQueue<>((a, b) -> Double.compare(b, a));
        for (int i = 0; i < tempY.size(); i++) {
            tempPQueue.offer(tempY.get(i));
            if (tempPQueue.size() > tempPSize) {
                tempPQueue.poll();
            }
        }
        for (int i = tempPSize - 1; i >= 0; i--) {
            tempP[i] = tempPQueue.poll();
        }

//        System.out.println("test:" + tempP[tempPSize - 1]);
        return tempP[tempPSize - 1];
    }

//    public static double randomSampling(int iter, List<double[]> ds, double sup, List<Integer> pruned,
//                                        double dsMin, double dsMax, double theta) {
//        long time1 = System.currentTimeMillis();
//        Random rand = new Random();
//        int k = rand.nextInt(4) + 2; // Randomly choose k between 2 and 5
//        double[] p = new double[k + 1];
//
//        for (int i = 0; i <= k; i++) {
//            p[i] = ((dsMax - dsMin) / (k + 1)) * (i + 1) + dsMin;
//        }
//
//        List<double[]>[] P = new List[k + 1];
//        for (int i = 0; i <= k; i++) {
//            P[i] = new ArrayList<>();
//        }
//
//        for (int i = 0; i < ds.size(); i++) {
//            for (int j = 0; j < k; j++) {
//                if (ds.get(i)[0] < p[j]) {
//                    P[j].add(ds.get(i));
//                    break;
//                }
//                if (ds.get(i)[0] > p[k]) {
//                    P[k].add(ds.get(i));
//                    break;
//                }
//            }
//        }
//
//        double[] evi = new double[k + 1];
//        double[] obs = new double[k + 1];
//
//        for (int i = 0; i <= k; i++) {
//            for (int j = 0; j < P[i].size(); j++) {
//                obs[i]++;
//                if (P[i].get(j)[1] >= theta) {
//                    evi[i]++;
//                }
//            }
//        }
//
//        double[] S = new double[k];
//        for (int i = 0; i < k; i++) {
//            if (Arrays.stream(obs, i + 1, k + 1).sum() != 0) {
//                S[i] = Arrays.stream(evi, i + 1, k + 1).sum() / Arrays.stream(obs, i + 1, k + 1).sum();
//            }
//        }
//
//        double minP = -1;
//        List<double[]> dsNew = new ArrayList<>();
//        for (int i = 0; i < k; i++) {
//            if (S[i] > sup) {
//                minP = p[i];
//                int l = (int) (Arrays.stream(obs, i + 1, k + 1).sum() - Arrays.stream(evi, i + 1, k + 1).sum() / sup);
//                long time3 = System.currentTimeMillis();
//                double[][] tempP = P[i].stream().sorted((a, b) -> Double.compare(b[1], a[1]))
//                        .limit(l)
//                        .toArray(double[][]::new);
//                for (double[] temp : tempP) {
//                    pruned.add(ds.indexOf(temp));
//                    P[i].remove(temp);
//                }
//                dsNew.addAll(P[i]);
//                dsNew.addAll(Arrays.asList(tempP));
//                List<Integer> tempPrune = new ArrayList<>();
//                for (int temp = dsNew.size(); temp < ds.size(); temp++) {
//                    tempPrune.add(temp);
//                }
//                pruned.addAll(tempPrune);
//                for (int newIndex = i + 1; newIndex < P.length; newIndex++) {
//                    dsNew.addAll(P[newIndex]);
//                }
//                break;
//            } else {
//                int l = (int) ((sup * Arrays.stream(obs, i + 1, k + 1).sum()) - Arrays.stream(evi, i + 1, k + 1).sum() / (1 - sup));
//                double[][] tempP = P[i].stream()
//                        .sorted((a, b) -> Double.compare(b[1], a[1]))
//                        .limit(l)
//                        .toArray(double[][]::new);
//                for (double[] temp : tempP) {
//                    pruned.add(ds.indexOf(temp));
//                    P[i].remove(temp);
//                }
//                dsNew.addAll(P[i]);
//                dsNew.addAll(Arrays.asList(tempP));
//                int r = (int) (Arrays.stream(obs, i + 1, k + 1).sum() - Arrays.stream(evi, i + 1, k + 1).sum() / sup);
//                double[][] tempPR = P[i + 1].stream()
//                        .sorted((a, b) -> Double.compare(a[1], b[1]))
//                        .limit(r)
//                        .toArray(double[][]::new);
//                for (double[] temp : tempPR) {
//                    pruned.add(ds.indexOf(temp));
//                    P[i + 1].remove(temp);
//                }
//                dsNew.addAll(Arrays.asList(tempPR));
//                if (i + 1 == k) {
//                    dsNew.addAll(P[i + 1]);
//                }
//            }
//        }
//        pruned = new ArrayList<>(new HashSet<>(pruned));
//        long time2 = System.currentTimeMillis();
//        if (iter < M) {
//            iter++;
//            minP = randomSampling(iter, dsNew, sup, pruned, dsMin, dsMax, theta);
//        } else {
//            List<double[]> toSorted = new ArrayList<>();
//            List<double[]> dsFinal = new ArrayList<>();
//            double eviSum = 0;
//            double obsSum = 0;
//            Collections.sort(pruned);
//            for (int i = 0; i < dsNew.size(); i++) {
//                obsSum++;
//                if (dsNew.get(i)[1] > theta) {
//                    eviSum++;
//                }
//                if (!pruned.isEmpty() && i == pruned.get(0)) {
//                    pruned.remove(0);
//                    if (!toSorted.isEmpty()) {
//                        Collections.sort(toSorted, (a, b) -> Double.compare(a[0], b[0]));
//                        dsFinal.addAll(toSorted);
//                    }
//                    dsFinal.add(dsNew.get(i));
//                    toSorted = new ArrayList<>();
//                } else {
//                    toSorted.add(dsNew.get(i));
//                }
//            }
//            if (!toSorted.isEmpty()) {
//                Collections.sort(toSorted, (a, b) -> Double.compare(a[0], b[0]));
//                dsFinal.addAll(toSorted);
//            }
//            double eviTest = 0;
//            double obsTest = 0;
//            for (double[] dsDatum : ds) {
//                if (dsDatum[1] > theta) {
//                    eviTest++;
//                }
//                obsTest++;
//            }
//            System.out.println();
//            int i = 0;
//            while (i < dsFinal.size() && eviSum / obsSum < sup) {
//                obsSum--;
//                if (dsFinal.get(i)[1] > theta) {
//                    eviSum--;
//                }
//                i++;
//            }
//            if (i == dsFinal.size()) {
//                return -1;
//            } else {
//                return dsFinal.get(i)[0];
//            }
//        }
//        System.out.println(minP);
//        return minP;
//    }

    public static double randomSampling(int iter, List<double[]> ds, double sup, List<Integer> pruned,
                                        double dsMin, double dsMax, double theta) {
        long time1 = System.currentTimeMillis();
        int k = (int) Math.floor(Math.random() * 4 + 2);
        double[] p = new double[k];
        for (int i = 0; i < k; i++) {
            p[i] = (dsMax - dsMin) / (k + 1) * (i + 1) + dsMin;
        }

        List<double[]>[] P = new ArrayList[k + 1];
        for (int i = 0; i <= k; i++) {
            P[i] = new ArrayList<>();
        }

        for (double[] d : ds) {
            boolean added = false;
            for (int j = 0; j < k; j++) {
                if (d[0] < p[j]) {
                    P[j].add(d);
                    added = true;
                    break;
                }
                if (d[0] > p[p.length-1]) {
                    P[k].add(d);
                    break;
                }
            }

        }

        int[] evi = new int[k + 1];
        int[] obs = new int[k + 1];

        for (int i = 0; i <= k; i++) {
            for (double[] d : P[i]) {
                obs[i]++;
                if (d[1] >= theta) {
                    evi[i]++;
                }
            }
        }

        double[] S = new double[k];
        for (int i = 0; i < k; i++) {
            if (obs[i + 1] != 0) {
                S[i] = evi[i + 1] / obs[i + 1];
            }
        }

        double minP = -1;
        List<double[]> dsNew = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            if (S[i] > sup) {
                minP = p[i];
                int l = (int) (obs[i + 1] - evi[i + 1] / sup);
                long time3 = System.currentTimeMillis();
                List<double[]> tempP = new ArrayList<>(P[i]);
                Collections.sort(tempP, (d1, d2) -> Double.compare(d2[1], d1[1]));
                for (double[] temp : tempP) {
                    pruned.add(ds.indexOf(temp));
                    P[i].remove(temp);
                }
                dsNew.addAll(P[i]);
                dsNew.addAll(tempP);
                List<Integer> tempPrune = new ArrayList<>();
                for (int temp = dsNew.size(); temp < ds.size(); temp++) {
                    tempPrune.add(temp);
                }
                pruned.addAll(tempPrune);
                for (int newIndex = i + 1; newIndex < P.length; newIndex++) {
                    dsNew.addAll(P[newIndex]);
                }
                break;
            } else {
                int l = (int) ((int) ((sup * obs[i + 1]) - evi[i + 1]) / (1 - sup));
                List<double[]> tempP = new ArrayList<>(P[i]);
                Collections.sort(tempP, (d1, d2) -> Double.compare(d2[1], d1[1]));
                for (double[] temp : tempP) {
                    pruned.add(ds.indexOf(temp));
                    P[i].remove(temp);
                }
                dsNew.addAll(P[i]);
                dsNew.addAll(tempP);

                int r = (int) (obs[i + 1] - evi[i + 1] / sup);
                List<double[]> tempP2 = new ArrayList<>(P[i + 1]);
                Collections.sort(tempP2, (d1, d2) -> Double.compare(d1[1], d2[1]));
                for (double[] temp : tempP2) {
                    pruned.add(ds.indexOf(temp));
                    P[i + 1].remove(temp);
                }
                dsNew.addAll(tempP2);
                if (i + 1 == k) {
                    dsNew.addAll(P[i + 1]);
                }
            }
        }
        pruned = new ArrayList<>(new HashSet<>(pruned));
        long time2 = System.currentTimeMillis();
        if (iter < M) {
            iter++;
            try{
                minP = randomSampling(iter, dsNew, sup, pruned, dsMin, dsMax, theta);
            }catch (Exception e){
                int xscs=3;
            }

        } else {
            List<double[]> toSorted = new ArrayList<>();
            List<double[]> dsFinal = new ArrayList<>();
            int eviCount = 0;
            int obsCount = 0;
            Collections.sort(pruned);
            for (int i = 0; i < dsNew.size(); i++) {
                obsCount++;
                if (dsNew.get(i)[1] > theta) {
                    eviCount++;
                }
                if (!pruned.isEmpty() && i == pruned.get(0)) {
                    pruned.remove(0);
                    if (!toSorted.isEmpty()) {
                        Collections.sort(toSorted, (d1, d2) -> Double.compare(d1[0], d2[0]));
                        dsFinal.addAll(toSorted);
                    }
                    dsFinal.add(dsNew.get(i));
                    toSorted = new ArrayList<>();
                } else {
                    toSorted.add(dsNew.get(i));
                }
            }
            if (!toSorted.isEmpty()) {
                Collections.sort(toSorted, (d1, d2) -> Double.compare(d1[0], d2[0]));
                dsFinal.addAll(toSorted);
            }

            int eviTest = 0;
            int obsTest = ds.size();
            for (double[] d : ds) {
                if (d[1] > theta) {
                    eviTest++;
                }
            }

            System.out.println();
            int i = 0;
            while (i < dsFinal.size() && (double) evi[i] / obs[i] < sup) {
                obs[i] = obs[i] - 1;
                if (dsFinal.get(i)[1] > theta) {
                    evi[i] = evi[i] - 1;
                }
                i++;
            }
            if (i == dsFinal.size()) {
                return -1;
            } else {
                return dsFinal.get(i)[0];
            }
        }
        //System.out.println(minP);
        return minP;
    }


    public static double getMin(List<double[]> ds) {
        double min = Double.MAX_VALUE;
        for (double[] data : ds) {
            if (data[0] < min) {
                min = data[0];
            }
        }
        return min;
    }

    public static double getMax(List<double[]> ds) {
        double max = Double.MIN_VALUE;
        for (double[] data : ds) {
            if (data[0] > max) {
                max = data[0];
            }
        }
        return max;
    }


//    public static double naive(List<double[]> ds, double theta) {
//        double evi = 0;
//        double obs = 0;
//        for (double[] data : ds) {
//            if (data[1] > theta) {
//                evi++;
//            }
//        for (int i = 0; i < ds.size(); i++) {
//              double[] data = ds.get(i);
//              if (data[1] > theta) {
//                  evi++;
//              }
//            obs++;
//            double index = data[0];
//            double[] indexAll = data;
//            int low = 0;
//            int high = (int) obs - 1;
//            while (low <= high) {
//                int mid = (low + high) / 2;
//                if (ds.get(mid)[0] > index) {
//                    high = mid - 1;
//                } else {
//                    low = mid + 1;
//                }
//            }
//            for (int j = (int) obs; j > high + 1; j--) {
//                ds.add(j, ds.get(j - 1));
//            }
//            ds.add(high + 1, indexAll);
//        }
//        int i = 0;
//        while (i < ds.size() && evi / obs < sup) {
//            obs--;
//            if (ds.get(i)[1] > theta) {
//                evi--;
//            }
//            i++;
//        }
//        if (i == ds.size()) {
//            return -1;
//        } else {
//            return ds.get(i)[0];
//        }
//    }

//    public static double naive(List<double[]> ds, double theta) {
//        System.out.println("test");
//        double evi = 0;  // 初始化满足条件的数据数量为0
//        double obs = 0;  // 初始化观测到的数据总数量为0
//        for (double[] data : ds) {  // 遍历数据集中的每个数据
//            if (data[1] > theta) {  // 如果数据的第二个元素大于给定的阈值
//                evi++;  // 满足条件的数据数量加1
//            }
//            obs++;  // 观测到的数据总数量加1
//            double index = data[0];  // 获取数据的第一个元素
//            double[] indexAll = data;  // 将整个数据保存到一个新的数组中
//            int low = 0;  // 初始化最低索引为0
//            int high = (int) obs - 1;  // 初始化最高索引为观测到的数据总数量减1
//            while (low <= high) {  // 二分查找，找到数据应该插入的位置
//                int mid = (low + high) / 2;  // 计算中间索引
//                if (ds.get(mid)[0] > index) {  // 如果中间索引对应的数据的第一个元素大于当前数据的第一个元素
//                    high = mid - 1;  // 更新最高索引为中间索引减1
//                } else {
//                    low = mid + 1;  // 更新最低索引为中间索引加1
//                }
//            }
//            for (int j = (int) obs; j > high + 1; j--) {  // 将数据插入到正确的位置
//                System.out.println("j: " + j + ", ds.size(): " + ds.size());
//                ds.set(j, ds.get(j - 1));  // 将索引为j-1的数据赋值给索引为j的位置
//            }
//            ds.set(high + 1, indexAll);  // 将新数据插入到找到的位置
//        }
//        int i = 0;  // 初始化索引变量为0
//        while (i < ds.size() && evi / obs < sup) {  // 循环直到满足条件或遍历完数据集
//            obs--;  // 数据总数量减1
//            if (ds.get(i)[1] > theta) {  // 如果当前数据的第二个元素大于给定的阈值
//                evi--;  // 满足条件的数据数量减1
//            }
//            i++;  // 索引加1，继续遍历下一个数据
//        }
//        if (i == ds.size()) {  // 如果索引等于数据集的大小
//            return -1;  // 返回-1，表示没有满足条件的数据
//        } else {
//            return ds.get(i)[0];  // 返回满足条件的数据的第一个元素
//        }
//    }




    public static double naive(List<double[]> ds, double theta) {
        int evi = 0, obs = 0;
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i)[1] > theta) {
                evi += 1;
            }
            obs += 1;
            int low = 0;
            int high = i - 1;

            double index = ds.get(i)[0];
            double[] index_all = ds.get(i);
            while (low <= high) {
                int mid = (low + high) / 2;
                if (ds.get(mid)[0] > index) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }

            for (int j = i; j > high + 1; j--) {
                ds.set(j, ds.get(j - 1));
            }
            ds.set(high + 1, index_all);

        }
        int i = 0;
        while (i < ds.size() && (double) evi / obs < sup) {
            obs -= 1;
            if (ds.get(i)[1] > theta) {
                evi -= 1;
            }
            i += 1;
        }
        if (i == ds.size()) {
            return -1;
        } else {
            return ds.get(i)[0];
        }
    }



}