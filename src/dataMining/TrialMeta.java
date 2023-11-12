package dataMining;

import java.io.*;
import java.util.List;

public class TrialMeta {

    public List<Double> y;
    public List<Double> statistics;
    public int index;
    public int X1Index;
    public int X2Index;
    public int yIndex;
    public double getX1(){
        return statistics.get(X1Index);
    }
    public double getX2(){
        return statistics.get(X2Index);
    }
    public double getY(){
        return y.get(yIndex);
    }
    public double getY(int n){
        return y.get(n);
    }
    public double getSS(int n){
        return statistics.get(n);
    }
}
