package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BiWordIndex implements Index {
	private HashMap<String, List<Integer>> BiwordVocab;

	public BiWordIndex() {
		BiwordVocab = new HashMap<>();
	}

	public void addBiwordTerm(String term, int documentId) {
		List<Integer> docList = BiwordVocab.get(term);

		// if list does not exist create it
		if (docList == null) {
			List<Integer> new_docList = new ArrayList<>();
			new_docList.add(documentId);
			BiwordVocab.put(term, new_docList);
		} else {
			// add if docID is not already in list
			if (docList.get(docList.size() - 1) < documentId) {
				docList.add(documentId);
				BiwordVocab.replace(term, docList);
			}
		}
	}

	@Override
	public List<Posting> getPostings(String term, String directory) {
		List<Posting> result = new ArrayList<>();
		DataInputStream din;
		DiskPositionalIndex disk = new DiskPositionalIndex();
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/Biwordpostings.bin"));
			int totalDocs = disk.seekByteLoc(term, din);
			int i = 0;
			do {
				int docIdGap = din.readInt();
				int prevDocId = 0;
				if (result.size() > 0) {
					prevDocId = result.get(i - 1).getDocumentId();
				}
				int docId = docIdGap + prevDocId;
				Posting p = new Posting(docId);
				result.add(p);
				i++;
			} while (i < totalDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> result = new ArrayList<>();
		List<Integer> docList = BiwordVocab.get(term);
		if (docList != null) {
			for (int i = 0; i < docList.size(); i++) {
				result.add(new Posting(docList.get(i)));
			}
		}
		return result;
	}

	@Override
	public List<Posting> getPostingsDocandPos(String term, String directory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVocabulary() {
		ArrayList<String> sortedBiwordVocab = new ArrayList<String>(BiwordVocab.keySet());
		Collections.sort(sortedBiwordVocab);
		return Collections.unmodifiableList(sortedBiwordVocab);
	}

	public void showVocab() {
		for (String word : BiwordVocab.keySet()) {
			System.out.println("Word : " + word + " DocList: " + BiwordVocab.get(word));
		}
	}
}
