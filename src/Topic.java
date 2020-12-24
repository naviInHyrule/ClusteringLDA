import java.util.HashMap;
import java.util.Map;
import java.util.Dictionary;
import java.util.Hashtable;
public class Topic {

    private  Map<String, Object> topic = new HashMap<String, Object>();

    public Topic(double[] phi, double alpha) {
        topic.put("distribution", phi);
        topic.put("alpha", alpha);

    }

}
