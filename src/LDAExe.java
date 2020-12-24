//package edu.umass.cs.iesl.wallach.hierarchical;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class LDAExe {

  public static void main(String[] args) throws IOException {

    if (args.length != 11) {
      System.out.println("Usage: OLDAExe <vocabulary> <instances> <numTopics> <alphaSum> <betaSum><numItns><optInitItn> <optlag> <optItns><writelag><output_dir>");
      System.exit(1);
    }

    // load data

    String vocabularyInputFile = args[0];
    Vocabulary voc = new Vocabulary();
    voc.readVocabulary(vocabularyInputFile);
    //voc.writeVocabulary(vocabularyOutputFile);

    String CorpusFile = args[1];
    Integer W = voc.size();
    Corpus docs = new Corpus(CorpusFile, voc);
    Integer D = docs.numDocs();

    System.out.println("Data loaded.");
    int T = Integer.parseInt(args[2]); // # of topics
    // # Gibbs iterations
    double alphaSum = Double.parseDouble(args[3]);
    double betaSum = Double.parseDouble(args[4]);
    int numItns = Integer.parseInt(args[5]);
    int optInitItn = Integer.parseInt(args[6]);
    int optLag = Integer.parseInt(args[7]);
    int optItns = Integer.parseInt(args[8]);
    int wrtLag = Integer.parseInt(args[9]);
    String outputDir = args[11]; // output directory

    // form output filenames

    String optionsFile = outputDir + "options.txt";


    PrintWriter pw = new PrintWriter(optionsFile, "UTF-8");
    pw.println("Data = " + CorpusFile);
    pw.println("Num docs: = " + docs.numDocs());
    pw.println("Num words = " + docs.numWords());
    pw.println("Num words in vocab: = " + W);
    pw.println("Num topics: = " + T);
    pw.println("AlphaSum: " + alphaSum);
    pw.println("BetaSum: " + betaSum);
    pw.println("Iterations = " + numItns);
    pw.println("Optimization Starting Iteration= " + optInitItn);
    pw.println("Optimization Lag = " + optLag);
    pw.println("Optimization Iterations = " + optItns);
    pw.println("writing Lag = " + wrtLag);
    pw.println("Date = " + (new Date()));
    pw.close();

    System.out.println("Num docs: " + D);
    System.out.println("Num words in vocab: " + W);
    System.out.println("Num topics: " + T);


    LDA LDA = new LDA();

    LDA.estimate(voc , docs, T, alphaSum, betaSum, numItns,optInitItn, optLag, optItns, wrtLag, outputDir);

  }
}
