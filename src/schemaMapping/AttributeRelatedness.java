package schemaMapping;

import sim_func.SimilarityFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttributeRelatedness {
    public String dataPath;
    private Set<String> leftWordSet;
    private Set<String> leftValueSet;
    private Set<String> rightWordSet;
    private Set<String> rightValueSet;
    private String leftAttr;
    private String rightAttr;

    /*
    放到主函数中
     */
    public double calcNameDistance() {
        Name name_measure = new Name();
        Set<String> leftSet = new HashSet<>();
        leftSet = name_measure.calcQset(leftAttr);
        Set<String> rightSet = new HashSet<>();
        rightSet = name_measure.calcQset(rightAttr);
        SimilarityFunction function = new SimilarityFunction();
        return 1 - function.jaccardForSet(leftSet, rightSet);
    }

    public double calcValueDistance() {
        Value leftValue = new Value(leftAttr, dataPath);
        Value rightValue = new Value(rightAttr, dataPath);
        Set<String> leftSet = leftValue.calcTset();
        Set<String> rightSet = rightValue.calcTset();
        // format计算需要分词后的属性值value set
        leftValueSet = leftValue.valueSet;
        rightValueSet = rightValue.valueSet;
        // embedding计算需要TF-IDF较低的word set
        leftWordSet = leftValue.lowTF_IDF_Set;
        rightWordSet = rightValue.lowTF_IDF_Set;
        SimilarityFunction function = new SimilarityFunction();
        return function.jaccardForSet(leftSet, rightSet);
    }

    public double calcFormatDistance() {
        Format leftFormat = new Format(leftValueSet);
        Format rightFormat = new Format(rightValueSet);
        Set<String> leftSet = leftFormat.calcRset();
        Set<String> rightSet = rightFormat.calcRset();
        SimilarityFunction function = new SimilarityFunction();
        return function.jaccardForSet(leftSet, rightSet);
    }

    public double calcEmbeddingDistance() {
        // 利用value中算出的low tf-idf set构造embedding
        Embedding left = new Embedding(leftWordSet);
        List<Double> leftAttrEmbedding = left.calcEmbedding();
        Embedding right = new Embedding(rightWordSet);
        List<Double> rightAttrEmbedding = right.calcEmbedding();
        // 计算两个大向量的embedding,cosine相似度
        SimilarityFunction function = new SimilarityFunction();
        return function.cosineForSet(leftAttrEmbedding, rightAttrEmbedding);
    }
    public double calcDomainDistance() {
        // 前提是提供的属性是numerical
        Domain domain = new Domain();
        // dim1 : maxDiv : 累计分布的差值 (最大值）
        // dim2 : pval : p值，皮尔森相关系数
        return domain.calcKS(leftValueSet, rightValueSet)[1];
    }
    public void judgeAttrType(){
        // fixme : 能否读取一行属性值，判断该属性为numerical或者text
        List<String> numericalAttrList = new ArrayList<>();
        List<String> textAttrList = new ArrayList<>();
        for(int i = 0;i<numericalAttrList.size();i++){
            for(int j = i + 1;j< numericalAttrList.size()-1;j++){
                calcNameDistance();
                calcFormatDistance();
                calcDomainDistance();
            }
        }
        for(int i = 0;i<textAttrList.size();i++){
            for(int j = i+1;j<textAttrList.size()-1;j++){
                calcNameDistance();
                calcValueDistance();
                calcFormatDistance();
                calcEmbeddingDistance();
            }
        }

    }
}
