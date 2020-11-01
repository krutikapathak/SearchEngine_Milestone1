package cecs429.index;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Krutika Pathak
 *
 */

public class DiskIndexWriter {

	public HashMap<String, Integer> writeIndex(Index index,List<Double> weights, String absolutePath) throws FileNotFoundException {
		HashMap<String, Integer> result = new HashMap<>();
		Integer bytePos = null;
		String term;
		DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(absolutePath + "/postings.bin"));
                DataOutputStream weightsOut = new DataOutputStream(new FileOutputStream(absolutePath + "/docWeights.bin"));
		try {
			List<String> vocab = index.getVocabulary();

			for (int i = 0; i < vocab.size(); i++) {
				List<Posting> postings = new ArrayList<>();
				term = vocab.get(i);
				postings = index.getPostings(term);
				int totalDocs = postings.size();
				// write total no of docs for the term to disk
				bytePos = dataOut.size();
				dataOut.writeInt(totalDocs);

				for (int j = 0; j < postings.size(); j++) {
					int docIdGap;
					if (j == 0) {
						docIdGap = postings.get(j).getDocumentId();
					} else {
						docIdGap = (postings.get(j).getDocumentId()) - (postings.get(j - 1).getDocumentId());
					}
					// write doc ID for the term to disk
					dataOut.writeInt(docIdGap);

					int termFreq = postings.get(j).getmPositionId().size();
//					System.out.println(postings.get(j).getmPositionId());
					// write term frequency for the doc ID to disk
					dataOut.writeInt(termFreq);

					List<Integer> positionList = postings.get(j).getmPositionId();

					for (int k = 0; k < positionList.size(); k++) {
						int posGap;
						if (k == 0) {
							posGap = positionList.get(k);
						} else {
							posGap = positionList.get(k) - positionList.get(k - 1);
						}
						// write position gap to disk
						dataOut.writeInt(posGap);
					}
				}
				result.put(term, bytePos);
			}
                        
                        for (int w = 0; w < weights.size(); w++) {
                            weightsOut.writeDouble(weights.get(w));
                        }
                        
			dataOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public HashMap<String, Integer> writeBiwordIndex(Index biwordIndex, String absolutePath)
			throws FileNotFoundException {
		HashMap<String, Integer> result = new HashMap<>();
		Integer bytePos = null;
		String term;
		DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(absolutePath + "/Biwordpostings.bin"));
		List<String> biwordVocab = biwordIndex.getVocabulary();
		try {
			for (int i = 0; i < biwordVocab.size(); i++) {
				List<Posting> postings = new ArrayList<>();
				term = biwordVocab.get(i);
				postings = biwordIndex.getPostings(term);
				int totalDocs = postings.size();

				bytePos = dataOut.size();
				dataOut.writeInt(totalDocs);

				for (int j = 0; j < postings.size(); j++) {
					int docIdGap;
					if (j == 0) {
						docIdGap = postings.get(j).getDocumentId();
					} else {
						docIdGap = (postings.get(j).getDocumentId()) - (postings.get(j - 1).getDocumentId());
					}
					// write doc ID for the term to disk
					dataOut.writeInt(docIdGap);
				}
				result.put(term, bytePos);
			}
			dataOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
