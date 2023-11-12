package autoEM;

import RuleMining.DPmining;
import dataMining.DSR;

import org.junit.Test;
import pre_experiment.StringStatics;
import tools.GenerateFeatureWithSim;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoFeatureEngineering {
    // 单表的属性个数
    public int attrNum = 3;
    public String datasetName = "Amazon-Google";




    /**
     * our method
     */
    @Test
    public void runSpark(){
        // 读取文件，构建相似度函数库
        // step1 : sim file name
        String simName = "AutoFE_CompareWithAutoEM_Test_" + datasetName;
        GenerateFeatureWithSim generate = new GenerateFeatureWithSim();
        // attention : dataset path
        generate.filePath = "data/huawei/dataset/"+datasetName+".csv";
        // label path
        String labelPath = "data/huawei/label/"+datasetName+"-label.csv";
        // 计算的特征值output path
        generate.outputPath = "data/huawei/autoFE/sim/unlabelFV_"+simName+".csv";
        generate.map_savePath = "data/huawei/map/"+datasetName+"-map.csv";
        List<Integer> list = new ArrayList<>();
        // add long text attr
        generate.longTextIndexList = list;
        List<Integer> numericalList = new ArrayList<>();

        generate.numericalIndexList = numericalList;
        // attention : 计算特征，函数内部包括了自动特征工程
        // attention : 利用历史信息构建规则库并储存，加载规则库，构建相似度函数库，然后生成特征向量
        // attention: 规则库使用离线规则库，存储到本地并加载为ruleList
        // todo:cold start rules,替换成现成的规则库
        DPmining dPmining = new DPmining();
        // 从离线存储的规则库中加载规则，定义规则存储方式
        List<DSR> ruleList = dPmining.loadRules("data/huawei/rules/sup=0.65,xita=0.65,k=5.txt");

        // 计算测试数据集的统计量信息,每个属性计算8种属性刻画
        StringStatics ss = new StringStatics();
        String filePath = generate.filePath;
        ss.calculateStatistic_Spark(filePath);

        // 属性刻画存储在ss对象中，将其转储为二维数组X方便查询，或者直接接入到相似度计算函数中
        double[][] X = new double[attrNum][8];
        for(int i = 0;i<attrNum;i++){
            // 8种属性刻画
            X[i][0] = ss.arrayLength[2*i];
            X[i][1] = ss.arrayEntropy[2*i];
            X[i][2] = ss.arrayIC[2*i];
            X[i][3] = ss.arrayAvg[2*i];
            X[i][4] = ss.arrayLength[2*i+1];
            X[i][5] = ss.arrayEntropy[2*i+1];
            X[i][6] = ss.arrayIC[2*i+1];
            X[i][7] = ss.arrayAvg[2*i+1];
        }

        // 并行化利用冷启动规则库和测试数据集属性刻画，推导属性上的相似度函数，并计算相似度作为特征值
        generate.generateFeatureUseRules_Spark(ruleList, X);
        String labeledSimPath = "data/huawei/autoFE/data/labeled/labeledFV_"+simName+".csv";
        // 添加标签,不需要第一行
        generate.addLabel(labelPath, labeledSimPath,false);
        System.out.println("特征工程已完成，请查看python模型效果");

    }

    /**
     * 复现AutoEM
     */
    @Test
    public void runAutoEM(){
        List<Integer> numerical_Index = new ArrayList<>();
        numerical_Index.add(-1);
//        numerical_Index.add(9);
        // 读取文件，构建相似度函数库
        // step1 : sim file name
        String simName = "AutoEMTest_" + datasetName;
        GenerateFeatureWithSim generate = new GenerateFeatureWithSim();
        // attention : dataset path
        generate.filePath = "data/huawei/dataset/"+datasetName+".csv";
        // label path
        String labelPath = "data/huawei/label/"+datasetName+"-label.csv";
        // 计算的特征值output path
        generate.outputPath = "data/huawei/autoFE/sim/unlabelFV_"+simName+".csv";
        List<Integer> list = new ArrayList<>();
        // add long text attr
        generate.longTextIndexList = list;
        List<Integer> numericalList = new ArrayList<>();
        generate.numericalIndexList = numericalList;

        // 并行化利用冷启动规则库和测试数据集属性刻画，推导属性上的相似度函数，并计算相似度作为特征值
        generate.generateAutoEM_FeatureUseSpark(numerical_Index);
        String labeledSimPath = "data/huawei/autoFE/data/labeled/labeledFV_"+simName+".csv";
        // 添加标签,不需要第一行
        generate.addLabel(labelPath, labeledSimPath,false);
        System.out.println("特征工程已完成，请查看python模型效果");

    }

    /**
     * model test
     */
    @Test
    public void testModel(){
        // use attr to predict match/unmatch
        CSVLoader loader = new CSVLoader();
        // attention : 加载计算的特征向量，设置CSV文件的路径
        try {
            loader.setSource(new File("/Users/ovoniko/Documents/GitHub/EntityMatching/data/huawei/autoFE/data/labeled/labeledFV_huaweiTest_dirty-DBLP-ACM.csv"));
//            loader.setNoHeaderRowPresent(true);
            Instances data = loader.getDataSet();
            // 设置要用于训练的列的索引（假设第1、2列作为特征，最后一列作为类别）
            int classIndex = data.numAttributes()-1;
            data.setClassIndex(classIndex);
            // attention : percentage
            double percentage = 0.7;
            int trainSize = (int)(0.7*data.numInstances());
            int testSize = data.numInstances() - trainSize;
            Instances trainData = new Instances(data,0,trainSize);
            Instances testData = new Instances(data,trainSize,testSize);

            // 创建随机森林分类器
            RandomForest rf = new RandomForest();
            // 参数设置
            String[] options= Utils.splitOptions("-K 0");
            rf.setOptions(options);
            // 训练模型
            rf.buildClassifier(trainData);
            Instance testInst;
            Evaluation testingEvaluation = new Evaluation(testData);
            int length = testData.numInstances();
            double num = 0;
            int tp = 0;
            int fn = 0;
            for (int i = 0; i < length; i++) {
                testInst = testData.instance(i);
                // 通过这个方法来用每个测试样本测试分类器的效果
                double predictValue = testingEvaluation.evaluateModelOnceAndRecordPrediction(rf,
                        testInst);
                System.out.println(testInst.classValue()+"--"+predictValue);
                if(testInst.classValue()>0.5){
                    // 对所有正样本判断
                    if(predictValue>=0.5){
                        tp++;
                    }else fn++;
                }
                if(Math.abs(testInst.classValue()-predictValue)>=0.5){
                    // 误差过大的累计
                    num++;
                }
                // num += Math.abs(testInst.classValue()-predictValue);
            }
            // System.out.println("分类器的正确率：" + (1 - testingEvaluation.errorRate()));
            double precision = 1 - (double)num/length;
            double recall = (double)tp/(tp+fn);
            double f1Score = 2*(precision*recall)/(precision + recall);
            System.out.print("分类器的正确率：");
            System.out.println(precision);
            System.out.print("分类器的召回率：");
            System.out.println(recall);
            System.out.print("分类器的F1 score：");
            System.out.println(f1Score);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    /**
     * 对比试验，Magellan没有自动特征工程，只依靠专家知识
     */
    public void runMegallan(){
        // 读取文件，构建相似度函数库
        // strp1 : sim file name
        String simName = "magellanTest_" + datasetName;
        GenerateFeatureWithSim generate = new GenerateFeatureWithSim();
        // attention : dataset path
        generate.filePath = "data/huawei/dataset/"+datasetName+".csv";
        // label path
        String labelPath = "data/huawei/label/"+datasetName+"-label.csv";
        // 计算的特征值output path
        generate.outputPath = "data/huawei/autoFE/sim/unlabelFV_"+simName+".csv";
        List<Integer> list = new ArrayList<>();
        // add long text attr
        generate.longTextIndexList = list;
        List<Integer> numericalList = new ArrayList<>();

        generate.numericalIndexList = numericalList;
        StringStatics ss = new StringStatics();
        String filePath = generate.filePath;
        ss.calculateStatistic_Spark(filePath);

        // 属性刻画存储在ss对象中，将其转储为二维数组X方便查询，或者直接接入到相似度计算函数中
        double[][] X = new double[attrNum][8];
        for(int i = 0;i<attrNum;i++){
            // 8种属性刻画
            X[i][0] = ss.arrayLength[2*i];
            X[i][1] = ss.arrayEntropy[2*i];
            X[i][2] = ss.arrayIC[2*i];
            X[i][3] = ss.arrayAvg[2*i];
            X[i][4] = ss.arrayLength[2*i+1];
            X[i][5] = ss.arrayEntropy[2*i+1];
            X[i][6] = ss.arrayIC[2*i+1];
            X[i][7] = ss.arrayAvg[2*i+1];
        }

        // 并行化利用冷启动规则库和测试数据集属性刻画，推导属性上的相似度函数，并计算相似度作为特征值
        generate.generateMagellanFeatureUseSpark(X);
        String labeledSimPath = "data/huawei/autoFE/data/labeled/labeledFV_"+simName+".csv";
        // 添加标签
        generate.addLabel(labelPath, labeledSimPath,false);
    }

}
