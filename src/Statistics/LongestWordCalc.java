package Statistics;

public class LongestWordCalc {
    public double longestWordLen(String str) {
        if(str.isEmpty()){
            return 0.0;
        }
//        String input = "This is a sample string with different word lengths 1234567890.";
        return (int)findLongestWordLength(str);

    }

    public static int findLongestWordLength(String input) {
        // 将字符串分割成单词，使用空格作为分隔符
        String[] words = input.split("\\s+");

        int longestLength = 0;

        // 遍历所有单词，找到最长的一个
        for (String word : words) {
            // 移除非字母数字字符
            String cleanedWord = word.replaceAll("[^a-zA-Z0-9]", "");
            int wordLength = cleanedWord.length();

            if (wordLength > longestLength) {
                longestLength = wordLength;
            }
        }

        return longestLength;
    }
}
