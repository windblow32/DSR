package sim_func;

import java.io.Serializable;
import java.util.*;

public class MinHashSimilarity implements Serializable {
    int hashFuncNum = 10;
    public List<String> sig1;
    public List<String> sig2;
    public MinHashSimilarity() {
    }

    public void calculateSimilarity(String s1, String s2) {
        Set<String> set1 = toSet(s1);
        Set<String> set2 = toSet(s2);
        sig1 = toSignature(set1);
        sig2 = toSignature(set2);
    }

    private Set<String> toSet(String s) {
        String[] words = s.split("\\s+");
        Set<String> set = new HashSet<>();
        for (String word : words) {
            set.add(word);
        }
        return set;
    }

    private List<String> toSignature(Set<String> set) {
        // 5
        List<String> result = new ArrayList<>();
        int[] sig = new int[10];
        Arrays.fill(sig, Integer.MAX_VALUE);
        for (String s : set) {
            sig[0] = Math.min(sig[0], HashUtil.gbkHash(s));
            sig[1] = Math.min(sig[1], HashUtil.apHash(s));
            sig[2] = Math.min(sig[2], HashUtil.dekHash(s));
            sig[3] = Math.min(sig[3], HashUtil.djbHash(s));
            sig[4] = Math.min(sig[4], HashUtil.fnvHash(s));
            sig[5] = Math.min(sig[5], HashUtil.bkdrHash(s));
            sig[6] = Math.min(sig[6], HashUtil.elfHash(s));
            sig[7] = Math.min(sig[7], HashUtil.jsHash(s));
            sig[8] = Math.min(sig[8], HashUtil.rsHash(s));
            sig[9] = Math.min(sig[9], HashUtil.javaDefaultHash(s));
        }
        for(int v : sig){
            result.add(String.valueOf(v));
        }
        return result;
    }


    private double jaccardSimilarity(int[] sig1, int[] sig2) {
        int intersection = 0;
        int union = 0;
        for (int i = 0; i < 10; i++) {
            if (sig1[i] == sig2[i]) {
                intersection++;
            }
            union++;
        }
        return (double) intersection / union;
    }

}

