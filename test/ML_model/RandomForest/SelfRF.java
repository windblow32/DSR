package ML_model.RandomForest;

import org.junit.Test;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.util.Random;

public class SelfRF {
    @Test
    public void testIris(){
        Instances iris = null;
        try {
            File file = new File("D:\\Tools\\Weka-3-8-6\\data\\iris.arff");
            ArffLoader loader = new ArffLoader();
            loader.setFile(file);
            iris = loader.getDataSet();
            // 在使用样本之前一定要首先设置instances的classIndex，否则在使用instances对象是会抛出异常
            iris.setClassIndex(iris.numAttributes() - 1);
            RandomForest rf = new RandomForest();
//            参数设置
            String[] options=weka.core.Utils.splitOptions("-K 0");
            rf.setOptions(options);
            rf.buildClassifier(iris);
            Instance testInst;
            Evaluation testingEvaluation = new Evaluation(iris);
            int length = iris.numInstances();
            for (int i = 0; i < length; i++) {
                testInst = iris.instance(i);
                // 通过这个方法来用每个测试样本测试分类器的效果
                double predictValue = testingEvaluation.evaluateModelOnceAndRecordPrediction(rf,
                        testInst);
                System.out.println(testInst.classValue()+"--"+predictValue);
            }
            System.out.println("分类器的正确率：" + (1 - testingEvaluation.errorRate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testBird(){
        Instances bird = null;
        try {
            // 直接用csv获取instance
            CSVLoader csv = new CSVLoader();
            csv.setSource(new File("data/modelshare test data/birdmap-samples.csv"));
            // 文件读取
//            File file = new File("data/modelshare test data/birdmap-samples.arff");
//            ArffLoader loader = new ArffLoader();
//            loader.setFile(file);
//            bird = loader.getDataSet();
            bird = csv.getDataSet();
            // 在使用样本之前一定要首先设置instances的classIndex，否则在使用instances对象是会抛出异常
            bird.setClassIndex(2);
            RandomForest rf = new RandomForest();
//            参数设置
            String[] options=weka.core.Utils.splitOptions("-K 0");
            rf.setOptions(options);
            rf.buildClassifier(bird);
            Instance testInst;
            Evaluation testingEvaluation = new Evaluation(bird);
            int length = bird.numInstances();
            int num = 0;
            for (int i = 0; i < length; i++) {
                testInst = bird.instance(i);
                // 通过这个方法来用每个测试样本测试分类器的效果
                double predictValue = testingEvaluation.evaluateModelOnceAndRecordPrediction(rf,
                        testInst);
                System.out.println(testInst.classValue()+"--"+predictValue);
                if(Math.abs(testInst.classValue()-predictValue)>0.5){
                    // 误差过大的累计
                    num++;
                }
            }
            // System.out.println("分类器的正确率：" + (1 - testingEvaluation.errorRate()));
            System.out.print("分类器的正确率：");
            System.out.println(1 - (double)num/length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testModel(){
        // use attr to predict match/unmatch
        CSVLoader loader = new CSVLoader();
        // attention : 设置CSV文件的路径
        try {
            loader.setSource(new File("E:\\GitHub\\EntityMatching\\data\\dblp-scholar\\chinese\\similarity\\all.csv"));
            // 读取数据实例
            Instances data = loader.getDataSet();
            data.randomize(new Random());
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
            String[] options=weka.core.Utils.splitOptions("-K 0");
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
}

