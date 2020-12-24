import org.apache.commons.cli.*;

import java.io.PrintWriter;

/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */


public class PerplexityExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("docs", true, "corpus file directory"));
        options.addOption(new Option("alphaPrecision", true, "alphaPrecision"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        options.addOption(new Option("R", true, "the number of particles"));
        options.addOption(new Option("Alg", true, "Perplexity algorithm"));
        return options;
    }

    public static void main(String[] args) throws java.io.IOException{
        System.out.println("-------------------- Calculating Perplexity -------------------- ");
        Options options = buildOption();
        CommandLineParser parser = new GnuParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine line = null;
        try{
            line = parser.parse(options, args);
        }catch( ParseException exp ) {
            System.err.println( "Unexpected exception:" + exp.getMessage() );
            formatter.printHelp("OLDA", options);
            System.exit(1);
        }
        /*
         * Print out help message
         */
        if (line.hasOption("help")) {
            formatter.printHelp("OLDA", options);
            System.exit(0);
        }
        /*
         * Get the folder where all the outputs will be stored.
         */
        String root = null;
        if(line.hasOption("root")){
            root = line.getOptionValue("root");
        }else{
            System.err.println("Please specify the output directory \'-root\'");
            System.exit(1);
        }
        /*
         * Get vocabulary file.
         */
        String vocabularyInputFile = null;
        if(line.hasOption("voc")){
            vocabularyInputFile = line.getOptionValue("voc");
        }else{
            System.err.println("Please specify the vocabulary directory \'-voc\'");
            System.exit(1);
        }
        /*
         * Get corpus file.
         */
        String CorpusFile = null;
        if(line.hasOption("docs")){
            CorpusFile = line.getOptionValue("docs");
        }else{
            System.err.println("Please specify the documents directory \'-docs\'");
            System.exit(1);
        }
        /*
         * Get Dirichlet parameter alpha
         */

        double alphaPrecision = 3.0;
        if(line.hasOption("alphaPrecision")){
            alphaPrecision = new Double(line.getOptionValue("alphaPrecision"));
        }
        else{
            System.err.println("Precision of Alpha");
        }
        /*
         * Get the number of particles
         */
        Integer R = 30;
        if(line.hasOption("R"))
            R = new Integer(line.getOptionValue("R"));
        /*
         * Get Phi file.
         */

        String phiFile = null;;
        if(line.hasOption("Phi")){
            phiFile = line.getOptionValue("Phi");
        }else{
            System.err.println("Please specify the Phi directory \'-Phi\'");
            System.exit(1);
        }

        int algType = 0;
        if(line.hasOption("Alg")){
            algType = new Integer(line.getOptionValue("Alg"));
        }
        else{
            System.err.println("perplexity Algorithm: 1 for Particle, ow for Sequential (default)");
        }

        String PerplexityType;
        if (algType==1){
            PerplexityType="Part";
            System.out.println("Perptype: "+algType +" "+ PerplexityType);
        }else{
            PerplexityType="Seq";
            System.out.println("Perptype: "+algType +" "+ PerplexityType);
        }
        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);

        Integer W = voc.size();
        Corpus teCorpus = new Corpus(CorpusFile, voc);
        Integer D = teCorpus.numDocs();

        TopicDistributions Phi = new TopicDistributions(phiFile, false);


        System.out.println("Num docs: " + D);
        System.out.println("Num words in vocab: " + W);
        System.out.println("Num topics: " + Phi.length());

        MTRandom.setSeed(System.currentTimeMillis());

        Perplexity perp = new Perplexity(teCorpus, R, PerplexityType, root);
        perp.run(alphaPrecision, Phi);

        System.out.println("-------------------- Fin -------------------- ");
    }

}
