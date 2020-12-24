/**
 * @author Mariflor Vega
 * @version 1.0 Build Aug 19 2020
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Measures the Cosine similarity of two vectors of an inner product space and
 * compares the angle between them.
 */

public class CosineDistance {

    private ArrayList<Double> CosineDistance;
    private ArrayList<Integer> xindex;
    private ArrayList<Integer> yindex;
    private TopicDistributions Phi;
    private int K;

    public CosineDistance(TopicDistributions Phi){
        this.Phi = Phi;
        K=Phi.length();
        Init();
    }

    /**
     * Computes partial cosine distance matrix (triangular upper matrix).
     * Diagonal and triangular upper matrix values are 1.0.
     */
    public void Init(){
        CosineDistance = new ArrayList<Double>();
        xindex = new ArrayList<Integer>();
        yindex = new ArrayList<Integer>();
        for (int i=0; i<K;i++){
            for (int j=i+1; j<Phi.length();j++) {
                double d = cosineDistance(Phi.getTopic(i), Phi.getTopic(j));
                CosineDistance.add(d);
                xindex.add(i);
                yindex.add(j);
            }
        }
    }
    /**
     * Calculates the cosine distance for two given vectors.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine distance between the two vectors
     */
    public double cosineDistance(double[] leftVector, double[] rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (leftVector.length != rightVector.length) {
            throw new IllegalArgumentException("Vectors must not have the same length");
        }

        double dotProduct = dot(leftVector, rightVector);

        double d1 = 0.0d;
        for (int i = 0; i < leftVector.length; i++) {
            d1 += Math.pow(leftVector[i] , 2);
        }

        double d2 = 0.0d;
        for (int i = 0; i < rightVector.length; i++) {
            d2 += Math.pow(rightVector[i] , 2);
        }
        double cosineSimilarity = dotProduct /  (Math.sqrt(d1) * Math.sqrt(d2));
        return 1-cosineSimilarity;
    }

    public double[] cosineDistance(double[] leftVector, double[][] rightMatrix) {
        if (leftVector == null || rightMatrix == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (leftVector.length != rightMatrix[0].length) {
            throw new IllegalArgumentException("Vectors must not have the same length");
        }

        double[] cosineDistanceVector = new double[rightMatrix.length];
        for (int c = 0; c < rightMatrix.length; c++) {

            double[] rightVector = rightMatrix[c];
            double dotProduct = dot(leftVector, rightVector);

            double d1 = 0.0d;
            for (int i = 0; i < leftVector.length; i++) {
                d1 += Math.pow(leftVector[i], 2);
            }

            double d2 = 0.0d;
            for (int i = 0; i < rightVector.length; i++) {
                d2 += Math.pow(rightVector[i], 2);
            }
            double cosineSimilarity = dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
            cosineDistanceVector[c] = 1-cosineSimilarity;
        }
        return cosineDistanceVector;
    }


    /**
     * Computes the dot product of two vectors.  both vector must have the same length
     * @param leftVector left vector
     * @param rightVector right vector
     * @return the dot product
     */
    private double dot(double[] leftVector, double[] rightVector) {
        double dotProduct = 0;
        for (int i = 0; i < leftVector.length; i++) {
            dotProduct += leftVector[i] * rightVector[i];
        }
        return dotProduct;
    }

    /**
     * print Cosine Dsitance Matrix
     * @param file root to print in
     */
    public void print(String file) {
        try {
            PrintWriter pw = new PrintWriter(file, "UTF-8");
            int k=0;
            pw.print(CosineDistance.get(k));
            for (k=1; k<CosineDistance.size(); k++) {
                if(xindex.get(k-1)==xindex.get(k)) {
                    pw.print(",");
                    pw.print(CosineDistance.get(k));
                }else{
                    pw.println();
                    pw.print(CosineDistance.get(k));
                }
            }
            pw.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public double getMinDist() {
        double minCD = Collections.min(CosineDistance);
        return(minCD);
    }
    public ArrayList getMinPair() {
        double minCD =  getMinDist();
        ArrayList<Integer> minPair= new ArrayList<Integer>(2);
        int minx= xindex.get(CosineDistance.indexOf(minCD));
        int miny= yindex.get(CosineDistance.indexOf(minCD));
        minPair.add(0, minx);
        minPair.add(1, miny);
        return(minPair);
    }

    public void alterMatrix(int c1,int c2, double value) {
        for (int k = 0; k < CosineDistance.size(); k++) {
            if ((xindex.get(k) == c1) & (yindex.get(k) == c2)) {
                CosineDistance.set(k, 1.0);
            }
        }
    }

    public void removeRows(ArrayList<Integer> keys){
        for (int k = 0; k < CosineDistance.size(); k++) {
            if((keys.contains(xindex.get(k))) | (keys.contains(yindex.get(k))))
                    CosineDistance.set(k, 1.0);
            }
    }

    public void removeRow(ArrayList<Integer> keys){
        for (int k = 0; k < CosineDistance.size(); k++) {
            if((keys.contains(xindex.get(k))) & (keys.contains(yindex.get(k))))
                CosineDistance.set(k, 1.0);
        }
    }

    public void addRow(double[] CosineDistanceVector, int key, ArrayList<Integer> keys) {
        for (int j = 0; j < CosineDistanceVector.length; j++) {
            if (key != keys.get(j)) {
                CosineDistance.add(CosineDistanceVector[j]);
                xindex.add(key);
                yindex.add(keys.get(j));
            }
        }
    }
    public double leftSize(){
        return Collections.min(CosineDistance);
    }

}
