package dataMining;

import java.util.*;

public class SSRM {
    public double rou = 0;
    public double xita = 0;
    public double sup = 0.9;

    public double run(Map<Double, Integer> data){
        // 默认升序排序
        Map<Double, Integer> sortedData = new TreeMap<>();
        Set<Double> keySet = data.keySet();
        double evi = 0;
        int obs = 0;
        for(double key : keySet){
            int value;
            if(data.get(key)>xita){
                evi++;
                value = 1;
            }else value = 0;
            obs++;
            sortedData.put(key, value);
        }
        int i = 1;
        Iterator iterator = sortedData.keySet().iterator();
        double key = -1;
        while(evi/obs<sup&& iterator.hasNext()){
            i++;
            obs--;
            key = (int)iterator.next();
            evi = evi - sortedData.get(key);
        }
        return key;
    }
}
