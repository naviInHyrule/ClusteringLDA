/**
 * @author Mariflor Vega
 * @version 1.0 Build Aug 19 2020
 */


import org.apache.commons.cli.*;


public class HCwithPerp2Exe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("testDocs", true, "test corpus file directory"));
        options.addOption(new Option("alphaPrior", true, "alpha prior"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        options.addOption(new Option("R", true, "the number of particles"));
        options.addOption(new Option("averageAlpha", true, "aggregation method for alpha. true for average false for sumation"));
        options.addOption(new Option("GroupMaxMinSize", true, "maximum minimum cluster size per group"));
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

        String TeCorpusFile = null;
        if(line.hasOption("testDocs")){
            TeCorpusFile = line.getOptionValue("testDocs");
        }else{
            System.err.println("Please specify the documents directory \'-testDocs\'");
            System.exit(1);
        }

        Integer GroupMaxMinSize = 1;
        if(line.hasOption("GroupMaxMinSize")) {
            GroupMaxMinSize = new Integer(line.getOptionValue("GroupMaxMinSize"));
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

        String alphaFile = null;
        if(line.hasOption("alphaPrior")){
            alphaFile = line.getOptionValue("alphaPrior");
        } else{
            System.err.println("Please alpha or alpha_prior ");
            System.exit(1);
        }

        boolean averageAlpha = false;
        if(line.hasOption("averageAlpha")) {
                averageAlpha = new Boolean(line.getOptionValue("averageAlpha"));
            }
        else{
            System.err.println("averageAlpha can only be true or false");
        }

        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);

        Corpus teCorpus = new Corpus(TeCorpusFile, voc);

        TopicDistributions Phi = new TopicDistributions(phiFile, false);
        Prior Alpha = new Prior(alphaFile);
        Perplexity perplexity = new Perplexity(teCorpus, R, null);

        MTRandom.setSeed(System.currentTimeMillis());

        HCwithPerp2 HC = new HCwithPerp2(Phi, Alpha, perplexity, root, averageAlpha,GroupMaxMinSize );
        HC.compute();

        System.out.println("-------------------- Fin -------------------- ");
    }

}
