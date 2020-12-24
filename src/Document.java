import gnu.trove.TIntArrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Mariflor Vega
 * @author Lan Du
 * @version 1.0 Build Jul 17 2020
 */
public class Document implements Serializable {
	private final int docIndex;
	private final String text;
	private TIntArrayList words;

	/**
	 * Build a text passage.
	 *
	 * @param text
	 *            the original text
	 * @param docIndex
	 *            the index of the text passage in the document
	 * @param voc
	 *            the vocabulary
	 */


    public Document(String text, int docIndex, Vocabulary voc) {
		this.text = text;
		this.docIndex = docIndex;
		words = new TIntArrayList();
		String[] tokens = text.split("\\|");
		for (int i = 0; i < tokens.length; i++) {
			//System.out.println(tokens[i]);
			String token = tokens[i];
			if (token.length() >= 1 && voc.contains(token)) {
				words.add(voc.getTypeIndex(token).intValue());
			}
		}
	}

	/**
	 *
	 * @param words an int arraylist of word indices
	 * @param docIndex the passage index
	 * @param text
	 */

	public Document(TIntArrayList words, int docIndex, String text) {
		this.docIndex = docIndex;
		this.text = text;
		this.words = words;
	}

	/**
	 * 
	 * @return the text content.
	 */
	public String text() {
		return text;
	}

	/**
	 * 
	 * @return the size of the tokenized text passage.
	 */
	public int size() {
		return words.size();
	}

	/**
	 * Return the index of text passage in a document.
	 * @return 
	 */
	public int getDocIndex() {
		return docIndex;
	}

	/**
	 * Get the word in the specified position in the text.
	 * 
	 * @param n
	 *            the word position in a text passage
	 * @return the type index
	 */
	public int getWord(int n) {
		return words.get(n);
	}

	/**
	 * 
	 * @return the hash code of this text passage.
	 */
	public int getHashCode() {
		return this.hashCode();
	}


}
