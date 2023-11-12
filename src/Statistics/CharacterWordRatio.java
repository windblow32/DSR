package Statistics;

import java.util.Arrays;

public class CharacterWordRatio {
    public double characterRatio(String input) {
        if(input.isEmpty()){
            return 0.0;
        }
        // 统计不同字符的数量
        int distinctCharacters = countDistinctCharacters(input);

        // 计算比例
        return (double) distinctCharacters / input.length() * 100;
    }

    public double wordRatio(String input) {
        // 统计不同单词的数量
        int distinctWords = countDistinctWords(input);

        // 计算比例
        return (double) distinctWords / countWords(input) * 100;
    }

    // 统计不同字符的数量
    private static int countDistinctCharacters(String input) {
        return (int) input.chars().distinct().count();
    }

    // 统计不同单词的数量
    private static int countDistinctWords(String input) {
        String[] words = input.split("\\s+");
        return (int) Arrays.stream(words).distinct().count();
    }

    // 统计总单词数
    private static int countWords(String input) {
        String[] words = input.split("\\s+");
        return words.length;
    }
}
