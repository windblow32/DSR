package dataMining.single;

public class ds_i_ObsEvi {
    private double ds_i;
    private int obs;
    private int evi;
    public ds_i_ObsEvi(double ds_i, int obs, int evi)
    {
        this.ds_i = ds_i;
        this.obs  = obs;
        this.evi  = evi;
    }
    public double getDs_i() {
        return ds_i;
    }
    public int getEvi() {
        return evi;
    }
    public int getObs() {
        return obs;
    }
}
