package cecs429.index;

import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private double mAccumulator;
	private List<Integer> mPositionId;

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public Posting(int documentId, List<Integer> positionId) {
		mDocumentId = documentId;
		mPositionId = positionId;
	}

	public Posting(int documentId, double accumulator) {
		mDocumentId = documentId;
		mAccumulator = accumulator;
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getmPositionId() {
		return mPositionId;
	}

	public void setmPositionId(List<Integer> positionId) {
		mPositionId = positionId;
	}

	public double getAccumulator() {
		return mAccumulator;
	}
}
