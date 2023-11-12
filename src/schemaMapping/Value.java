package schemaMapping;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

import static java.lang.System.exit;

public class Value {
    public int totalFreq;
    private Map<String, List<String>> tupleMap;
    private final String attrName;
    /**
     * wordSet存储每个单元格分词后的word
     */
    private Set<String> wordSet;
    private String dataPath;
    // use in Embedding
    public Set<String> lowTF_IDF_Set;
    /**
     * k is size of lowTF_IDF_Set
     */

    public int k;

    /**
     * 存储每个单元格的内容，后续计算Rset使用
     */
    public Set<String> valueSet;

    public Value(String attrName, String dataPath) {
        this.attrName = attrName;
        this.dataPath = dataPath;
    }

    /**
     * @description
     given an attribute value, we transform it into a set of
    informative tokens (tset, for short). By informative, we mean
    a notion akin to term-frequency/inverse-document-frequency
    (TF/IDF) from information retrieval
    首先对value分词，然后计算文档的非频繁项集，然后进行分词
    @param: currentDoc已经进行了分词处理,有重复，用list保存
     */
    private Map<String, Integer> calcFreqMap() {
        // 当前文档中每个词的词频
        Map<String, Integer> freqMap = new HashMap<>();
        int totalFreq = 0;
        for (String s : wordSet) {
            int freq = 0;
            if (!freqMap.containsKey(s)) {
                for (String t : wordSet) {
                    if (s.equals(t)) {
                        freq++;
                    }
                }
                totalFreq += freq;
                freqMap.put(s, freq);
            }
        }
        this.totalFreq = totalFreq;
        return freqMap;
    }

    private double calcTF(String word) {
        Map<String, Integer> freqMap = calcFreqMap();
        double wordFreq = freqMap.get(word);
        return wordFreq / totalFreq;
    }

    private void initTupleMap() {
        // init
        tupleMap.clear();
        wordSet.clear();
        // 把每个元组的属性A对应的属性值进行3-gram分词，然后储存在tupleMap中
        // read dataset, 第一行是属性名
        // fixme : set dataset path
        File dataset = new File(dataPath);
        try {
            FileReader fr = new FileReader(dataset);
            BufferedReader br = new BufferedReader(fr);
            String str;
            String[] data;
            // 用于存储attrName的索引
            int index = -1;
            // 读取属性行，获取该属性对应的索引
            str = br.readLine();
            data = str.split(",", -1);
            for (int i = 0; i < data.length; i++) {
                if (data[i].equals(attrName)) {
                    // i是attrName对应的索引
                    index = i;
                }
            }
            // 若未获取到索引，说明输入属性不在数据集中，报错
            if (index == -1) {
                System.out.println("未获取到索引，输入属性不在数据集中");
                exit(-1);
            }
            // 继续遍历属性值，获取下标为index的属性值，并分词，存储在map中
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(br);
            for (CSVRecord record : records) {
                // 获取单元格内容
                String value = record.get(index);
                // valueSet赋值
                valueSet.add(value);
                // 分词
                data = value.split(",");
                List<String> valueList = Arrays.asList(data);
                // 填充wordSet，后续计算Tset遍历用
                Collections.addAll(wordSet, data);
                // 存储在tupleList
                tupleMap.put(String.valueOf(record.getRecordNumber()), valueList);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    // 每个文件实际上是每个元组
    private double calcIDF(String word) {
        // 每个元组对该属性的属性值进行分词，存储为tupleMap<tuple,wordlist>
        Set<String> keySet = tupleMap.keySet();
        //  累计word在多少文件中出现过
        int wordFreq = 0;
        for (String str : keySet) {
            if (tupleMap.get(str).contains(word)) {
                wordFreq++;
            }
        }
        return Math.log((double) (tupleMap.size()) / wordFreq);
    }

    /**
    @description 给定单词计算TF-IDF
     */
    private double calcTF_IDF(String word) {
        return calcTF(word) * calcIDF(word);
    }

    /**
     @description 对当前属性attrName的所有属性值中的单词计算TF-IDF，
     寻找TF-IDF最大的一系列单词，越大表明越关键
     最终返回T，由外部处理Tset，计算相似度
     */
    public Set<String> calcTset() {
        // init tupleMap
        initTupleMap();
        // init T
        Set<String> T = new HashSet<>();
        double top = 0;
        Map<Double, String> scoreMap = new TreeMap<>(Comparator.naturalOrder());
        // 利用wordSet遍历计算TF-IDF,保留top
        for (String word : wordSet) {
            double score = calcTF_IDF(word);
            scoreMap.put(score, word);
            if (score > top) {
                T.clear();
                T.add(word);
            } else if (score == top) {
                T.add(word);
            }
        }
        calcLowTF_IDF_Set(scoreMap);
        return T;
    }

    private void calcLowTF_IDF_Set(Map<Double, String> scoreMap) {
        lowTF_IDF_Set.clear();
        Set<Double> scoreSet = scoreMap.keySet();
        int i = 0;
        for (double score : scoreSet) {
            if (i < k) {
                lowTF_IDF_Set.add(scoreMap.get(score));
            } else break;
            i++;
        }
    }
}
