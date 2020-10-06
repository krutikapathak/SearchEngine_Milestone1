package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cecs429.text.AdvanceTokenProcessor;

/**
 * 
 * @author Krutika Pathak
 *
 */
public class PositionalInvertedIndex implements Index {
	private final HashMap<String, List<Posting>> refinedVocab;

	public PositionalInvertedIndex() {
		refinedVocab = new HashMap<>();
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
				pos.add(position);
				Posting posting = new Posting(documentId, pos);
				docList.add(posting);
				refinedVocab.replace(term, docList);
			}
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(term);
		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
		term = AdvanceTokenProcessor.stemWord(processedmTerm);
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
	public List<String> getVocabulary() {
		ArrayList<String> sortedVocab = new ArrayList<String>(refinedVocab.keySet());
		Collections.sort(sortedVocab);
		return Collections.unmodifiableList(sortedVocab);
	}
}
