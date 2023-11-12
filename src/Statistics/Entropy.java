package Statistics;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Entropy {

    public double entropy(String str) {
        if(str.equals("")){
            return 0.0;
        }
        int[] xs = string2ascii(str);
        Entropy m = new Entropy();
        return m.entropy(xs);
    }

    double entropy(int[] xs) {
        if(xs.length==0){
            return 0;
        }
        //数组中的value作为map的key，map的value代表重复次数
        //(int)Math.ceil(log2(xs.length))只是优化，防止扩容带来的性能开销
        Map<Integer, Integer> map = new HashMap<>((int) Math.ceil(log2(xs.length)));

        for (int x : xs) {
            map.merge(x, 1, Integer::sum);
        }
        return calculate(map, xs.length);
    }

    public int[] string2ascii(String str) {
        int length = str.length();
        int[] sc = new int[length];
        for(int i = 0;i<length;i++){
            sc[i] = (int) str.charAt(i);
        }
        return sc;
    }

    public double calculate(Map<Integer, Integer> map, int total) {
        double res = 0;
        double p = 0;
        Collection<Integer> values = map.values();
        for (Integer times : values) {
            p = (times + 0.0) / total;
            res -= p * log2(p);
        }
        return res;
    }

    //log2(N)=loge(N)/loge(2),loge(N)代表以e为底的N的对数,loge(2)代表以e为底的2的对数
    public double log2(double N) {
        return Math.log(N) / Math.log(2);//Math.log的底为e
    }
}
