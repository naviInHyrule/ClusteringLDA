import gnu.trove.TIntArrayList;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Mariflor Vega
 * @author Lan Du
 * @version 1.0 Build Jul 17 2020
 */
public class Corpus implements Serializable {
	private ArrayList<Document> docList;
	private int wordTotal;

	public Corpus(){
		docList = new ArrayList<Document>();
	}

	public Corpus(ArrayList<Document> docList){
		this.docList = docList;
		for(int i = 0; i < docList.size(); i++)
			countEachDoc(docList.get(i));
	}

	public Corpus(String fileName, Vocabulary voc) {
		this();
		try {
			int docIndex = 0;
			FileReader freader = new FileReader(new File(fileName));
			BufferedReader breader = new BufferedReader(freader);
			String text = breader.readLine();
			while (text != null) {
				//System.out.println(text);
				Document doc = new Document(text, docIndex, voc);
				if(doc.size() > 0){
					docList.add(doc);
					docIndex +=1;
					countEachDoc(doc);
				}else{
					System.out.println(text);
				}
				text = breader.readLine();
			}
			breader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void countEachDoc(Document doc){
		wordTotal += doc.size();
	}

	public int numWords() {
		return wordTotal;
	}

	public int numDocs() {
		return docList.size();
	}


	/**
	 *
	 * @param docIndex
	 *            the index of document in a corpus.
	 * @return a document with specified index.
	 */
	public Document getDoc(int docIndex) {
		return docList.get(docIndex);
	}


}
