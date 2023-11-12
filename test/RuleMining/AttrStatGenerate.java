package RuleMining;

import crr.ModelShare;
import org.junit.Test;
import pre_experiment.StringStatics;
import tools.GenerateFeatureWithSim;

import java.util.ArrayList;
import java.util.List;

public class AttrStatGenerate {
    // todo : 1.确定数据集属性数量，左右两个表的
    public int attrLength =8;
    public double RouMax = 0.005;
    // todo : 2.数据集名字
    public String dataName = "dblp-acm";
    // .人工输入，相似度函数名字
    public String simName = dataName + "10.10";
    // new dataset path
    public String unlabelPath = "data/huawei/dataset/"+dataName+".csv";
    // 在all.csv结尾加上标注数据
    public String labelPath = "data/huawei/label/"+dataName+"-label.csv";
    // 5输出路径final X path
    public String output = "data/huawei/output/"+simName+".csv";
    public String labeledSimPath = "data/huawei/DSRM/sim/labeled/label_"+simName+".csv";

    // 子集划分，计算属性刻画，计算Y(python)
    @Test
    public void generateTrialX(){
        // attention step1 : 对整个数据集应用全部simFunc，计算all.csv
        GenerateFeatureWithSim generate = new GenerateFeatureWithSim();
        generate.filePath = unlabelPath;
        generate.outputPath = "data/huawei/DSRM/sim/unlabeled/unlabel_"+simName+".csv";
        List<Integer> list = new ArrayList<>();
        generate.longTextIndexList = list;
        List<Integer> numericalList = new ArrayList<>();
        generate.numericalIndexList = numericalList;
        generate.calcSim();
        // label需要第一行，withheader=true
        generate.addLabel(labelPath, labeledSimPath,true);
        // attention step2: 对all.csv子集划分
        ModelShare modelShare = new ModelShare();
        // all.csv path
        String dataPath = labeledSimPath;
        // step5:modelID>100时调大表现阈值
        modelShare.RouMax = RouMax;
        // label index
        modelShare.classIndex = generate.lineLength;
        // result
        modelShare.outputPath = "data/huawei/block/"+dataName+"all,ROU="+modelShare.RouMax+"block10.10.csv";
        modelShare.crr(dataPath);

    }
    @Test
    public void generateSS(){
        // 路径和46行一样
        String subsets ="data/huawei/block/"+dataName+"all,ROU="+RouMax+"block10.10.csv";
        StringStatics stringStatics = new StringStatics();
        // 每个block的每个属性生成8种刻画，就是我们需要的X了
        stringStatics.statisticsFromSubset(subsets, labeledSimPath, output, unlabelPath,attrLength);
        // 想办法生成Y，Y在python的computerF1文件输出，注意需要调用file.flush()函数刷新缓冲区才能输出到文件
    }
}
