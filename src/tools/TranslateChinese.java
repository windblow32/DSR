package tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TranslateChinese {
    private static final String YOUDAO_URL = "https://openapi.youdao.com/api";
    private static final String APP_KEY = "50167b910e4b7c60";
    private static final String APP_SECRET = "bUvgiPFica8xzdsCzzEGzM3uDncoIllU";

    public String trans(String query) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String salt = String.valueOf(System.currentTimeMillis());
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", "EN");
        params.put("to", "zh-CHS");
        params.put("appKey", APP_KEY);
        params.put("salt", salt);
        // params.put("signType", "v3");
        params.put("sign", md5(APP_KEY + query + salt + APP_SECRET));
        // params.put("signType","v3");
        String result = requestForHttp(YOUDAO_URL, params);
        JSONObject jsonObject = JSON.parseObject(result);
        JSONArray jsonArray = jsonObject.getJSONArray("translation");
        if(jsonArray == null){
            return "0";
        }
        return jsonArray.getString(0);
    }


    private static String md5(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md.digest(string.getBytes("UTF-8"));
        return DatatypeConverter.printHexBinary(md5Bytes).toLowerCase();
    }

    private static String sha256(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] shaBytes = sha.digest(string.getBytes("UTF-8"));
        return DatatypeConverter.printHexBinary(shaBytes).toLowerCase();
    }

    private static String requestForHttp(String url, Map<String, String> params) {
        String result = "";
        try {
            result = HttpUtil.post(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;


    }

}
