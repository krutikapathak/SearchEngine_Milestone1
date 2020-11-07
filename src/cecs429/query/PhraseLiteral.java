package cecs429.query;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		for (String term : terms) {
			mTerms.add(term);
		}
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		List<String> termList = Arrays.asList(terms.split(" "));
		for (String term : termList) {
			mTerms.add(term);
		}
	}

	@Override
	public List<Posting> getPostings(Index index, String directory) {
		List<Posting> result = new ArrayList<Posting>();

		if (mTerms.size() != 2) {
			List<Posting> firstTerm = index.getPostingsDocandPos(mTerms.get(0), directory);
			int i = 1;
			do {
				result.clear();
				List<Posting> secondTerm = index.getPostingsDocandPos(mTerms.get(i), directory);
				result = tempResult(firstTerm, secondTerm);
				i++;
				firstTerm.clear();
				firstTerm.addAll(result);
			} while (i < mTerms.size());
		} else {
			DataInputStream din;
			DiskPositionalIndex disk = new DiskPositionalIndex();
			try {
				din = new DataInputStream(new FileInputStream(directory + "/index/Biwordpostings.bin"));
				String term = mTerms.get(0) + " " + mTerms.get(1);
				int totalDocs = disk.seekByteLoc(term, din);
				int i = 0;
				if (totalDocs != 0) {
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
				}
				din.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
		List<Posting> firstTerm = index.getPostings(mTerms.get(0));
		int i = 1;
		do {
			result.clear();
			List<Posting> secondTerm = index.getPostings(mTerms.get(i));
			result = tempResult(firstTerm, secondTerm);
			i++;
			firstTerm.clear();
			firstTerm.addAll(result);
		} while (i < mTerms.size());
		return result;
	}

	private List<Posting> tempResult(List<Posting> docListOne, List<Posting> docListTwo) {
		int i = 0;
		int j = 0;
		List<Posting> tempResult = new ArrayList<Posting>();

		while (i < docListOne.size() && j < docListTwo.size()) {
			if (docListOne.get(i).getDocumentId() == docListTwo.get(j).getDocumentId()) {
				List<Integer> positionListOne = docListOne.get(i).getmPositionId();
				List<Integer> positionListTwo = docListTwo.get(j).getmPositionId();
				List<Integer> tempPosition = new ArrayList<Integer>();
				int a = 0, b = 0;
				while (a < positionListOne.size() && b < positionListTwo.size()) {
					if (positionListTwo.get(b) == positionListOne.get(a) + 1) {
//						tempResult.add(docListTwo.get(j));
//						break;
						tempPosition.add(positionListTwo.get(b));
						a++;
						b++;
					} else if (positionListTwo.get(b) > positionListOne.get(a)) {
						a++;
					} else if (positionListTwo.get(b) < positionListOne.get(a)) {
						b++;
					} else if (positionListTwo.get(b) == positionListOne.get(a)) {
						b++;
					}
				}
				if (tempPosition.size() > 0) {
					Posting p = new Posting(docListTwo.get(j).getDocumentId(), tempPosition);
					tempResult.add(p);
				}
				i++;
				j++;
			} else {
				while (i < docListOne.size() && j < docListTwo.size()
						&& docListOne.get(i).getDocumentId() != docListTwo.get(j).getDocumentId()) {
					if (docListOne.get(i).getDocumentId() < docListTwo.get(j).getDocumentId()) {
						i++;
					} else if (docListOne.get(i).getDocumentId() > docListTwo.get(j).getDocumentId()) {
						j++;
					}
				}
			}
		}
		return tempResult;
	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
