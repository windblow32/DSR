package crr;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.RandomTree.Tree;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Model {
    private int modelID;
    private final Instances instances;
    private Instances csvRecord;
    private RandomForest rf;

    public Model(Instances instances) {
        this.instances = instances;
    }

    public void setModelID(int id){
        this.modelID = id;
    }

    public int getModelID(){
        return this.modelID;
    }

    public Instances train() {
        try {
            // 在使用样本之前一定要首先设置instances的classIndex，否则在使用instances对象是会抛出异常
            rf = new RandomForest();
//            参数设置
            String[] options = weka.core.Utils.splitOptions("-K 0");
            rf.setOptions(options);
            rf.buildClassifier(instances);
            return instances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RandomForest getModel(){
        return rf;
    }

    public double predict(Instance input) {
        try {
            Evaluation testingEvaluation = new Evaluation(instances);
            // System.out.println(rf.classifyInstance(input));
            return testingEvaluation.evaluateModelOnceAndRecordPrediction(rf,
                    input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param input : 带估测的实例
     * @return 分类概率
     */
    public double getProbability(Instance input){
        try {
            return rf.classifyInstance(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Model) {
            Model model = (Model) obj;
            return model.rf == this.rf;
        }
        return false;
    }
}
