package sim_func;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class SimHash {
    private String text;
    public BigInteger hash;

    public SimHash(String text) {
        this.text = text;
        this.hash = this.simHash();
    }

    public BigInteger simHash() {
        int[] bits = new int[64];
        String[] words = this.text.split("\\s+");
        for (String word : words) {
            BigInteger hash = BigInteger.valueOf(HashUtil.gbkHash(word));
            for (int i = 0; i < 64; i++) {
                BigInteger bitMask = BigInteger.ONE.shiftLeft(i);
                if (hash.and(bitMask).signum() != 0) {
                    bits[i]++;
                } else {
                    bits[i]--;
                }
            }
        }
        BigInteger simHash = BigInteger.ZERO;
        for (int i = 0; i < 64; i++) {
            if (bits[i] > 0) {
                simHash = simHash.add(BigInteger.ONE.shiftLeft(i));
            }
        }
        return simHash;
    }


}
