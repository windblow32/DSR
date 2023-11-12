package schemaMapping;

import sim_func.SimilarityFunction;

import java.util.HashSet;
import java.util.Set;

public class Name {
    String tokenizer = "3-gram";
    public void setTokenizer(String method){
        tokenizer = method;
    }
    /**
     * @return : use tokenizer to split attr
     */
    public Set<String> calcQset(String attributeName){
        Set<String> result = new HashSet<>();
        // 根据分词方法特制
        if(tokenizer.equals("3-gram")){
            // too short
            if(attributeName.length()<=3){
                result.add(attributeName);
                return result;
            }else{
                for(int i = 0;i< attributeName.length()-2;i++){
                    String token = attributeName.substring(i,i+3);
                    result.add(token);
                }
            }
        }
        return result;
    }

}
