import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class HCwithAlphaPerp {

    private TopicDistributions Phi;
    private int T;
    private double alpha;
    private Clusters HC;
    private CosineDistance CD;
    private Alpha alphaEstimator;
    private Perplexity perplexity;
    private String root;
    private int Step;

    public HCwithAlphaPerp(TopicDistributions Phi, double alpha, Alpha alphaEstimator,  Perplexity perplexity , String root, Integer Step ) {
        this.Phi=Phi;
        this.T = Phi.length();
        this.alpha =alpha;
        this.HC = new Clusters(Phi);
        this.HC.Init();
        this.CD = new CosineDistance(Phi);
        this.alphaEstimator = alphaEstimator;
        this.perplexity = perplexity;
        this.root = root;
        this.Step = Step;
    }

    public void compute() throws FileNotFoundException, UnsupportedEncodingException {

        for(int s=0; s< T-2; s++) {
            System.out.println("step"+s);
            if(s%Step==0){
                evaluate(s);
            }
            step();
        }
        int s = T-1;
        System.out.println("step"+s);
        evaluate(s);
    }
    public void evaluate(int s) throws FileNotFoundException, UnsupportedEncodingException {
        TopicDistributions newPhi = new TopicDistributions(HC.getPhi(HC.getKeys()));
        int Q = newPhi.length();
        Prior AlphaPriorInit = new Prior(alpha * Q,Q);
        alphaEstimator.estimate(AlphaPriorInit, newPhi);
        Prior newAlphaPrior = alphaEstimator.getParam();
        double perp = perplexity.LeftToRight(newAlphaPrior, newPhi);
        System.out.println("perp"+perp);
        printAlpha(newAlphaPrior, root + "/s"+s);
        printPerplexity(perplexity,  root + "/s"+s);
        printClusters(HC,  root + "/s"+s);
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

    public void printClusters( Clusters HC , String root) {
        try {
            PrintWriter phiWriter = new PrintWriter(root +"_Phi.txt", "UTF-8");
            ArrayList<Integer>keys = HC.getKeys();
            int W = HC.getPhi((Integer) keys.get(0)).length;
            for (int j=0; j<keys.size(); j++) {
                for (int w=0; w<W; w++){
                    phiWriter.print(HC.getPhi(keys.get(j))[w]);
                    if(w<W-1)
                        phiWriter.print(",");
                    phiWriter.flush();
                }
                if(j<T-1)
                    phiWriter.println();
            }
            phiWriter.close();

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
