package Statistics;

public class ShortestWordCalc {
    public double shortestWordLen(String str) {
        if(str.isEmpty()){
            return 0.0;
        }
        return (double)findShortestWordLength(str);
    }
    public static int findShortestWordLength(String inputString) {
        String[] words = inputString.split("[\\s,;!.?]+"); // 使用正则表达式来分割单词

        int shortestLength = Integer.MAX_VALUE; // 初始化为一个较大的值

        for (String word : words) {
            int wordLength = word.length();
            if (wordLength < shortestLength) {
                shortestLength = wordLength;
            }
        }
        return shortestLength;
    }
}
