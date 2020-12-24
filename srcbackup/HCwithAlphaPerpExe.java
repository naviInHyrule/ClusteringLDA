/**
 * @author Mariflor Vega
 * @version 1.0 Build Aug 19 2020
 */


import org.apache.commons.cli.*;


public class HCwithAlphaPerpExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("trainDocs", true, "train corpus file directory"));
        options.addOption(new Option("testDocs", true, "test corpus file directory"));
        options.addOption(new Option("alpha", true, "alpha prior"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        options.addOption(new Option("R", true, "the number of particles"));
        options.addOption(new Option("numItns", true, "number of iterations"));
        options.addOption(new Option("step", true, "clustering step"));
        return options;
    }

    public static void main(String[] args) throws java.io.IOException{
        System.out.println("-------------------- HC with updated Prior and Perplexity -------------------- ");
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
        String TrCorpusFile = null;
        if(line.hasOption("trainDocs")){
            TrCorpusFile = line.getOptionValue("trainDocs");
        }else{
            System.err.println("Please specify the documents directory \'-trainDocs\'");
            System.exit(1);
        }
        String TeCorpusFile = null;
        if(line.hasOption("testDocs")){
            TeCorpusFile = line.getOptionValue("testDocs");
        }else{
            System.err.println("Please specify the documents directory \'-testDocs\'");
            System.exit(1);
        }

        Integer Step = 20;
        if(line.hasOption("step")) {
            Step = new Integer(line.getOptionValue("step"));
        }

        /*
         * Get Dirichlet parameter alpha
         */
        double alpha = 0.1;
        if(line.hasOption("alpha")) {
            alpha = new Double(line.getOptionValue("alpha"));
        }
        /*
         * Get the number of iterations
         */
        Integer numItns = 30;
        if(line.hasOption("numItns"))
            numItns = new Integer(line.getOptionValue("numItns"));

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

        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);

        Corpus teCorpus = new Corpus(TeCorpusFile, voc);
        Corpus trCorpus = new Corpus(TrCorpusFile, voc);

        TopicDistributions Phi = new TopicDistributions(phiFile,false);

        Perplexity perplexity = new Perplexity(teCorpus, R, null);
        Alpha alphaEst = new Alpha(trCorpus, numItns, null);

        MTRandom.setSeed(System.currentTimeMillis());

        HCwithAlphaPerp HC = new HCwithAlphaPerp(Phi, alpha, alphaEst, perplexity, root, Step);
        HC.compute();

        System.out.println("-------------------- Fin -------------------- ");
    }

}
