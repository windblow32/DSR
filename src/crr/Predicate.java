package crr;

public class Predicate {
    private final String first;
    private final String second;
    private final double third;

    public Predicate(String a, String b, double c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    public String getFirst() {
        return this.first;
    }

    public String getSecond() {
        return this.second;
    }

    public double getThird() {
        return this.third;
    }

    @Override
    public String toString() {
        return "(" + first + " " + second + " " + third + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Predicate) {
            Predicate pred = (Predicate) obj;
            return first.equals(pred.getFirst()) && second.equals(pred.getSecond())
                    && third == pred.getThird();
        }
        return false;
    }

    @Override
    public int hashCode(){
        return first.hashCode() + second.hashCode() + new Double(third).hashCode();
    }
}
