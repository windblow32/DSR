package tools;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ansj.splitWord.analysis.IndexAnalysis;
import sim_func.SimilarityFunction;
import sim_func.WordBag;

public class LoadModel {

    public static void main(String[] args) throws IOException {
        // 读取嵌入向量文件
        String embeddingFile = "E:\\GitHub\\pythonProject\\fasttext\\embedding_vectors.txt";
        Map<String, float[]> wordEmbeddings = readEmbeddings(embeddingFile);
        SimilarityFunction func = new SimilarityFunction();
        WordBag bag = new WordBag();
        // 查询单词的嵌入向量
        String word = "父亲购买了笔记本";
        String word2 = "爸爸买了电脑";
        bag.IndexAnalysisTextSplit(word,word2);
        float jiao = 0.0f;
        List<String> wordset = new ArrayList<>();
        for(String s1: bag.words1){
            wordset.add(s1);
            for(String s2 : bag.words2){
                float now = func.cosineForFloat(array2list(wordEmbeddings.get(s1)),array2list(wordEmbeddings.get(s2)));
                if(now>0.5){
                    jiao += now;
                }
                if(!wordset.contains(s2)){
                    wordset.add(s2);
                }
                System.out.println(s1);
                System.out.println(s2);
                System.out.println(now);
            }
        }
        System.out.println(jiao);
        System.out.println(wordset.size());
        System.out.println(jiao/wordset.size());

        System.out.println(func.simHashSimilarity(word,word2));


//        if (embedding != null) {
//            // 打印嵌入向量的维度和值
//            System.out.println("Dimension: " + embedding.length);
//            System.out.println("Dimension: " + embedding2.length);
//            System.out.print("Embedding: ");
//            for (double value : embedding) {
//                System.out.print(value + " ");
//            }
//            System.out.println();
//        } else {
//            System.out.println("Word not found in embeddings.");
//        }
    }

    public static void getDataEmbeddings(String embeddingFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(embeddingFile));
        String line;
        File output = new File("");
        PrintStream ps = new PrintStream(output);
        System.setOut(ps);
        List<String> wordList = new ArrayList<>();
        for(int source = 1;source<=5;source++){
            wordList.addAll(readData("E:\\GitHub\\KRAUSTD\\CTD\\data\\monitor0707\\source\\source"+source +".csv"));
        }
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            String word = parts[0];
            if(wordList.contains(word)){
                System.out.println(line);
            }
        }
        reader.close();
    }

    public static Map<String, float[]> readEmbeddings(String embeddingFile) throws IOException {
        Map<String, float[]> wordEmbeddings = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(embeddingFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            String word = parts[0];
            float[] embedding = new float[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                embedding[i - 1] = Float.parseFloat(parts[i]);
            }
            wordEmbeddings.put(word, embedding);
        }
        reader.close();
        return wordEmbeddings;
    }

    public static List<String> readData(String path){
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path),"GBK"));
            String str;
            List<String> words1 = new ArrayList<>();
            while((str = br.readLine())!=null) {
                words1.addAll(IndexAnalysis.parse(str).getTerms().stream().map(term -> term.getName()).collect(Collectors.toList()));
            }
            return words1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Float> array2list(float[] array){
        List<Float> list = new ArrayList<>();
        for(float num : array){
            list.add(num);
        }
        return list;
    }
}


