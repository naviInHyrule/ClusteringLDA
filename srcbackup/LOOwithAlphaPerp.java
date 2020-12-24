import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class LOOwithAlphaPerp {

    private TopicDistributions Phi;
    private Prior AlphaPrior;
    private Clusters HC;
    private ArrayList<Integer> keys;
    private Alpha alphaEst;
    private Perplexity perplexity;
    private String root;
    private boolean refit;

    public LOOwithAlphaPerp(TopicDistributions Phi, Prior AlphaPrior, Alpha alphaEst, Perplexity perplexity, String root, boolean refit) {
        this.Phi=Phi;
        this.AlphaPrior =AlphaPrior;
        this.alphaEst = alphaEst;
        this.perplexity = perplexity;
        this.root = root;
        this.refit = refit;

    }
    public LOOwithAlphaPerp(TopicDistributions Phi, Prior AlphaPrior, Perplexity perplexity, String root, boolean refit) {
        this.Phi=Phi;
        this.AlphaPrior =AlphaPrior;
        this.perplexity = perplexity;
        this.root = root;
        this.refit = refit;

    }

    public void compute() throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter logProbWriter = new PrintWriter(root + "LOO-log.txt", "UTF-8");
        PrintWriter logProbWriter2 = new PrintWriter(root + "Drop-log.txt", "UTF-8");
        HC = new Clusters(Phi,  AlphaPrior);
        HC.Init();
        int C = HC.getSize();

        Prior newAlpha;
        if(refit) {
            alphaEst.estimate(AlphaPrior, Phi);
            newAlpha = new Prior(alphaEst.getParam());
        }else{
            newAlpha = new Prior(AlphaPrior.getParam()) ;
        }

        double init_perp = perplexity.LeftToRight(newAlpha, Phi);
        System.out.println("Initial perp: " + init_perp);

        logProbWriter.println("TopicID"+","+"new_perp"+","+"perp");
        logProbWriter.flush();
        Probability[] perp_all = new Probability[C];

        for (int i=0; i<C; i++) {
            ArrayList<Integer> newKeys = filterKeys(HC.getKeys(),i);
            TopicDistributions newPhi = HC.getNewPhi(newKeys);
            if(refit) {
                alphaEst.estimate(AlphaPrior, newPhi);
                newAlpha = new Prior(alphaEst.getParam());
            }else{
                newAlpha = HC.getNewAlpha(newKeys);
            }
            double new_perp = perplexity.LeftToRight(newAlpha, newPhi);
            perp_all[i] = new Probability(i, new_perp);
            System.out.println(i + "," + new_perp+ ','+init_perp);
            logProbWriter.println(i + "," + new_perp+ ','+init_perp);
            logProbWriter.flush();
        }

        Arrays.sort(perp_all, Collections.reverseOrder());
        logProbWriter2.println("LEFT"+","+"new_perp"+","+"perp");
        logProbWriter2.flush();
        double perp = init_perp;
        for (int i=0; i<C; i++) {
            ArrayList<Integer> newKeys = filterKeys(HC.getKeys(), perp_all[i].index);
            TopicDistributions newPhi = HC.getNewPhi(newKeys);
            if(refit) {
                alphaEst.estimate(AlphaPrior, newPhi);
                newAlpha = new Prior(alphaEst.getParam());
            }else{
                newAlpha = HC.getNewAlpha(newKeys);
            }

            double new_perp = perplexity.LeftToRight(newAlpha, newPhi);
            double diff = Math.abs(new_perp-perp);

            if ((new_perp < perp) | (diff<0.01)){
                HC.removeCluster(perp_all[i].index);
                if (new_perp < perp)
                    perp = new_perp;
                logProbWriter2.println(perp_all[i].index + "," + new_perp+ ','+perp);
                logProbWriter2.flush();
            }
            System.out.println(perp_all[i].index + "th out perp: " + new_perp);
        }
    }


    public void compute2() throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter logProbWriter = new PrintWriter(root + "LOO-log.txt", "UTF-8");
        PrintWriter logProbWriter2 = new PrintWriter(root + "Drop-log.txt", "UTF-8");

        Prior newAlpha;
        if(refit) {
            alphaEst.estimate(AlphaPrior, Phi);
            newAlpha = new Prior(alphaEst.getParam());
        }else{
            newAlpha = new Prior(AlphaPrior.getParam()) ;
        }

        HC = new Clusters(Phi,  newAlpha);
        HC.Init();
        int C = HC.getSize();

        double init_perp = perplexity.LeftToRight(newAlpha, Phi);
        System.out.println("Initial perp: " + init_perp);

        logProbWriter.println("TopicID"+","+"new_perp"+","+"perp");
        logProbWriter.flush();
        Probability[] perp_all = new Probability[C];

        double perp = init_perp;
        for (int i=0; i<C; i++) {
            ArrayList<Integer> newKeys = filterKeys(HC.getKeys(),i);
            TopicDistributions newPhi = HC.getNewPhi(newKeys);
            if(refit) {
                alphaEst.estimate(AlphaPrior, newPhi);
                newAlpha = new Prior(alphaEst.getParam());
            }else{
                newAlpha = HC.getNewAlpha(newKeys);
            }
            double new_perp = perplexity.LeftToRight(newAlpha, newPhi);
            perp_all[i] = new Probability(i, new_perp);
            System.out.println(i + "," + new_perp+ ','+init_perp);
            logProbWriter.println(i + "," + new_perp+ ','+init_perp);
            logProbWriter.flush();


            double diff = Math.abs(new_perp-perp);
            if ((new_perp < perp) | (diff<0.01)){
                HC.removeCluster(perp_all[i].index);
                if (new_perp < perp)
                    perp = new_perp;
                logProbWriter2.println(perp_all[i].index + "," + new_perp+ ','+perp);
                logProbWriter2.flush();
            }
            System.out.println(perp_all[i].index + "th out perp: " + new_perp);
        }

    }


    public ArrayList<Integer> filterKeys(ArrayList<Integer> keys , Integer key) {
        ArrayList<Integer> newKeys = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            newKeys.add(keys.get(i));
        }
        newKeys.remove(key);
        return(newKeys);
    }


}
