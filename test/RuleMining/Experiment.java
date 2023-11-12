package RuleMining;

import dataMining.DSR;
import dataMining.single.SingleDSRM;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Experiment {
    @Test
    public void single_DSR(){
        List<DSR> ruleList = new ArrayList<>();
        String xPath = "data/huawei/DSRM/trialX.csv";
        String yPath = "data/huawei/DSRM/trialY.csv";
        SingleDSRM singleDSRM = new SingleDSRM();
        ruleList.addAll(singleDSRM.mining(xPath, yPath));
        for(int i = 0;i<8;i++){
            for(int j = 0;j<57;j++){
                System.out.print(singleDSRM.naiveTime[i][j]);
                System.out.print(",");
                System.out.println(singleDSRM.prunedTime[i][j]);
            }
        }

    }
}
