package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Krutika Pathak
 *
 */
public class PositionalInvertedIndex implements Index {
	private final HashMap<String, List<Posting>> refinedVocab;
	private HashMap<String, List<Integer>> biwordVocab;

	public PositionalInvertedIndex() {
		refinedVocab = new HashMap<>();
		biwordVocab = new HashMap<>();
	}

	public void addTerm(String term, int documentId, int position) {
		List<Integer> pos = new ArrayList<>();
		List<Posting> docList = refinedVocab.get(term);
		// if list does not exist create it
		if (docList == null) {
			List<Posting> new_docList = new ArrayList<>();
			pos.add(position);
			Posting posting = new Posting(documentId, pos);
			new_docList.add(posting);
			refinedVocab.put(term, new_docList);
		} else {
			int lastIndex = docList.size() - 1;
			// add if docID is not already in list
			Posting existingPosting = docList.get(lastIndex);
			if (existingPosting.getDocumentId() == documentId) {
				List<Integer> latestPos = existingPosting.getmPositionId();
				latestPos.add(position);
				existingPosting.setmPositionId(latestPos);
				docList.remove(lastIndex);
				docList.add(lastIndex, existingPosting);
				refinedVocab.replace(term, docList);
			} else {
				// add the positions if doc ID already exists
				pos.add(position);
				Posting posting = new Posting(documentId, pos);
				docList.add(posting);
				refinedVocab.replace(term, docList);
			}
		}
	}
	
	public void addBiwordTerm(String term, int documentId) {
		List<Integer> docList = biwordVocab.get(term);

		// if list does not exist create it
		if (docList == null) {
			List<Integer> new_docList = new ArrayList<>();
			new_docList.add(documentId);
			biwordVocab.put(term, new_docList);
		} else {
			// add if docID is not already in list
			if (docList.get(docList.size() - 1) < documentId) {
				docList.add(documentId);
				biwordVocab.replace(term, docList);
			}
		}
	}

	public List<Posting> getBiwordPostings(String term) {
		List<Posting> result = new ArrayList<>();
		List<Integer> docList = biwordVocab.get(term);
		if (docList != null) {
			for (int i = 0; i < docList.size(); i++) {
				result.add(new Posting(docList.get(i)));
			}
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(String term) {
		// Process query and fetch the postings
		List<Posting> result = new ArrayList<>();
		List<Posting> docList = refinedVocab.get(term);
		if (docList != null) {
			for (int i = 0; i < docList.size(); i++) {
				result.add(docList.get(i));
			}
		}
		return result;
	}
	
	@Override
	public List<String> getBiwordVocabulary() {
		ArrayList<String> sortedBiwordVocab = new ArrayList<String>(biwordVocab.keySet());
		Collections.sort(sortedBiwordVocab);
		return Collections.unmodifiableList(sortedBiwordVocab);
	}

	@Override
	public List<String> getVocabulary() {
		ArrayList<String> sortedVocab = new ArrayList<String>(refinedVocab.keySet());
		Collections.sort(sortedVocab);
		return Collections.unmodifiableList(sortedVocab);
	}

	@Override
	public List<Posting> getPostingsDocandPos(String term, String directory) {
		return null;
	}

	@Override
	public List<Posting> getPostings(String term, String directory) {
		return null;
	}
}
