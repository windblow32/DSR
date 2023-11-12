package schemaMapping;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Format {
    private Set<String> valueSet;

    /**
     * 从Value类生成的valueSet构造Format类
     * @param valueSet:Value类生成的全局变量，存储分词后的value
     */
    public Format(Set<String> valueSet){
        this.valueSet = valueSet;
    }

    /**
     * @description test refer to Regex
     * @return R set
     */
    public Set<String> calcRset(){
        // init R set
        Set<String> Rset = new HashSet<>();
        StringBuilder output = new StringBuilder();
        String C = "[A-Z][a-z]+";
        String U = "[A-Z]+";
        String L = "[a-z]+";
        String N = "[0-9]+";
        String A = "[A-Za-z0-9]+";
        String P = "[.,;:/_ ]+";
        String pattern = C + "|"
                + U + "|"
                + L + "|"
                + N + "|"
                + A + "|"
                + P;
        Pattern strPattern = Pattern.compile(pattern);
        for(String str : valueSet){
            Matcher strMatcher = strPattern.matcher(str);
            while (strMatcher.find()) {
                System.out.println(strMatcher.group());
                if (strMatcher.group().matches(C)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("C")){
                        output.append("+");
                    }else {
                        output.append("C");
                    }
                } else if (strMatcher.group().matches(U)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("U")){
                        output.append("+");
                    }else {
                        output.append("U");
                    }
                } else if (strMatcher.group().matches(L)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("L")){
                        output.append("+");
                    }else {
                        output.append("L");
                    }
                } else if (strMatcher.group().matches(N)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("N")){
                        output.append("+");
                    }else {
                        output.append("N");
                    }
                } else if (strMatcher.group().matches(A)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("A")){
                        output.append("+");
                    }else {
                        output.append("A");
                    }
                } else if (strMatcher.group().matches(P)) {
                    if(output.length()>0&&output.substring(output.length()-1).equals("P")){
                        output.append("+");
                    }else {
                        output.append("P");
                    }
                }
            }
            // 一个str元素对应一个output，是Rset中的元素
            Rset.add(output.toString());
        }
        return Rset;
    }

}
