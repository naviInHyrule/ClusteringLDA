import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */


public class Perplexity {

    private int  D; // constants
    private double perp;
    private double[] LLK;
    private double[] Nd;
    private MTRandom rng; // random number generator
    private Corpus docs;
    private int R;
    private  String root;
    private String perplexityType;

    public Perplexity(Corpus docs, int R, String perplexityType, String root) {
        this.docs = docs;
        this.D = docs.numDocs();
        this.R = R;
        this.root = root;
        this.perplexityType= perplexityType;
    }

    public double run(double alphaSum, TopicDistributions Phi) throws FileNotFoundException, UnsupportedEncodingException {
        LLK = new double[D];
        Nd = new double[D];
        double llk = 0.0;
        if( perplexityType=="Part"){
            System.out.println("LeftToRightPart");
        }else{
            System.out.println("LeftToRightSeq");
        }
        for (int d = 0; d < D; d++) {

            Document doc = docs.getDoc(d);
            if( perplexityType=="Part"){
                LLK[d] = LeftToRight(doc, alphaSum, Phi);
            }else{
                LLK[d] = LeftToRightSeq(doc, alphaSum, Phi);
            }

            Nd[d] = docs.getDoc(d).size();
            System.out.println("----d:"+d+" Nd:" + docs.getDoc(d).size()+" llk:"+LLK[d]);
            llk += LLK[d] ;
        }

        perp=-1*llk/docs.numWords();
        if(root !=null) {
            PrintWriter logProbWriter = new PrintWriter(root + "llk.txt", "UTF-8");

            for (int d = 0; d <D; d++) {
                logProbWriter.println(LLK[d] + "," + docs.getDoc(d).size());
                logProbWriter.flush();
            }

            PrintWriter perpWriter = new PrintWriter(root + "perp.txt", "UTF-8");
            perpWriter.println(perp);
            perpWriter.flush();
        }

        return(perp);
    }


    public double LeftToRight(Document doc, double alphaSum, TopicDistributions Phi) throws FileNotFoundException, UnsupportedEncodingException {

        rng = new MTRandom();
        double logllk = 0.0;
        int Nd= doc.size();
        int T = Phi.length();

        double alpha=alphaSum/T;


        int[] Ns ;
        int[] ss ;
        double[] ps = new double[T];
        double pn;
        int ss_t;

        for (int n = 0; n < Nd; n++) {
            pn = 0.0;
            //System.out.println("w"+doc.getWord(n));
            for (int r = 0; r < R; r++) {
                Ns = new int[T];
                ss = new int[Nd];
                for (int n_ = 0; n_ < n; n_++) {
                    //System.out.println("w_"+doc.getWord(n_));
                    for (int k = 0; k < T; k++) {
                        ps[k] = Phi.getScore(k, doc.getWord(n_)) * (Ns[k] + alpha) / (sum(Ns) + alphaSum);
                    }
                    ss_t = rng.nextDiscrete(ps);
                    ss[n_] = ss_t;
                    Ns[ss_t] += 1;
                    //System.out.println("n: "+n+" r: "+r+ " n_: "+n_+" NS: "+sum(Ns));
                    //for(int j = 0; j < ss.length; j++){
                    //    System.out.print(ss[j]+",");
                    //}
                    //System.out.println();
                }
                for (int k = 0; k < T; k++) {

                    ps[k] = Phi.getScore(k, doc.getWord(n)) * (Ns[k] + alpha) / (sum(Ns) + alphaSum);
                    pn += ps[k];
                    //System.out.println("phiw: "+Phi.getScore(k, n)+" alphaSum: "+alphaSum+ " alpha: "+alpha+" NS: "+sum(Ns)+" pn:"+pn);
                }

            }

            pn /= R;
            logllk += Math.log(pn);
            //System.out.println("--n: "+n+ " pn: "+ pn);

        }

        return logllk;
    }



