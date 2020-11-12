package cecs429.index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Krutika Pathak
 *
 */

public class DiskIndexWriter {

	public HashMap<String, Integer> writeIndex(Index index, List<Double> weights, String absolutePath)
			throws FileNotFoundException {
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
				postings = index.getPostings(term, absolutePath);
				int totalDocs = postings.size();
				// write total no of docs for the term to disk
				bytePos = dataOut.size();
				dataOut.writeInt(totalDocs);

				for (int j = 0; j < postings.size(); j++) {
					calcDocIdGap(dataOut, postings, j);

					int termFreq = postings.get(j).getmPositionId().size();
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
			weightsOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void calcDocIdGap(DataOutputStream dataOut, List<Posting> postings, int j) throws IOException {
		int docIdGap;
		if (j == 0) {
			docIdGap = postings.get(j).getDocumentId();
		} else {
			docIdGap = (postings.get(j).getDocumentId()) - (postings.get(j - 1).getDocumentId());
		}
		// write doc ID for the term to disk
		dataOut.writeInt(docIdGap);
	}

	public HashMap<String, Integer> writeBiwordSoundexIndex(Index index, String absolutePath, String processorType)
			throws FileNotFoundException {
		HashMap<String, Integer> result = new HashMap<>();
		DataOutputStream dataOut;
		List<String> vocab;
		dataOut = getDout(absolutePath, processorType);
		vocab = getVocab(index, processorType);
		try {
			result = createSoundexBiwordBin(index, dataOut, vocab, processorType);
			dataOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private DataOutputStream getDout(String absolutePath, String processorType) throws FileNotFoundException {
		if (processorType.equalsIgnoreCase("advance"))
			return new DataOutputStream(new FileOutputStream(absolutePath + "/Biwordpostings.bin"));
		else
			return new DataOutputStream(new FileOutputStream(absolutePath + "/Soundexpostings.bin"));
	}

	private List<String> getVocab(Index index, String processorType) {
		if (processorType.equalsIgnoreCase("advance"))
			return index.getBiwordVocabulary();
		else
			return index.getVocabulary();
	}

	private HashMap<String, Integer> createSoundexBiwordBin(Index index, DataOutputStream dataOut, List<String> vocab,
			String processorType) throws IOException {
		Integer bytePos;
		String term;
		HashMap<String, Integer> result = new HashMap<>();
		for (int i = 0; i < vocab.size(); i++) {
			List<Posting> postings = new ArrayList<>();
			term = vocab.get(i);
			postings = getSoundexBiwordPostings(index, processorType, term);
			int totalDocs = postings.size();

			bytePos = dataOut.size();
			dataOut.writeInt(totalDocs);

			for (int j = 0; j < postings.size(); j++) {
				calcDocIdGap(dataOut, postings, j);
			}
			result.put(term, bytePos);
		}
		return result;
	}

	private List<Posting> getSoundexBiwordPostings(Index index, String processorType, String term) {
		if (processorType.equalsIgnoreCase("soundex"))
			return ((SoundexIndex) index).getSoundexPostings(term, "");
		else
			return index.getPostings(term, "");
	}

	public double getDocWeight(int docId, String directory) {
		DataInputStream din;
		double docWeight = 0;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
			din.skipBytes(docId * (8 * 4));
			docWeight = din.readDouble();
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docWeight;
	}

	public double getdocLength(int docId, String directory) {
		DataInputStream din;
		double docWeight = 0;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
			din.skipBytes((docId * (8 * 4)) + 8);
			docWeight = din.readDouble();
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docWeight;
	}

	public double getbyteSize(int docId, String directory) {
		DataInputStream din;
		double docWeight = 0;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
			din.skipBytes((docId * (8 * 4)) + 16);
			docWeight = din.readDouble();
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docWeight;
	}

	public double getavgtftd(int docId, String directory) {
		DataInputStream din;
		double docWeight = 0;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
			din.skipBytes((docId * (8 * 4)) + 24);
			docWeight = din.readDouble();
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docWeight;
	}

	public double getdocLengthA(int corpusSize, String directory) {
		DataInputStream din;
		double docWeight = 0;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
			din.skipBytes((corpusSize * 8 * 4));
			docWeight = din.readDouble();
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docWeight;
	}
}
