package crr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rule implements Comparable<Rule>{
    private double index;
    private Set<Predicate> context;
    public Rule(double index, Set<Predicate> context){
        super();
        this.index = index;
        this.context = context;
    }
    @Override
    public int compareTo(Rule o) {
        return this.getIndex() > o.getIndex() ? 1 : 0;
    }

    @Override
    public String toString(){
        return "Rule [index=" + index + ", context=" + context + "]";
    }


    public double getIndex() {
        return index;
    }

    public void setIndex(double index) {
        this.index = index;
    }

    public Set<Predicate> getContext() {
        return context;
    }

    public void setContext(Set<Predicate> context) {
        this.context = context;
    }
    public void addConstraint(Predicate constraint){
        this.context.add(constraint);
    }
}
