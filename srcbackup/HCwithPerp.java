import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class HCwithPerp {


    private TopicDistributions Phi;
    private Prior Alpha;
    private int T;
    private Clusters HC;
    private CosineDistance CD;
    private Perplexity perplexity;
    private String root;
    private int Step;


    public HCwithPerp(TopicDistributions Phi, Prior Alpha, Perplexity perplexity , String root, Integer Step, Boolean averageAlpha ) {
        this.Phi = Phi;
        this.Alpha = Alpha;
        this.T = Phi.length();
        this.HC = new Clusters(Phi,Alpha, averageAlpha);
        this.HC.Init();
        this.CD = new CosineDistance(Phi);
        this.perplexity = perplexity;
        this.root = root;
        this.Step = Step;
    }

    public void compute() throws FileNotFoundException, UnsupportedEncodingException {

        int s=0;
        System.out.println("step"+s);
        evaluate(s);
        for(s=1; s< T-1; s++) {
            System.out.println("step"+s);
            step();
            if(s%Step==0)
                evaluate(s);
        }
        s+=1;
        finalStep();
        evaluate(s);


    }
    public void evaluate(int s) throws FileNotFoundException, UnsupportedEncodingException {
        //TopicDistributions newPhi = new TopicDistributions(HC.getPhi(HC.getKeys()));
        //Prior newAlphaPrior = new Prior(HC.getAlpha(HC.getKeys()));
        System.out.println("Calculating Perp with at step"+ s);

        int maxSize= HC.getMaxSize();

        System.out.println("maxSize"+maxSize);
        for(int size=1; size<= maxSize; size++) {
            ArrayList<Integer> keys = new ArrayList<Integer>();
            for (int i = 0; i < HC.getKeys().size(); i++) {
                if (HC.getSize(HC.getKeys().get(i)) >= size) {
                    keys.add(HC.getKeys().get(i));
                }
            }

            if (keys.size()>1) {
                System.out.println("Calculating Perp with "+ keys.size()+" topics.");
                TopicDistributions newPhi = new TopicDistributions(HC.getPhi(keys));
                Prior newAlphaPrior = new Prior(HC.getAlpha(keys));
                double perp = perplexity.LeftToRight(newAlphaPrior, newPhi);
                System.out.println("perp" + perp);
                //printAlpha(newAlphaPrior, root + "/s" + s + "_" + size);
                printPerplexity(perplexity, root + "/s" + s + "_" + size);
                printClusterIDs(HC, root + "/s" + s + "_" + size);
                //printClusterPhis(HC, root + "/s" + s + "_" + size);
                printClusterSize(HC, root + "/s" + s + "_" + size);
            }

        }
    }

    public void step(){
        ArrayList<Integer> ClusterIDs = CD.getMinPair();
        Cluster newCluster = HC.newCluster(ClusterIDs);
        int newkey = HC.getLastKey()+1;
        HC.removeClusters(ClusterIDs);
        CD.removeRows(ClusterIDs);
        double[] newClusterCD = CD.cosineDistance(newCluster.getPhi(), HC.getPhi(HC.getKeys()));
        CD.addRow(newClusterCD, newkey, HC.getKeys());
        HC.addCluster(newkey, newCluster);
    }

    public void finalStep(){
        ArrayList<Integer> ClusterIDs = CD.getMinPair();
        Cluster newCluster = HC.newCluster(ClusterIDs);
        int newkey = HC.getLastKey()+1;
        HC.removeClusters(ClusterIDs);
        CD.removeRows(ClusterIDs);
        HC.addCluster(newkey, newCluster);
    }

    public void accept(ArrayList ClusterIDs, Cluster newCluster, TopicDistributions newPhi){
        HC.removeClusters(ClusterIDs);
        CD.removeRows(ClusterIDs);
        double[] newClusterCD = CD.cosineDistance(newCluster.getPhi(), newPhi.getTopics());
        CD.addRow(newClusterCD, HC.getLastKey(), HC.getKeys() );

    }
    public void reject (int newKey, ArrayList minPair){
        CD.alterMatrix((Integer) minPair.get(0), (Integer) minPair.get(1), 1.0);
        HC.removeCluster(newKey);
    }


    public void printAlpha( Prior AlphaPrior , String root) {
        double[] param = AlphaPrior.getParam();
        try {
            PrintWriter pw = new PrintWriter(root+"_alpha.txt", "UTF-8");
            for (int i=0; i<param.length; i++) {
                pw.print(param[i]);
                if (i < param.length - 1)
                    pw.println();
            }
            pw.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }


    public void printPerplexity( Perplexity perplexity , String root) {
        try {
            PrintWriter logProbWriter = new PrintWriter(root + "_llk.txt", "UTF-8");
            double[] LLK = perplexity.getLLK();
            double[] Nd = perplexity.getNd();
            for (int d = 0; d <LLK.length; d++) {
                logProbWriter.println(LLK[d] + "," + Nd[d]);
                logProbWriter.flush();
            }
            logProbWriter.close();

            PrintWriter perpWriter = new PrintWriter(root + "_perp.txt", "UTF-8");
            perpWriter.println(perplexity.getPerplexity());
            perpWriter.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void printClusterIDs( Clusters HC , String root) {
        try {
            ArrayList<Integer> keys = HC.getKeys();

            PrintWriter IDWriter = new PrintWriter(root + "_IDs.txt", "UTF-8");
            for (int j = 0; j < keys.size(); j++) {
                ArrayList<Integer> members = HC.getMembers(keys.get(j));
                for (int i = 0; i < members.size(); i++) {
                    IDWriter.print(members.get(i));
                    if (i < members.size() - 1)
                        IDWriter.print(",");
                    else
                        IDWriter.println();
                    IDWriter.flush();
                }
            }
            IDWriter.close();
        }
            catch (IOException e) {
                System.out.println(e);
            }

        }

    public void printClusterPhi( Clusters HC , String root) {
        try {
            ArrayList<Integer> keys = HC.getKeys();
            PrintWriter phiWriter = new PrintWriter(root + "_Phi.txt", "UTF-8");
            int W = HC.getPhi((Integer) keys.get(0)).length;
            for (int j = 0; j < keys.size(); j++) {
                for (int w = 0; w < W; w++) {
                    phiWriter.print(HC.getPhi(keys.get(j))[w]);
                    if (w < W - 1)
                        phiWriter.print(",");
                    phiWriter.flush();
                }
                if (j < T - 1)
                    phiWriter.println();
            }
            phiWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void printClusterSize( Clusters HC , String root) {
        try {
            ArrayList<Integer> keys = HC.getKeys();
            PrintWriter sizeWriter = new PrintWriter(root +"_size.txt", "UTF-8");
            for (int i=0; i<keys.size(); i++) {
                sizeWriter.print(HC.getSize(keys.get(i)));
                if(i<keys.size()-1)
                    sizeWriter.print(",");
                sizeWriter.flush();
            }
            sizeWriter.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

}
