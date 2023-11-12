package sim_func;

import com.hankcs.hanlp.HanLP;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class WordBag implements Serializable {
    public List<String> words1;
    public List<String> words2;
    /**
     * ansj语法分词
     * @param str1
     * @param str2
     * @return
     */
    public void ToAnalysisTextSplit (String str1, String str2){
        words1 = HanLP.newSegment().enablePartOfSpeechTagging(true).seg(str1).stream().map(term -> term.word).collect(Collectors.toList());
        words2 = HanLP.newSegment().enablePartOfSpeechTagging(true).seg(str2).stream().map(term -> term.word).collect(Collectors.toList());
    }

    public void NlpAnalysisTextSplit (String str1, String str2){
        words1 = HanLP.newSegment().enableNameRecognize(true).enableOrganizationRecognize(true).seg(str1).stream().map(term -> term.word).collect(Collectors.toList());
        words2 = HanLP.newSegment().enableNameRecognize(true).enableOrganizationRecognize(true).seg(str2).stream().map(term -> term.word).collect(Collectors.toList());
    }

    public void IndexAnalysisTextSplit (String str1, String str2){
        words1 = HanLP.newSegment().enableIndexMode(true).enableAllNamedEntityRecognize(true).seg(str1).stream().map(term -> term.word).collect(Collectors.toList());
        words2 = HanLP.newSegment().enableIndexMode(true).enableAllNamedEntityRecognize(true).seg(str2).stream().map(term -> term.word).collect(Collectors.toList());
    }

    public void BaseAnalysisTextSplit (String str1, String str2){
        words1 = HanLP.newSegment().seg(str1).stream().map(term -> term.word).collect(Collectors.toList());
        words2 = HanLP.newSegment().seg(str2).stream().map(term -> term.word).collect(Collectors.toList());
    }




}
