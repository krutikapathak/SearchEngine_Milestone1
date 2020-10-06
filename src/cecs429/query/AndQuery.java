package cecs429.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.Posting;

/**
 * An AndQuery composes other Query objects and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mChildren;

	public AndQuery(Iterable<QueryComponent> children) {
		mChildren = new ArrayList<>();
		for (QueryComponent q : children) {
			mChildren.add(q);
		}
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
		List<List<Posting>> tempList = new ArrayList<>();

		// TODO: program the merge for an AndQuery, by gathering the postings of the
		// composed QueryComponents and
		// intersecting the resulting postings.

		for (QueryComponent q : mChildren) {
			tempList.add(q.getPostings(index));
		}

		List<Posting> tempResult = tempList.get(0);
		int i = 1;
		do {
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

		while (i < first.size() && j < second.size()) {
			if (first.get(i).getDocumentId() == second.get(j).getDocumentId()) {
				tempResult.add(first.get(i));
				i++;
				j++;
			} else {
				while (i < first.size() && j < second.size()
						&& first.get(i).getDocumentId() != second.get(j).getDocumentId()) {
					if (first.get(i).getDocumentId() < second.get(j).getDocumentId()) {
						i++;
					} else if (first.get(i).getDocumentId() > second.get(j).getDocumentId()) {
						j++;
					}
				}
			}
		}
		return tempResult;
	}

	@Override
	public String toString() {
		return String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
