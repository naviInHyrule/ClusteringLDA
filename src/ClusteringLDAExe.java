/**
 * @author Mariflor Vega
 * @version 1.0 Build Aug 19 2020
 */


import org.apache.commons.cli.*;


public class ClusteringLDAExe {

    public static Options buildOption(){
        Options options = new Options();
        options.addOption(new Option("help", "print the help message"));
        options.addOption(new Option("root", true, "output file directory"));
        options.addOption(new Option("voc", true, "vocabulary file directory"));
        options.addOption(new Option("testDocs", true, "test corpus file directory"));
        options.addOption(new Option("alphaPrecision", true, "alpha precision"));
        options.addOption(new Option("Phi", true, "Phi file directory"));
        options.addOption(new Option("R", true, "the number of particles"));
        options.addOption(new Option("Alg", true, "Perplexity algorithm"));
        options.addOption(new Option("step", true, "clustering step"));
        options.addOption(new Option("groupMaxMinSize", true, "maximum minimum cluster size per group"));
        options.addOption(new Option("Eval", true, "True for Perplexity"));
        options.addOption(new Option("Alg", true, "Perplexity algorithm"));
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

        Integer groupMaxMinSize = 1;
        if(line.hasOption("groupMaxMinSize")) {
            groupMaxMinSize = new Integer(line.getOptionValue("groupMaxMinSize"));
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




        double alphaPrecision = 3.0;
        if(line.hasOption("alphaPrecision")){
            alphaPrecision = new Double(line.getOptionValue("alphaPrecision"));
        }
        else{
            System.err.println("Precision of Alpha");
        }

        Integer step = 0;
        if(line.hasOption("step")) {
            step = new Integer(line.getOptionValue("step"));
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


        Boolean evaluation = null;
        if(line.hasOption("Eval")){
            evaluation= Boolean.valueOf(line.getOptionValue("Eval"));
        }
        if(evaluation != true & evaluation != false){
            evaluation= false;
        }
        System.out.println(evaluation);

        Vocabulary voc = new Vocabulary();
        voc.readVocabulary(vocabularyInputFile);

        Corpus teCorpus = new Corpus(TeCorpusFile, voc);

        TopicDistributions Phi = new TopicDistributions(phiFile, true);
        Perplexity perplexity = new Perplexity(teCorpus, R, PerplexityType,null);

        MTRandom.setSeed(System.currentTimeMillis());

        ClusteringLDA HC = new ClusteringLDA(Phi, alphaPrecision, root, step, groupMaxMinSize, evaluation,perplexity);
        HC.compute();

        System.out.println("-------------------- Fin -------------------- ");
    }

}
