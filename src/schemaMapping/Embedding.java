package schemaMapping;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.neuralnetwork.NeuralNetworkType;
import sim_func.SimilarityFunction;

import java.util.*;

public class Embedding {
    private Set<String> lowTF_IDFset;
    private Word2VecModel model;
    public Embedding(Set<String> lowTF_IDFset){
        this.lowTF_IDFset = lowTF_IDFset;
    }
    private void train(){
        List<String> wordList = new ArrayList<>();
        wordList.addAll(lowTF_IDFset);
        try {
            model = Word2VecModel.trainer().setMinVocabFrequency(1).
                    useNumThreads(4).setWindowSize(1).
                    type(NeuralNetworkType.CBOW).setLayerSize(50).useNegativeSamples(5).
                    setDownSamplingRate(1.0E-4D).setNumIterations(5).
                    train(Collections.singleton(wordList));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private List<Double> getEmbedding(String word){
        Searcher searcher = model.forSearch();
        List<Double> vector = new ArrayList<>();
        try {
            vector = new ArrayList<>(searcher.getRawVector(word));
        } catch (Searcher.UnknownWordException e) {
            throw new RuntimeException(e);
        }
        return vector;
    }
    public List<Double> calcEmbedding(){
        List<List<Double>> embeddingList = new ArrayList<>();
        train();
        for(String word : lowTF_IDFset){
            embeddingList.add(getEmbedding(word));
        }
        double similarity = 0;
        Map<Double,List<Double>> embeddingMap = new TreeMap<>(Comparator.reverseOrder());
        // 按照字符串的jaccard相似度排序
        for(int i = 0;i<embeddingList.size();i++){
            for (int j = i + 1;j<embeddingList.size()-1;j++){
                // 计算i和j的字符串相似度
                List<Double> vec1 = embeddingList.get(i);
                List<Double> vec2 = embeddingList.get(j);
                String str1 = vec1.toString();
                String str2 = vec2.toString();
                List<Double> vec = new ArrayList<>(vec1);
                vec.addAll(vec2);
                SimilarityFunction func = new SimilarityFunction();
                similarity = func.jaccardForString(str1,str2);
                embeddingMap.put(similarity,vec);
            }
        }
        Set<Double> similaritySet = embeddingMap.keySet();
        List<Double> resultVec = new ArrayList<>();
        // 拼接
        for(double score : similaritySet){
            resultVec.addAll(embeddingMap.get(score));
        }
        return resultVec;

    }


}
