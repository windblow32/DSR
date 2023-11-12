package metaBlocking;

import graph.MetaBlocking;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class MetaGraph {
    @Test
    public void testMeta() throws Exception {
        MetaBlocking m = new MetaBlocking();
        m.setDataPath("data/computer/large/entity.csv");
        m.meta();
    }

    @Test
    public void test(){
        String str = "hp saca ausa";
        System.out.println(str.contains("hp"));
    }

}
