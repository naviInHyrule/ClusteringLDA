/**
 * @author Hanna Wallach
 * @version 1.0 Build Jul 17 2020
 */


public class Probability implements Comparable {

  public int index;
  public double prob;

  public Probability(int index, double prob) {
    this.index = index;
    this.prob = prob;
  }

  public final int compareTo(Object o) {
    if (prob > ((Probability) o).prob)
      return -1;
    else if (prob == ((Probability) o).prob)
      return 0;
    else
      return 1;
  }
}
