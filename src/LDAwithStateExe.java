import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

public class LDAwithStateExe {

  public static void main(String[] args) throws IOException {

    if (args.length != 11) {
      System.out.println("Usage: OLDAExe <vocabulary> <stateFile> <alphaFile> <betaFile> <stateItn> <numItns> <optInitItn><optLag><optItn><wrtLag> <output_dir>");
      System.exit(1);
    }

    // load data

    String vocabularyInputFile = args[0];
    Vocabulary voc = new Vocabulary();
    voc.readVocabulary(vocabularyInputFile);
    //voc.writeVocabulary(vocabularyOutputFile);

    String stateFile = args[1];
    ArrayList state = readState(stateFile);
    String alphaFile = args[2];
    Prior alpha = new Prior(alphaFile);

    String betaFile = args[3];
    Prior beta = new Prior(betaFile);

    System.out.println("Data loaded.");


    Integer S = Integer.parseInt(args[4]); //state to resume from
    int numItns = Integer.parseInt(args[5]); // # Gibbs iterations
    int optInitItn = Integer.parseInt(args[6]);
    int optLag = Integer.parseInt(args[7]);
    int optItn = Integer.parseInt(args[8]);
    int wrtLag = Integer.parseInt(args[9]);
    String outputDir = args[10]; // output directory

    int T = alpha.length(); // # of topics
    int W = beta.length();
    // form output filenames

    String optionsFile = outputDir + "resume_options.txt";


    PrintWriter pw = new PrintWriter(optionsFile, "UTF-8");
    System.out.println("------------------------------ALDA Resume--------------------------------");

    pw.println("State = " + stateFile);
    pw.println("Num words in vocab: = " + W);
    pw.println("Num topics: = " + T);
    pw.println("AlphaSum: " + alpha.sumParam());
    pw.println("BetaSum: " + beta.sumParam());
    pw.println("Iterations = " + numItns);
    pw.println("Optimization Starting Iteration= " + optInitItn);
    pw.println("Optimization Lag = " + optLag);
    pw.println("Optimization Iterations = " + optItn);
    pw.println("writing Lag = " + wrtLag);
    pw.println("Date = " + (new Date()));
    pw.close();
    LDA LDA = new LDA();
    LDA.resume(voc , state, alpha, beta, S, numItns, optInitItn, optLag, optItn, wrtLag, outputDir);

  }

  public static ArrayList<ArrayList> readState(String fileName) {
    ArrayList state = new ArrayList();
    try {

      String read = null;
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      br.readLine();

      while ((read = br.readLine()) != null) {

        String[] line = read.split("\\s+");
        ArrayList state_line = new ArrayList();
        state_line.add( Integer.parseInt(line[0]));
        state_line.add( Integer.parseInt(line[1]));
        state_line.add( Integer.parseInt(line[2]));
        state_line.add( line[3]);
        state_line.add( Integer.parseInt(line[4]));
        state.add(state_line);
      }
      br.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return state;
  }

}
