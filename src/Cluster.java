

import java.util.ArrayList;

public class Cluster {
    private ArrayList<Integer> members;
    private ArrayList<Integer> samples;
    private double[] phi;
    private double alpha;

    public Cluster(ArrayList<Integer> members, double[] phi) {
        this.members = members;
        this.phi = phi;
    }

    public Cluster(ArrayList<Integer> members, double[] phi, double alpha) {
        this.members = members;
        this.phi = phi;
        this.alpha = alpha;
    }

    public Cluster(ArrayList<Integer> members,  ArrayList<Integer> sample_ids, double[] phi ) {
        this.members = members;
        this.phi = phi;
        this.samples = sample_ids;
}


    public ArrayList<Integer> getMembers(){
        return members;
    }

    public ArrayList<Integer> getSamples(){
        return samples;
    }

    public double[] getPhi(){
        return phi;
    }

    public double getAlpha(){
        return alpha;
    }

    public int getsize(){
        return members.size();
    }

}
