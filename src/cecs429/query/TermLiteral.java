package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
//		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(mTerm);
//		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
//		mTerm = AdvanceTokenProcessor.stemWord(processedmTerm);
		return mTerm;
	}

	@Override
	public List<Posting> getPostings(Index index) {
//		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(mTerm);
//		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
//		mTerm = AdvanceTokenProcessor.stemWord(processedmTerm);
		return index.getPostings(mTerm);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
