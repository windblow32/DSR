package tools;

public class CheckString {
    /**
    检测字符串是否符合相似度函数比较标准，符合返回true，否则返回false
     包括字符串属性是否为空，是否为null；数值型属性是否为NaN等
     */
    public static boolean check(String s1, String s2){
        String str = s1;
        int flag = 1;
//        if(s1==null&&s2==null){
//            return true;
//        }
        if(str==null|| str.isEmpty()){
            flag = 0;
        }else{
            try{
                double num = Double.parseDouble(str);
                if (Double.isNaN(num)){
                    flag = 0;
                }
            }catch (NumberFormatException ignored){}
        }
        str = s2;
        if(str==null|| str.isEmpty()){
            flag = 0;
        }else{
            try{
                double num = Double.parseDouble(str);
                if (Double.isNaN(num)){
                    flag = 0;
                }
            }catch (NumberFormatException ignored){}
        }
        return flag == 1;
    }

    public static boolean isNumber(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        int dotCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c)) {
                if (c == '.') {
                    dotCount++;
                    if (dotCount > 1) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
