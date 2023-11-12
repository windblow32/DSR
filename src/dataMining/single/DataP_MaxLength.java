package dataMining.single;

import java.util.ArrayList;
import java.util.List;

public class DataP_MaxLength {
    private List<Double> data;
    private int max_length;
    private double temp_p_min;

    public DataP_MaxLength(List<Double> data, double temp_p_min, int max_length) {
        this.data = data;
        this.max_length = max_length;
        this.temp_p_min = temp_p_min;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    public void setTemp_p_max(double temp_p_min) {
        this.temp_p_min = temp_p_min;
    }

    public void setMax_length(int max_length) {
        this.max_length = max_length;
    }

    public List<Double> getData() {
        return data;
    }

    public double getTemp_p_min() {
        return temp_p_min;
    }

    public int getMax_length() {
        return max_length;
    }

    public void show() {
        System.out.println("Data: " + data);
        System.out.println("Temp_p_min: " + temp_p_min);
        System.out.println("Max_length: " + max_length);

    }
}
