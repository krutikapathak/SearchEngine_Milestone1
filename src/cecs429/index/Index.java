package cecs429.index;

import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
	/**
	 * Retrieves a list of docIds that contain the given term.
	 */
	List<Posting> getPostings(String term, String directory);
	
	/**
	 * Retrieves a list of Postings of documents and positions that contain the given term.
	 */
	List<Posting> getPostingsDocandPos(String term, String directory);
	
	/**
	 * A (sorted) list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
	
	/**
	 * A (sorted) list of all terms in the Biword index vocabulary.
	 */
	List<String> getBiwordVocabulary();
}