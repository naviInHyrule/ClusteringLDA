/**
 * @author Mariflor Vega
 * @author Lan Du
 * @version 1.0 Build Jul 17 2020
 */
import org.apache.commons.collections.bidimap.TreeBidiMap;

import java.io.*;

public class Vocabulary implements Serializable {
	private TreeBidiMap types2Index;
	private FileWriter vocWriter;
	/**
	 * 
	 */
	public Vocabulary() {
		types2Index = new TreeBidiMap();
	}

	/**
	 * 
	 * @return the size of the vocabulary.
	 */
	public int size() {
		return types2Index.size();
	}

	/**
	 * 
	 * @param type
	 *            token type
	 * @return
	 */
	public boolean contains(String type) {
		return types2Index.containsKey(type);
	}

	/**
	 * 
	 * @param type
	 * @return the type index in the vocabulary.
	 */
	public Integer getTypeIndex(String type) {
		return (Integer) types2Index.get(type);
	}

	/**
	 * 
	 * @param index
	 *            the type index
	 * @return the string of the type
	 */
	public String getType(Integer index) {
		return (String) types2Index.getKey(index);
	}

	/**
	 * Read a vocabulary from a file, which has the following format:
	 * 
	 * "String:Integer"
	 * 
	 * @param fileName
	 */
	public void readVocabulary(String fileName) {
		try {
			FileReader freader = new FileReader(new File(fileName));
			BufferedReader breader = new BufferedReader(freader);
			String str = breader.readLine();
			while (str != null) {
				String[] strs = str.split(" ");
				types2Index.put(strs[0], new Integer(strs[1]));
				str = breader.readLine();
			}
			breader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Save the vocabulary in a file with the following format: 
	 * 
	 * "String:Integer"
	 * 
	 * @param fileName
	 */

    public void writeVocabulary(String fileName) throws IOException
    {
        String newLine = System.getProperty("line.separator");
        vocWriter = new FileWriter(fileName + File.separator+ "vocabulary.dat");

        for (Object key : types2Index.keySet()) {
            String type = (String) key;
            Integer index = (Integer) types2Index.get(key);
            vocWriter.write(index  + ":" + type + newLine);
        }

        vocWriter.flush();
        vocWriter.close();
    }

    public void save(String fileName) {
		try {
			FileWriter fwriter = new FileWriter(new File(fileName));
			BufferedWriter bwriter = new BufferedWriter(fwriter);
			for (Object key : types2Index.keySet()) {
				String type = (String) key;
				Integer index = (Integer) types2Index.get(key);
				bwriter.write(type + ":" + index);
				bwriter.newLine();
			}
			bwriter.flush();
			bwriter.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	



	/**
	 * Clear the vocabulary.
	 */
	public void clear() {
		types2Index.clear();
	}
}
