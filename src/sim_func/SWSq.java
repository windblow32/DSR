package sim_func;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;


public class SWSq {
    private int[][] H;
    private int[][] isEmpty;
    private static int SPACE ;                      //空格匹配的得分 
    private static int MATCH ;                       //两个字母相同的得分 
    private static int DISMACH;                    //两个字母不同的得分 
    private int maxIndexM, maxIndexN;

    private Stack<Character> stk1, stk2;

    public String subSq1, subSq2;                        //相似度最高的两个子串 

    public SWSq(){
        stk1 = new Stack<Character>();
        stk2 = new Stack<Character>();
        SPACE = -4;
        MATCH = 3;
        DISMACH = -1;
    }
    private int max(int a, int b, int c){
        int maxN;
        if(a >= b)
            maxN = a;
        else
            maxN = b;
        if(maxN < c)
            maxN = c;
        if(maxN < 0)
            maxN = 0;
        return maxN;
    }

    private void calculateMatrix(String s1, String s2, int m, int n){//计算得分矩阵 

        if(m == 0)
            H[m][n] = 0;
        else if(n == 0)
            H[m][n] = 0;
        else{
            if(isEmpty[m - 1][n - 1] == 1)
                calculateMatrix(s1, s2, m-1, n-1);
            if(isEmpty[m][n - 1] == 1)
                calculateMatrix(s1, s2, m, n-1);
            if(isEmpty[m - 1][n] == 1)
                calculateMatrix(s1, s2, m-1, n);
            if(s1.charAt(m-1) == s2.charAt(n-1))
                H[m][n] = max(H[m - 1][n - 1] + MATCH, H[m][n - 1] + SPACE, H[m - 1][n] + SPACE);
            else
                H[m][n] = max(H[m - 1][n - 1] + DISMACH, H[m][n - 1] + SPACE, H[m - 1][n] + SPACE);
        }
        isEmpty[m][n] = 0;
    }

    private void findMaxIndex(int[][] H, int m, int n){//找到得分矩阵H中得分最高的元组的下标 
        int curM, curN, i, j, max;
        curM = 0;
        curN = 0;
        max = H[0][0];
        for(i = 0; i < m; i++)
            for(j = 0; j < n; j++)
                if(H[i][j] > max){
                    max = H[i][j];
                    curM = i;
                    curN = j;
                }
        maxIndexM = curM;
        maxIndexN = curN;
    }
    private void traceBack(String s1, String s2, int m, int n){//回溯 寻找最相似子序列 
        if(H[m][n] == 0)
            return;
        if(H[m][n] == H[m-1][n] + SPACE) {
            stk1.add(s1.charAt(m-1));
            stk2.add('-');
            traceBack(s1, s2, m - 1, n);
        }
        else if(H[m][n] == H[m][n-1] + SPACE) {
            stk1.add('-');
            stk2.add(s2.charAt(n-1));
            traceBack(s1, s2, m, n - 1);
        }
        else {
            stk1.push(s1.charAt(m - 1));
            stk2.push(s2.charAt(n-1));
            traceBack(s1, s2, m - 1, n - 1);
        }
    }

    public String ALtoString(ArrayList<Character> A) {
        StringBuilder sb = new StringBuilder();
        for (Character a : A) {
            sb.append(a.toString());
        }
        return sb.toString();
    }

    public void find(String s1, String s2){
        //initMatrix(s1.length(), s2.length()); 
        int i, j;
        H = new int[s1.length() + 1][s2.length() + 1];
        isEmpty = new int[s1.length() + 1][s2.length() + 1];
        for(i = 0; i<=s1.length(); i++)
            for(j = 0; j<=s2.length(); j++)
                isEmpty[i][j] = 1;
        calculateMatrix(s1, s2, s1.length(), s2.length());
        findMaxIndex(H, H.length, H[0].length);
        traceBack(s1, s2, maxIndexM, maxIndexN);
        ArrayList<Character> arr1 = new ArrayList<>();
        ArrayList<Character> arr2 = new ArrayList<>();
        while(!stk1.empty())
            arr1.add(stk1.pop());
        subSq1 = ALtoString(arr1);
        while(!stk2.empty())
            arr2.add(stk2.pop());
        subSq2 = ALtoString(arr2);
    }

    public double smith_waterman(String s1, String s2) {
        SWSq x = new SWSq();
        x.find(s1, s2);
       return Math.max((double)x.subSq1.length()/s1.length(), (double)x.subSq2.length()/s2.length());

    }
} 