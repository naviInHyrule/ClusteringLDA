/**
 * @author Mariflor Vega
 * @version 1.0 Build Jul 17 2020
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TopicDistributions {

    private int numTopics;
    private int numTypes;
    private double[][] Phi;
    private int[] topicID;
    private int[] sampleID;

    /**
     * Create topic-by-word matrix, which should have
     * each row contains probability vector, the elements
     * of which are seperated by ", ".
     * @param numTopics number of topics
     * @param numTypes the vocabulary size.
     */

    public TopicDistributions(int numTopics, int numTypes) {
        this.numTopics = numTopics;
        this.numTypes = numTypes;
        this.Phi = new double[numTopics][numTypes];
    }

    public TopicDistributions(double[][] Phi) {
        this.numTopics = Phi.length;
        this.numTypes = Phi[0].length;
        this.Phi = Phi;
    }

    public TopicDistributions(String phiFile, boolean withID) throws IOException {
        if( withID){
            readPhiMatrixWithIDs(phiFile);
        }else {
            readPhiMatrix(phiFile);
        }
    }

        /**
         * Read topic-by-word matrix, which shoule have
         * each row contains probablity vector, the elements
         * of which are seperated by ", ".
         * @param phiFile the file storing the matrix.
         */

    public void readPhiMatrix(String phiFile) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(phiFile));
            numTopics = 0;
            numTypes=0;
            String read = null;
            while ((read = br.readLine()) != null) {
                numTopics+=1;
                String[] split_phi = read.split(",");
                numTypes = split_phi.length;
            }

            topicID = new int[numTopics];
            sampleID = new int[numTopics];
            Phi = new double[numTopics][numTypes];

            BufferedReader br2 = new BufferedReader(new FileReader(phiFile));
            int k = 0;
            while ((read = br2.readLine()) != null) {
                String[] splited = read.split(",");
                topicID[k]=k;
                sampleID[k]=0;
                for (int w = 0; w < splited.length; w++) {
                    Phi[k][w] = Double.parseDouble(splited[w]);
                    if (Phi[k][w] == 0.0){
                        Phi[k][w] += 0.00000001;
                    }
                }
                k+=1;
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void readPhiMatrixWithIDs(String phiFile) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(phiFile));
            numTopics = 0;
            numTypes=0;
            String read = null;
            while ((read = br.readLine()) != null) {
                numTopics+=1;
                String[] split_row = read.split("\\|");
                // chainID|topicID|phi11,phi12...phi1W
                String[] split_phi = split_row[2].split(",");
                numTypes = split_phi.length;
            }

            topicID = new int[numTopics];
            sampleID = new int[numTopics];
            Phi = new double[numTopics][numTypes];
            //System.out.println(topicID.length + sampleID.length +Phi.length);
            BufferedReader br2 = new BufferedReader(new FileReader(phiFile));
            int k = 0;
            while ((read = br2.readLine()) != null) {

                String[] split_row = read.split("\\|");
                sampleID[k]=Integer.parseInt(split_row[0]);
                topicID[k]=Integer.parseInt(split_row[1]);
                String[] split_phi = split_row[2].split(",");
                for (int w = 0; w < split_phi.length; w++) {
                    Phi[k][w] = Double.parseDouble(split_phi[w]);
                    if (Phi[k][w] == 0.0){
                        Phi[k][w] += 0.00000001;
                    }
                }
                k+=1;
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }




    public double getScore(int k, int w){
        return Phi[k][w];
    }

    public int length(){return Phi.length;}

    public double[] getTopic(int k){
        return Phi[k];
    }

    public int getSampleID(int k){
        return sampleID[k];
    }
    public int getTopicID(int k){
        return topicID[k];
    }

    public int getNumberOfSamples(){
        ArrayList<Integer> sampleIDs = new ArrayList<Integer>();
        for(int i = 0; i < sampleID.length; i++){
            sampleIDs.add(sampleID[i]);
        }
        Set<Integer> uniqueSampleID = new HashSet<Integer>(sampleIDs);
        return(uniqueSampleID.size());
    }

    public  ArrayList<Integer>  getSampleTopicIDs(int k){
        ArrayList<Integer> members = new ArrayList<Integer>();
        for(int i = 0; i < sampleID.length; i++){
            if (sampleID[i]==k){
                members.add(topicID[i]);
            }
        }
        return members;
    }


    public double[] aggregate(ArrayList<Integer> indexes){
        double[] new_phi = new double[numTypes];
        int N =  indexes.size();
        if(N>1) {
            for (int w = 0; w < numTypes; w++) {
                new_phi[w] = 0.0;
                for (int i = 0; i < N; i++) {
                    new_phi[w] += Phi[indexes.get(i)][w];
                }
                new_phi[w] = new_phi[w] / N;
            }
            return new_phi;
        }
        else
            return Phi[(int) indexes.get(0)];
    }

    public double[][] getTopics(){
        return(Phi);
    }
}
