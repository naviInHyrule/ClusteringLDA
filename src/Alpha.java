/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Alpha {

  // observed counts
  private Corpus docs;
  private String root;
  private int numItns;
  private int D; // constants
  private MTRandom rng; // random number generator
  private TopicScoreOpt topicScore;
  private int[][] z;
  private TopicDistributions Phi;
  private int T;

  public Alpha( Corpus docs,  int numItns, String root) {
    this.docs = docs;
    this.root = root;
    this.numItns = numItns;
    D = docs.numDocs();
  }

  // computes P(w, z) using the predictive distribution

  private double logProb() {

    double logProb = 0;

    topicScore.resetCounts();

    for (int d = 0; d < D; d++) {

      Document doc = docs.getDoc(d);
      int nd = doc.size();

      for (int i = 0; i < nd; i++) {

        int w = doc.getWord(i);
        int j = z[d][i];

        double score = Phi.getScore(j, w) * topicScore.getScore(j, d);

        logProb += Math.log(score);

        topicScore.incrementCounts(j, d);
      }
    }

    return logProb;
  }

  private void sampleTopics(boolean init) {

    // resample topics
    int ndMax = -1;

    for (int d = 0; d < D; d++) {

      Document doc = docs.getDoc(d);

      int nd = doc.size();

      if (init) {

        z[d] = new int[nd];

        if (nd > ndMax)
          ndMax = nd;
      }

      for (int i = 0; i < nd; i++) {

        int w = doc.getWord(i);
        int oldTopic = z[d][i];

        if (!init) {
          topicScore.decrementCounts(oldTopic, d);
        }

        // build a distribution over topics

        double dist[] = new double[T];
        double distSum = 0.0;

        for (int j = 0; j < T; j++) {
          double score = Phi.getScore(j, w) * topicScore.getScore(j, d);
          dist[j] = score;
          distSum += score;
        }

        int newTopic = rng.nextDiscrete(dist);

        z[d][i] = newTopic;
        topicScore.incrementCounts(newTopic, d);

      }
    }

    if (init) {
      topicScore.initializeHists(ndMax);
    }
  }


  // estimate topics

  public void estimate(Prior alphaPrior, TopicDistributions Phi) throws FileNotFoundException, UnsupportedEncodingException {


    this.Phi = Phi;
    T = Phi.length();
    double[] alpha = alphaPrior.getParam();

    rng = new MTRandom();
    rng.setSeed(System.currentTimeMillis());
    topicScore = new TopicScoreOpt(T, D, alpha);
    z = new int[D][];

    sampleTopics(true);

    for (int s = 1; s <= numItns; s++) {

      if (s % 10 == 0)
        System.out.println(s);
      else
        System.out.print(".");

      System.out.flush();

      sampleTopics(false);

      topicScore.optimizeParam(5);

      if ((root !=null) & (s % 10 == 0 | s == numItns) ){
        topicScore.printParam(root + "alpha_refit_"+s+".txt");
      }

    }
  }

  public Prior getParam(){
    return  new Prior(topicScore.getParam());
  }
}
