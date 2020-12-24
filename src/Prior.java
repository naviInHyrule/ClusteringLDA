import java.io.*;
import java.util.ArrayList;

/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */
public class Prior {
    private double[] param;
    public Prior(double[] alpha) {
        this.param = alpha;
    }

    public Prior(double priorSum, int T) {
        param = new double[T];
        for (int i = 0; i < T; i++) {
            this.param[i] = priorSum/T;
        }
    }

    public Prior(String fileName) {
        param = readPrior(fileName);
    }

    public double[] readPrior(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            int T = 0;
            String read = null;
            while ((read = br.readLine()) != null) {
                T+=1;
            }
            param = new double[T];

            int i = 0;
            BufferedReader br2 = new BufferedReader(new FileReader(fileName));
            while ((read = br2.readLine()) != null) {
                String[] splited = read.split(",");
                param[i]= Double.parseDouble(splited[0]);
                i++;
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return param;
    }
    public double getParam(int t){
        return param[t];
    }

    public double[] getParam(){
        return param;
    }


    public double sumParam(){
        double sumParam = 0.0;
        for (int i = 0; i < param.length; i++) {
            sumParam += getParam(i);
        }
        return sumParam;
    }

    public int length(){
        return param.length;
    }


    public double aggregate(ArrayList<Integer> indexes , boolean average){
        double new_alpha=0.0;
        int N = indexes.size();
        for (int i = 0; i < N; i++) {
            new_alpha += getParam(indexes.get(i));
        }
        //new alpha is the average alpha given indexes o.w it is the sum
        if (average)
                new_alpha= new_alpha/indexes.size();

        return new_alpha;

    }
}
