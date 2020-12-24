//package edu.umass.cs.iesl.wallach.hierarchical;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import gnu.trove.TIntArrayList;

import org.apache.commons.math3.analysis.function.Gaussian;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LDA {
  // observed counts

  private WordScoreOpt wordScore;
  private TopicScoreOpt topicScore;

  private int W, T, D; // constants

  private int[][] z; // topic assignments

  private MTRandom rng; // random number generator

  private double getScore(int w, int j, int d) {
    return wordScore.getScore(w, j) * topicScore.getScore(j, d);
  }

  // computes P(w, z) using the predictive distribution

  private double logProb(Corpus docs) {

    double logProb = 0;

    wordScore.resetCounts();
    topicScore.resetCounts();

    for (int d=0; d<D; d++) {

      Document doc = docs.getDoc(d);

      int nd = doc.size();

      for (int i=0; i<nd; i++) {

        int w = doc.getWord(i);
        int j = z[d][i];

        logProb += Math.log(getScore(w, j, d));

        wordScore.incrementCounts(w, j, false);
        topicScore.incrementCounts(j, d);
      }
    }

    return logProb;
  }

  //t temperature
  private void sampleTopics(Corpus docs, boolean init) {

    // resample topics

    int ndMax = -1;

    int[] wordCounts = (init) ? new int[W] : null;

    for (int d=0; d<D; d++) {

      Document doc = docs.getDoc(d);

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

          double score = getScore(w, j, d);
          dist[j] = score;
          distSum += score;
        }

        int newTopic = rng.nextDiscrete(dist);

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

  public void printState(Vocabulary voc, Corpus docs, int[][] z, String file) {

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

  public void estimate(Vocabulary voc, Corpus docs, int T, double alpha, double beta, int numItns, int optInitItn, int optLag, int optItns, int writeLag, String outputDir) {


    rng = new MTRandom();
    rng.setSeed(System.currentTimeMillis());

    this.T = T;

    W = voc.size();
    D = docs.numDocs();

    wordScore = new WordScoreOpt(W, T, beta);
    topicScore = new TopicScoreOpt(T, D, alpha);

    z = new int[D][];

    sampleTopics(docs, true);

    try {

      PrintWriter logProbWriter = new PrintWriter( outputDir + "log_prob.txt", "UTF-8");

      // count matrices have been populated, every token has been
      // assigned to a single topic, so Gibbs sampling can start

      for (int s=1; s<=numItns; s++) {

        //if (s % 10 == 0)
        //  System.out.print(s);
        //else
        //System.out.print(".");
        //System.out.flush();

        sampleTopics(docs, false);

        if(s>=optInitItn & s%optLag==0) {
          System.out.println("iteration: "+s);
          if( optItns!=0) {
            topicScore.optimizeParam(optItns);
            wordScore.optimizeParamSum(optItns);
          }else{
            topicScore.optimizeParamC();
            wordScore.optimizeParamSumC();
          }
        }

        if (s%10 == 0 | s==numItns) {
          double lp = wordScore.logProb(docs, z);
          logProbWriter.println(lp + " " + logProb(docs));
          logProbWriter.flush();
        }

        if (s%writeLag == 0 | s==numItns) {
            topicScore.printParam(outputDir + "/alpha_"+s+".txt");
            wordScore.printParam(outputDir + "/beta_"+s+".txt");
            //topicScore.print(docs, 0.0, -1, outputDir + "Theta_"+s+".txt");
            wordScore.print(voc, 0.0, -1, outputDir + "Phi_"+s+".txt");
            wordScore.print(outputDir + "Phi_vec"+s+".txt");
            printState( voc, docs, z, outputDir + "/state.txt");

        }

      }

    }
    catch (IOException e) {
      System.out.println(e);
    }
  }



  public void resume(Vocabulary voc, ArrayList<ArrayList> state, Prior alpha, Prior beta, int S , int numItns,  int optInitItn, int optLag, int optItns, int wrtLag, String outputDir) {

    rng = new MTRandom();
    rng.setSeed(System.currentTimeMillis());

    W = voc.size();
    int[] wordCounts = new int[W];

    this.T = alpha.length();
    this.D = (Integer) state.get(state.size()-1).get(0)+1;

    z = new int[D][];
    wordScore = new WordScoreOpt(W, T, beta.getParam());
    topicScore = new TopicScoreOpt(T, D, alpha.getParam());

    ArrayList<Document> docList = new ArrayList();
    int docIndex = (Integer) state.get(0).get(0);
    TIntArrayList words = new TIntArrayList();
    TIntArrayList topics = new TIntArrayList();
    String text = null;
    int ndMax=0;
    for(int i=0; i<state.size(); i++){
      int d = (Integer) state.get(i).get(0);
      int w = (Integer) state.get(i).get(2);
      String p = (String) state.get(i).get(3);
      int t = (Integer) state.get(i).get(4);

      if(d==docIndex){
        words.add(w);
        topics.add(t);
        text= text+"|"+p;
        if(i==state.size()-1){
          Document doc = new Document(words, docIndex, text);
          docList.add(doc);
          int Nd= topics.size();
          z[docIndex]= new int[Nd];
          for(int j=0; j<Nd; j++){
            z[docIndex][j]=topics.get(j);
          }
          if(Nd>ndMax){
            ndMax=Nd;
          }
        }
      }else{
        Document doc = new Document(words, docIndex, text);
        docList.add(doc);
        int Nd= topics.size();
        z[docIndex]= new int[Nd];
        for(int j=0; j<Nd; j++){
          z[docIndex][j]=topics.get(j);
        }
        if(Nd>ndMax){
          ndMax=Nd;
        }

        words = new TIntArrayList();
        topics = new TIntArrayList();
        words.add(w);
        topics.add(t);
        text = p;
        docIndex=d;
      }

      wordScore.incrementCounts(w, t, false);
      topicScore.incrementCounts(t, d);
      wordCounts[w]++;
    }


    Corpus docs = new Corpus(docList);
    wordScore.initializeHists(wordCounts);
    topicScore.initializeHists(ndMax);

    System.out.println("Number of documents: " + D);
    System.out.println("Number of topics: " + T);
    System.out.println("Number of words: " + docs.numWords());
    System.out.println("Vocabulary size: " + W);

    try {

      PrintWriter logProbWriter = new PrintWriter( outputDir + "log_prob_"+S+".txt", "UTF-8");

      // count matrices have been populated, every token has been
      // assigned to a single topic, so Gibbs sampling can start

      for (int s=S+1; s<=numItns; s++) {

        //if (s % 10 == 0)
        //  System.out.print(s);
        //else
        //  System.out.print(".");
        //System.out.flush();

        sampleTopics(docs, false);

          if(s>=optInitItn & s%optLag==0) {
              System.out.println("iteration: "+s);
              if( optItns!=0) {
                  topicScore.optimizeParam(optItns);
                  wordScore.optimizeParamSum(optItns);
              }else{
                  topicScore.optimizeParamC();
                  wordScore.optimizeParamSumC();
              }
          }

          if (s%10 == 0 | s==numItns) {
              double lp = wordScore.logProb(docs, z);
              logProbWriter.println(lp + " " + logProb(docs));
              logProbWriter.flush();
          }

        if (s%wrtLag == 0  | s==numItns) {
          topicScore.printParam(outputDir + "/alpha_"+s+".txt");
          wordScore.printParam(outputDir + "/beta_"+s+".txt");
          //topicScore.print(docs, 0.0, -1, outputDir + "Theta_"+s+".txt");
          System.out.println("printing");
          wordScore.print(voc, 0.0, -1, outputDir + "/Phi_"+s+".txt");
          wordScore.print(outputDir + "/Phi_vec"+s+".txt");
          printState( voc, docs, z, outputDir + "/state.txt");

        }

      }

    }
    catch (IOException e) {
      System.out.println(e);
    }
  }
}
