import org.apache.commons.cli.*;
/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */
public class AlphaExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("docs", true, "corpus file directory"));
        options.addOption(new Option("numItns", true, "number of iterations"));
        options.addOption(new Option("alpha", true, "Dirichlet parameter alpha"));
        options.addOption(new Option("alpha_prior", true, "Dirichlet parameter alpha"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        return options;
    }

    public static void main(String[] args) throws java.io.IOException{
        System.out.println("-------------------- Refitting Alpha Prior -------------------- ");
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
         * Get the number of iterations
         */
        Integer numItns = 1;
        if(line.hasOption("numItns"))
            numItns = new Integer(line.getOptionValue("numItns"));
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

        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);
        Integer W = voc.size();

        Corpus trCorpus = new Corpus(CorpusFile, voc);
        Integer D = trCorpus.numDocs();

        TopicDistributions Phi = new TopicDistributions(phiFile, false);
        int T = Phi.length();

        /*
         * Get Dirichlet parameter alpha
         */

        Prior alphaPrior = null;
        if ( (line.hasOption("alpha"))| (line.hasOption("alpha_prior"))) {
            if(line.hasOption("alpha_prior")){
            String alpha = line.getOptionValue("alpha_prior");
            alphaPrior = new Prior(alpha);
            }
            if(line.hasOption("alpha")){
                double alpha= new Double(line.getOptionValue("alpha"));
                double alphaSum = alpha * T;
                alphaPrior = new Prior(alphaSum, T);
        } else{
            System.err.println("Please alpha or alpha_prior ");
            System.exit(1);
        }
        }


        System.out.println("Num docs: " + D);
        System.out.println("Num words in vocab: " + W);
        System.out.println("Num topics: " + T);
        MTRandom.setSeed(System.currentTimeMillis());

        Alpha alphaEst = new Alpha(trCorpus, numItns,root);
        alphaEst.estimate(alphaPrior, Phi);

        System.out.println("-------------------- Fin -------------------- ");
    }


}
