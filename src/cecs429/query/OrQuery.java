package cecs429.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.Posting;

/**
 * An OrQuery composes other Query objects and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mChildren;

	public OrQuery(Iterable<QueryComponent> children) {
		mChildren = new ArrayList<>();
		for (QueryComponent q : children) {
			mChildren.add(q);
		}
	}

	@Override
	public List<Posting> getPostings(Index index, String directory) {

		List<Posting> result = new ArrayList<Posting>();
		List<List<Posting>> tempList = new ArrayList<>();

		// program the merge for an OrQuery, by gathering the postings of the
		// composed Query children and

		for (QueryComponent q : mChildren) {
			tempList.add(q.getPostings(index, directory));
		}

		List<Posting> tempResult = tempList.get(0);
		int i = 1;
		do {
			// Adding the final union list to the Result
			result.clear();
			result = tempResult(tempResult, tempList.get(i));
			i++;
			tempResult.clear();
			tempResult.addAll(result);
		} while (i < tempList.size());

		return result;

	}

	private List<Posting> tempResult(List<Posting> first, List<Posting> second) {
		int i = 0;
		int j = 0;
		List<Posting> tempResult = new ArrayList<Posting>();

		// unioning the resulting postings.
		while (i < first.size() && j < second.size()) {
			if (first.get(i).getDocumentId() == second.get(j).getDocumentId()) {
				tempResult.add(first.get(i));
				i++;
				j++;
			} else {
				while (i < first.size() && j < second.size()
						&& first.get(i).getDocumentId() != second.get(j).getDocumentId()) {
					if (first.get(i).getDocumentId() < second.get(j).getDocumentId()) {
						tempResult.add(first.get(i));
						i++;
					} else if (first.get(i).getDocumentId() > second.get(j).getDocumentId()) {
						tempResult.add(second.get(j));
						j++;
					}
				}
			}
		}
		while (i < first.size()) {
			tempResult.add(first.get(i));
			i++;
		}
		while (j < second.size()) {
			tempResult.add(second.get(j));
			j++;
		}
		return tempResult;
	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" + String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList())) + " )";
	}
}
