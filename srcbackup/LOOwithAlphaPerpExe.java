/**
 * @author Mariflor Vega
 * @version 1.0 Build Aug 19 2020
 */


import org.apache.commons.cli.*;


public class LOOwithAlphaPerpExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("testDocs", true, "test corpus file directory"));
        options.addOption(new Option("alpha", true, "alpha hyperparameter"));
        options.addOption(new Option("alphaPrior", true, "alpha prior"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        options.addOption(new Option("R", true, "the number of particles"));
        options.addOption(new Option("refit", true, "refit"));
        options.addOption(new Option("numItns", true, "number of iterations"));
        options.addOption(new Option("trainDocs", true, "train corpus file directory"));
        return options;
    }

    public static void main(String[] args) throws java.io.IOException{
        System.out.println("-------------------- Left One Out with updated Prior and Perplexity -------------------- ");
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
         * Get corpus file.
         */
        String TeCorpusFile = null;
        if(line.hasOption("testDocs")){
            TeCorpusFile = line.getOptionValue("testDocs");
        }else{
            System.err.println("Please specify the documents directory \'-testDocs\'");
            System.exit(1);
        }

        /*
         * Get the number of particles
         */
        Integer R = 100;
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
        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);

        Corpus teCorpus = new Corpus(TeCorpusFile, voc);
        Perplexity perplexity = new Perplexity(teCorpus , R, null);

        TopicDistributions Phi = new TopicDistributions(phiFile);
        Integer T = Phi.length();

        /*
         * Get Dirichlet parameter alpha
         */
        Prior alphaPrior = null;
        if ( (line.hasOption("alpha"))| (line.hasOption("alphaPrior"))) {
            if(line.hasOption("alphaPrior")){
                String alpha = line.getOptionValue("alphaPrior");
                alphaPrior = new Prior(alpha);

            }
            if(line.hasOption("alpha")){
                double alpha= new Double(line.getOptionValue("alpha"));
                double alphaSum = alpha * T;
                alphaPrior = new Prior(alphaSum, T);


            }
        }else{
                System.err.println("Please alpha or alphaPrior ");
                System.exit(1);
        }


        boolean refit= true;
        if(line.hasOption("refit")){
            refit =  Boolean.parseBoolean(line.getOptionValue("refit"));
        }


        LOOwithAlphaPerp LOO;

        if(refit){

            String TrCorpusFile = null;
            if(line.hasOption("trainDocs")){
                TrCorpusFile = line.getOptionValue("trainDocs");
            }else{
                System.err.println("Please specify the documents directory \'-trainDocs\'");
                System.exit(1);
            }
            Corpus trCorpus = new Corpus(TrCorpusFile, voc);
            /*
             * Get the number of iterations
             */
            Integer numItns = 5;
            if(line.hasOption("numItns"))
                numItns = new Integer(line.getOptionValue("numItns"));


            Alpha alphaEst = new Alpha( voc , trCorpus, numItns, null);

            LOO = new LOOwithAlphaPerp(Phi, alphaPrior, alphaEst, perplexity, root, refit);
        }else{
            LOO = new LOOwithAlphaPerp(Phi, alphaPrior, perplexity, root, refit);
        }

        LOO.compute();
        System.out.println("-------------------- Fin -------------------- ");
    }

}
