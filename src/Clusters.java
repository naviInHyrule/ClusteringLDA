
import java.util.*;
import java.util.ArrayList;

public class Clusters {


    private Map<Integer, Cluster> all_clusters = new HashMap<Integer,Cluster>();
    private TopicDistributions Phi;
    private Prior Alpha;
    private boolean AverageAlpha;


    public Clusters(TopicDistributions Phi) {
        this.Phi=Phi;
        this.Alpha = null;
    }


    public Clusters(TopicDistributions Phi, Prior Alpha) {
        this.Phi=Phi;
        this.Alpha=Alpha;
        this.AverageAlpha = true;
    }

    public Clusters(TopicDistributions Phi, Prior Alpha, boolean averageAlpha) {
        this.Phi=Phi;
        this.Alpha=Alpha;
        this.AverageAlpha = averageAlpha;
    }

    public void Init() {
        for (int c = 0; c < Phi.length(); c++) {
            ArrayList<Integer> member = new ArrayList<Integer>(1);
            member.add(Phi.getTopicID(c));
            ArrayList<Integer> chain = new ArrayList<Integer>(1);
            chain.add(Phi.getSampleID(c));
            Cluster cluster;
            if(Alpha == null )
                cluster = new Cluster( member, chain, Phi.getTopic(c));
            else
                cluster = new Cluster( member, Phi.getTopic(c), Alpha.getParam(c));
            all_clusters.put(all_clusters.size(), cluster);
        }
    }

    public boolean differentSamples(ArrayList minPair){
        boolean differentSamples = true;
        ArrayList<Integer> samples = new ArrayList<Integer>();
        ArrayList<Integer> s1 =  all_clusters.get(minPair.get(0)).getSamples();
        ArrayList<Integer> s2 =  all_clusters.get(minPair.get(1)).getSamples();
        for(int i=0; i<s1.size(); i++) {
            for(int j=0; j<s2.size(); j++) {
                if(s1.get(i)==s2.get(j)){
                    differentSamples = false;
                    break;
                }
            }
        }
        return(differentSamples);
    }


    public Cluster newCluster(ArrayList minPair){
        ArrayList<Integer> members = new ArrayList<Integer>();
        ArrayList<Integer> samples = new ArrayList<Integer>();
        for(int i=0; i<minPair.size(); i++){
            ArrayList<Integer> m =  all_clusters.get(minPair.get(i)).getMembers();
            ArrayList<Integer> s =  all_clusters.get(minPair.get(i)).getSamples();
            for(int j=0; j<m.size(); j++) {
                //System.out.println("member: "+m.get(j)+" - alpha: "+Alpha.getParam(m.get(j)));
                members.add(m.get(j));
                samples.add(s.get(j));
            }
        }
        Cluster newCluster;
        if(Alpha!= null ){
            newCluster = new Cluster(members, Phi.aggregate(members), Alpha.aggregate(members, AverageAlpha));
            System.out.println("new alpha: "+Alpha.aggregate(members,AverageAlpha ));
        }else{
            newCluster = new Cluster(members,samples, Phi.aggregate(members));
        }
        return newCluster;
    }

    public void addCluster( int key, Cluster newCluster) {
        all_clusters.put(key,newCluster);
        //System.out.println("N clusters :"+all_clusters.size());
    }

    public double[][] getPhi(ArrayList<Integer> keys){
        double[][] Phi = new double[keys.size()][];
        for (int i=0; i<keys.size();i++){
            Phi[i] = all_clusters.get(keys.get(i)).getPhi();
        }
        return Phi;
    }

    public ArrayList<Integer> getMembers(int key){
        return all_clusters.get(key).getMembers();
    }
    public double[] getAlpha(ArrayList<Integer> keys){
        double[] Alpha= new double[keys.size()];
        for (int i=0; i<keys.size();i++){
            Alpha[i] = all_clusters.get(keys.get(i)).getAlpha();
        }
        return Alpha;
    }

    public double[] getPhi(int key){
        return all_clusters.get(key).getPhi();
    }

    public int getSize(){
        return all_clusters.size();
    }

    public  int getSize(int key){
        return all_clusters.get(key).getsize();
    }


    public ArrayList<Integer> getKeys() {
        ArrayList<Integer> keys = new ArrayList<>(all_clusters.keySet());
        return keys;
    }

    public int getLastKey(){
        ArrayList<Integer>  keys = getKeys() ;
        return Collections.max(keys);
    }

    public void removeClusters(ArrayList ClusterIDs) {
        for(int i=0; i<ClusterIDs.size(); i++) {
            all_clusters.remove(ClusterIDs.get(i));
        }
    }
    public void removeCluster(int key){
        all_clusters.remove(key);
    }


    public int getMaxSize(){
        int maxSize=0;
        ArrayList<Integer> keys = new ArrayList<>(all_clusters.keySet());
        for (int i=0; i<keys.size();i++){
            if (maxSize< all_clusters.get(keys.get(i)).getsize()){
                maxSize= all_clusters.get(keys.get(i)).getsize();
            }
        }
        return maxSize;
    }
}
