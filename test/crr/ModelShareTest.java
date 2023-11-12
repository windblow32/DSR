package crr;

import org.junit.Test;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModelShareTest {
    @Test
    public void test() {
        ModelShare modelShare = new ModelShare();
        // all.csv path
        String dataPath = "data/abt-buy/chinese/similarity/all.csv";
        modelShare.RouMax = 0.11;
        // label index
        modelShare.classIndex = 87;
        // result
        modelShare.outputPath = "E:\\GitHub\\EntityMatching\\data\\abt-buy\\chinese\\block\\all,ROU="+modelShare.RouMax+"block.csv";
        modelShare.crr(dataPath);
    }

}