package Statistics;

public class AvgWordLenCalc {
    public double avgWordLen(String str) {
//        String text = "This is a sample sentence with different word lengths.";
        return calculateAverageWordLength(str);
    }

    public static double calculateAverageWordLength(String text) {
        if(text.isEmpty()){
            return 0.0;
        }
        // 使用正则表达式将文本拆分成单词（以空格为分隔符）
        String[] words = text.split("\\s+");

        if (words.length == 0) {
            return 0.0; // 文本为空，平均单词长度为0
        }

        int totalLength = 0;
        for (String word : words) {
            // 移除标点符号等非字母字符，然后计算单词长度
            word = word.replaceAll("[^a-zA-Z0-9]", "");
            totalLength += word.length();
        }

        // 计算平均单词长度
        return (double) totalLength / words.length;
    }
}
