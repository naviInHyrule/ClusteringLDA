/**
 * @author Hanna Wallach
 * @version 1.0 Build Jul 17 2020
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class LDAwithAnnealing {

  // observed counts

  private WordScoreOpt wordScore;
  private TopicScoreOpt topicScore;

  private int W, T, D; // constants

  private int[][] z; // topic assignments

  private MTRandom rng; // random number generator

  private double getScore(int w, int j, int d) {
    return wordScore.getScore(w, j) * topicScore.getScore(j, d);
  }

  private double getScoreWithFactor(int w, int j, int d, double F) {
    return wordScore.getScoreWithFactor(w, j, F) * topicScore.getScoreWithFactor(j, d, F);
  }



  // computes P(w, z) using the predictive distribution

  private double logProb(Corpus docs) {

    double logProb = 0;

    wordScore.resetCounts();
    topicScore.resetCounts();

    for (int d=0; d<D; d++) {

      Document doc =  docs.getDoc(d);
      int nd = doc.size();

      for (int i=0; i<nd; i++) {

        int w = doc.getWord(i);
        int j = z[d][i];

        double score = getScore(w, j, d);

        logProb += Math.log(score);

        wordScore.incrementCounts(w, j, false);
        topicScore.incrementCounts(j, d);
      }
    }

    return logProb;
  }

  private void sampleTopics(Corpus docs, boolean init, double F) {

    // resample topics

    int ndMax = -1;

    int[] wordCounts = (init) ? new int[W] : null;

    for (int d=0; d<D; d++) {

      Document doc =  docs.getDoc(d);

      int nd = doc.size();

      if (init) {

        z[d] = new int[nd];

        if (nd > ndMax)
          ndMax = nd;
      }

      for (int i=0; i<nd; i++) {

        int w = doc.getWord(i);
        int oldTopic = z[d][i];

        if (!init) {
          wordScore.decrementCounts(w, oldTopic, !init);
          topicScore.decrementCounts(oldTopic, d);
        }

        // build a distribution over topics

        double dist[] = new double[T];
        double distSum = 0.0;

        for (int j=0; j<T; j++) {
          double score = getScoreWithFactor(w, j, d, F);
          dist[j] = score;
          distSum += score;
        }

        int newTopic = rng.nextDiscrete(dist, distSum);

        z[d][i] = newTopic;
        wordScore.incrementCounts(w, newTopic, !init);
        topicScore.incrementCounts(newTopic, d);

        if (init)
          wordCounts[w]++;
      }
    }

    if (init) {
      wordScore.initializeHists(wordCounts);
      topicScore.initializeHists(ndMax);
    }
  }

  public void printState(Corpus docs, Vocabulary voc,  int[][] z, String file) {

    try {

      PrintWriter pw = new PrintWriter(file, "UTF-8");

      pw.println("#doc pos typeindex type topic");

      for (int d=0; d<D; d++) {
        Document doc =  docs.getDoc(d);

        int nd = doc.size();

        for (int i=0; i<nd; i++) {

          int w = doc.getWord(i);

          pw.print(d); pw.print(" ");
          pw.print(i); pw.print(" ");
          pw.print(w); pw.print(" ");
          pw.print(voc.getType(w)); pw.print(" ");
          pw.print(z[d][i]); pw.println();
        }
      }

      pw.close();
    }
    catch (IOException e) {
      System.out.println(e);
    }
  }

  // estimate topics

  public void estimate(Corpus docs, Vocabulary voc , int T, double alpha, double beta, int numItns, int burnItns, int lag, String root) throws FileNotFoundException, UnsupportedEncodingException {

    rng = new MTRandom();

    this.T = T;

    W = voc.size();
    D = docs.numDocs();

    System.out.println("Num docs: " + D);
    System.out.println("Num words in vocab: " + W);
    System.out.println("Num topics: " + T);

    System.out.println("Num numItns: " + numItns);
    System.out.println("Num burnItns: " + burnItns);
    System.out.println("Num lag: " + lag);

    wordScore = new WordScoreOpt(W, T, beta);
    topicScore = new TopicScoreOpt(T, D, alpha);

    z = new int[D][];

    long start = System.currentTimeMillis();

    sampleTopics(docs, true , 1.0);

    // count matrices have been populated, every token has been
    // assigned to a single topic, so Gibbs sampling can start

    PrintWriter logProbWriter = new PrintWriter(root + "/logProb.txt", "UTF-8");

    for (int s=1; s<=numItns; s++) {

      if (s % 10 == 0)
        System.out.println(s);
      else
        System.out.print(".");

      System.out.flush();

      if(s<=burnItns) {
        double F = 2.0 - s*(2.0/burnItns) +1.0;
        sampleTopics(docs, false, F);
      }else {
        sampleTopics(docs, false, 1.0);
      }

      topicScore.optimizeParam(5);
      wordScore.optimizeParamSum(5);

      if (s%1 == 0 | s==numItns) {
        double lp = wordScore.logProb(docs, z);
        logProbWriter.println(lp + " " + logProb(docs));
        logProbWriter.flush();
      }

      if ((s%lag == 0 & s>burnItns) | s==numItns) {
        printState(docs, voc,  z, root + "/state_"+s+".txt");
        topicScore.printParam(root + "/alpha_"+s+".txt");
        wordScore.printParam(root + "/beta_"+s+".txt");
        topicScore.print(root + "/Theta_"+s+".txt");
        wordScore.print(root + "/Phi_"+s+".txt");
      }
    }

  }
}
