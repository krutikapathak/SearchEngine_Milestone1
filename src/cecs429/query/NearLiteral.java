/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;

/**
 *
 * @author atandel
 */
public class NearLiteral implements QueryComponent {
	// The list of individual terms in the Near Query.
	private String firstTerm;
	private String lastTerm;
	private Integer distance;

	/**
	 * Constructs a NearLiteral with the given individual phrase terms.
	 */
	public NearLiteral(String first, String last, int d) {
		firstTerm = Normalize(first);
		lastTerm = Normalize(last);
		distance = d;
	}

	private String Normalize(String term) {
		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(term);
		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
		String mTerm = AdvanceTokenProcessor.stemWord(processedmTerm);
		return mTerm;
	}

	@Override
	public List<Posting> getPostings(Index index) {

		List<Posting> result = new ArrayList<Posting>();

		List<Posting> firstPosting = index.getPostings(firstTerm);
		List<Posting> lastPosting = index.getPostings(lastTerm);

		result = tempResult(firstPosting, lastPosting);

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

				int a = 0, b = 0;
				while (a < positionListOne.size() && b < positionListTwo.size()) {

					if (positionListTwo.get(b) == positionListOne.get(a) + distance) {
						tempResult.add(docListTwo.get(j));
						break;
					} else if (positionListTwo.get(b) > positionListOne.get(a)) {
						a++;
					} else if (positionListTwo.get(b) < positionListOne.get(a)) {
						b++;
					} else if (positionListTwo.get(b) == positionListOne.get(a)) {
						b++;
					}

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
		return "\"" + String.join(" ", firstTerm + " " + lastTerm) + "\"";
	}

}