    public double LeftToRightSeq(Document doc, double alphaSum, TopicDistributions Phi) throws FileNotFoundException, UnsupportedEncodingException {

        rng = new MTRandom();
        double logllk = 0.0;
        int Nd= doc.size();
        int T = Phi.length();
        double alpha=alphaSum/T;
        int[] Ns ;
        int[] ss ;
        Ns = new int[T];
        ss = new int[Nd];
        double[] ps = new double[T];
        double pn;
        int ss_t;
        for (int n = 0; n < Nd; n++) {
            pn = 0.0;
            for (int r = 0; r < R; r++) {
                for (int n_ = 0; n_ < n; n_++) {
                    Ns[ss[n_]] -= 1;
                    for (int k = 0; k < T; k++) {
                        ps[k] = Phi.getScore(k, doc.getWord(n_)) * (Ns[k] + alpha) / (sum(Ns) + alphaSum);
                    }
                    ss_t = rng.nextDiscrete(ps);
                    ss[n_] = ss_t;
                    Ns[ss_t] += 1;
                }
                for (int k = 0; k < T; k++) {
                    ps[k] = Phi.getScore(k, doc.getWord(n)) * (Ns[k] + alpha) / (sum(Ns) + alphaSum);
                    pn += ps[k];
                }

            }

            pn /= R;
            logllk += Math.log(pn);

            for (int k = 0; k < T; k++) {
                ps[k] = Phi.getScore(k, doc.getWord(n)) * (Ns[k] + alpha) / (sum(Ns) + alphaSum);
            }
            ss_t = rng.nextDiscrete(ps);
            ss[n] = ss_t;
            Ns[ss_t] += 1;
        }

        return logllk;
    }

    public double sum( int[] l){
        double sum=0;
        for(int r = 0; r < l.length; r++){
            sum +=l[r];
        }
        return(sum);
    }

    public double LeftToRightBU(Prior alpha, TopicDistributions Phi) throws FileNotFoundException, UnsupportedEncodingException {

        int T = Phi.length();
        rng = new MTRandom();

        TopicScoreOpt topicScore = new TopicScoreOpt(T, D, alpha.getParam());

        int[][] z = new int[D][];

        LLK = new double[D];
        Nd = new double[D];
        for (int d = 0; d < D; d++) {

            Document doc = docs.getDoc(d);
            double logProb = 0.0;
            int nd = doc.size();
            z[d] = new int[nd];
            Nd[d] = nd;
            System.out.println("----d:"+d+" Nd:" + nd);
            for (int j = 0; j < nd; j++) {
                System.out.println("----j:"+j);
                double probSum = 0;
                for (int r = 0; r < R; r++) {
                    System.out.println("----r:"+r);
                    for (int i = 0; i < j; i++) {
                        int w = doc.getWord(i);
                        int oldTopic = z[d][i];
                        topicScore.decrementCounts(oldTopic, d);
                        double dist[] = new double[T];
                        double distSum = 0.0;
                        for (int k = 0; k < T; k++) {
                            double score = Phi.getScore(k,w) * topicScore.getScore(k, d);
                            dist[k] = score;
                            distSum += score;
                        }
                        int newTopic = rng.nextDiscrete(dist);
                        z[d][i] = newTopic;
                        topicScore.incrementCounts(newTopic, d);
                    }
                    int w = doc.getWord(j);
                    double dist[] = new double[T];
                    double distSum = 0.0;
                    for (int k = 0; k < T; k++) {
                        System.out.println("***"+k);
                        double score = Phi.getScore(k,w) * topicScore.getScore(k, d);
                        dist[k] = score;
                        distSum += score;
                    }
                    int newTopic = rng.nextDiscrete(dist);
                    z[d][j] = newTopic;
                    topicScore.incrementCounts(newTopic, d);
                    probSum += distSum;
                }
                probSum = probSum / R;
                logProb += Math.log(probSum);
            }
            LLK[d] = logProb;
            perp +=logProb;
        }


        perp = -1*perp/docs.numWords();

        if(root !=null) {
            PrintWriter logProbWriter = new PrintWriter(root + "llk.txt", "UTF-8");

            for (int d = 0; d <D; d++) {
                logProbWriter.println(LLK[d] + "," + docs.getDoc(d).size());
                logProbWriter.flush();
            }

            PrintWriter perpWriter = new PrintWriter(root + "perp.txt", "UTF-8");
            perpWriter.println(perp);
            perpWriter.flush();
        }

        return perp;
    }

    public double[] getLLK(){
        return(LLK);
    }

    public double getPerplexity(){
        return(perp);
    }

    public double[] getNd(){
        return(Nd);
    }

}
