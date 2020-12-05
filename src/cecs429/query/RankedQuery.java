/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.weightVariant.Context;
import cecs429.weightVariant.DefaultWeighting;
import cecs429.weightVariant.OkapiBM25Weighting;
import cecs429.weightVariant.TfIdfWeighting;
import cecs429.weightVariant.WackyWeighting;

/**
 *
 * @author atandel
 */
public class RankedQuery implements QueryComponent {

	private List<String> mTerms = new ArrayList<>();
	private Queue<Double> largest = new PriorityQueue<>(100);
	private int corpusSize = 0;
	private int totalDoc = 0;
	private String strategy = "Default";
	private int totalDocs = 0;

	public RankedQuery(String terms, int totalDoc, String strag) {
		List<String> termList = Arrays.asList(terms.split(" "));
		for (String term : termList) {
			mTerms.add(term);
		}
		this.totalDoc = totalDoc;
		this.strategy = strag;
	}

	@Override
	public List<Posting> getPostings(Index index, String directory) {
		List<Posting> result = new ArrayList<Posting>();
		Map<Integer, Double> scoreMap = new HashMap<Integer, Double>();
		int k = 50;
		int N = totalDoc;
		double acc = 0.0;
		Context context;

		switch (strategy) {
		case "2":
			context = new Context(new TfIdfWeighting());
			break;
		case "3":
			context = new Context(new OkapiBM25Weighting());
			break;
		case "4":
			context = new Context(new WackyWeighting());
			break;
		default:
			context = new Context(new DefaultWeighting());
		}

		// for each term in query
		for (String term : mTerms) {
			List<Posting> termPosting = index.getPostingsDocandPos(term, directory);
			int dft = termPosting.size();
			double wqt = context.calculateWqt(N, dft);

			// for each document in posting
			for (Posting p : termPosting) {
				int docId = p.getDocumentId();
				List<Integer> termsPosition = p.getmPositionId();
				int tftd = termsPosition.size();

				double wdt = context.calculateWdt(totalDoc, docId, tftd, directory);

				acc = (wdt * wqt);

				if (scoreMap.containsKey(docId)) {
					double s = scoreMap.get(docId);
					s += acc;
					scoreMap.put(docId, s);
				} else {
					scoreMap.put(docId, acc);
				}
			}
		}

		// loop over key(docId)
		for (int docId : scoreMap.keySet()) {
			double accumulator = scoreMap.get(docId);
			if (accumulator > 0) {
				double ld = context.calculateLd(docId, directory);
				if (accumulator > 0) {
					accumulator = accumulator / ld;
					scoreMap.replace(docId, accumulator);
				}
			}
		}

		PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>((Map.Entry<Integer, Double> x,
				Map.Entry<Integer, Double> y) -> Double.compare(y.getValue(), x.getValue()));

		for (Map.Entry<Integer, Double> entry : scoreMap.entrySet()) {
			pq.offer(entry);
		}
		
		this.totalDocs = pq.size();

		// get top 50 elements from the heap
		int i = 0;
		while (pq.size() > 0 && i < 50) {
			Entry<Integer, Double> entry = pq.poll();
			Posting p = new Posting(entry.getKey(), entry.getValue());
			result.add(p);
			i++;
		}
		return result;
	}

	public int getTotalDocs() {
		return totalDocs;
	}
}
