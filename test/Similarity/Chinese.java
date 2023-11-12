package Similarity;

import org.junit.Test;
import sim_func.SimilarityFunction;
import sim_func.WordBag;

import java.util.HashSet;
import java.util.Set;

public class Chinese {
    @Test
    public void test(){
        SimilarityFunction si = new SimilarityFunction();
        try {
            WordBag bag  = new WordBag();
            bag.IndexAnalysisTextSplit("let us travel", "路由去");
            // double sim = si.cosineUsingWordBag(bag.words1, bag.words2);
            Set<String> set1 = new HashSet<>(bag.words1);
            Set<String> set2 = new HashSet<>(bag.words2);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String text = "按照语法对中文字符串分词";
//        List<Term> termList = HanLP.segment(text);
//        System.out.println(termList);
    }



}
