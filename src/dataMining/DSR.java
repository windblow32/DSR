package dataMining;

import java.util.ArrayList;
import java.util.List;

/**
 * X1Index : 第一个属性刻画的下标
 * X1 : 属性刻画取值
 * yIndex : 相似度函数的下标（一共14种）
 * indexList: 属性刻画的下标
 * ss_ind: 对应indexList的属性刻画取值列表
 */
public class DSR {
    public int X1Index;
    public int X2Index;
    public int yIndex;
    public double X1;
    public double X2;
    public List<Integer> indexList = new ArrayList<>();
    // 和indexList下标对应的ss列表
    public List<Double> ss_ind = new ArrayList<>();
    public int exist;
}
