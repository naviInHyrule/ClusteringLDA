import org.apache.commons.cli.*;

/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */
public class LDAExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("docs", true, "corpus file directory"));
        options.addOption(new Option("numItns", true, "number of iterations"));
        options.addOption(new Option("burnItns", true, "number burn-in of iterations"));
        options.addOption(new Option("lag", true, "number lag of iterations"));
        options.addOption(new Option("T", true, "the number of topics"));
        options.addOption(new Option("alpha", true, "Dirichlet parameter alpha"));
        options.addOption(new Option("beta", true, "Dirichlet parameter beta"));
        return options;
    }

    public static void main(String[] args) throws java.io.IOException{
        System.out.println("-------------------- LDA -------------------- ");
        Options options = buildOption();
        CommandLineParser parser = new GnuParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine line = null;
        try{
            line = parser.parse(options, args);
        }catch( ParseException exp ) {
            System.err.println( "Unexpected exception:" + exp.getMessage() );
            formatter.printHelp("LDA", options);
            System.exit(1);
        }
        /*
         * Print out help message
         */
        if (line.hasOption("help")) {
            formatter.printHelp("LDA", options);
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
         * Get the number of topics
         */
        Integer T = 2;
        if(line.hasOption("T"))
            T = new Integer(line.getOptionValue("T"));
        /*
         * Get Dirichlet parameter alpha
         */
        double alpha = 0.1;
        if(line.hasOption("alpha"))
            alpha = new Integer(line.getOptionValue("alpha"));
        /*
         * Get Dirichlet parameter beta
         */
        double beta = 0.01;
        if(line.hasOption("beta"))
            beta = new Integer(line.getOptionValue("beta"));
        /*
         * Get the number of iterations
         */
        Integer numItns = 100;
        if(line.hasOption("numItns"))
            numItns = new Integer(line.getOptionValue("numItns"));

        Integer burnItns = numItns- (int) Math.round(numItns*0.5);
        if(line.hasOption("burnItns"))
            burnItns = new Integer(line.getOptionValue("burnItns"));

        Integer lag = 100;
        if(line.hasOption("lag"))
            lag = new Integer(line.getOptionValue("lag"));

        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);
        //voc.writeVocabulary(vocabularyOutputFile);

        Integer W = voc.size();
        Corpus trCorpus = new Corpus(CorpusFile, voc);
        alpha = alpha * T; //by HW definition alpha_t = alpha/T
        beta = beta *W; //by HW definition beta_w = alpha/W

        LDA2 lda = new LDA2();
        lda.estimate(trCorpus, voc, T, alpha, beta, numItns,burnItns,lag,root,false);

        System.out.println("-------------------- Fin -------------------- ");
    }


}
