package cecs429.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Posting;

/**
 * 
 * @author Krutika Pathak
 *
 */

public class MapCalculator {

	private Map<Integer, Double> map = new HashMap<Integer, Double>();
	private List<Double> responseTimeList = new ArrayList<Double>();

	public void relDocs(String directory, DocumentCorpus corpus) {
		String rankmode = userInput();

		File fr = new File(directory + "/relevance/queries");
		Scanner scanner;
		try {
			scanner = new Scanner(fr);
			int lineNo = 0;
			String line = "";
			while (scanner.hasNextLine()) {
				List<Integer> qrelDocs = new ArrayList<>();
				lineNo++;
				String query = scanner.nextLine();
				fr = new File(directory + "/relevance/qrel");
				Stream<String> lines;
				try {
					lines = Files.lines(Paths.get(directory + "/relevance/qrel"));
					line = lines.skip(lineNo - 1).findFirst().get();
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] splitLine = line.split(" ");
				if (splitLine.length != 0) {
					for (int i = 0; i < splitLine.length; i++) {
						if (splitLine[i].isEmpty())
							continue;
						qrelDocs.add(Integer.parseInt(splitLine[i]));
					}
				}
				Collections.sort(qrelDocs);
//				for (int i = 0; i < 5; i++) {
				parseQuery(directory, corpus, rankmode, qrelDocs, query, lineNo);
				System.out.println("=======================");
//				}
				mapCalc(map);
				meanResponseTime();
//				break;
			}
		} catch (

		FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void parseQuery(String directory, DocumentCorpus corpus, String rankmode, List<Integer> qrelDocs,
			String query, int lineNo) {
		double start = System.currentTimeMillis();
		query = query.trim();
		DiskPositionalIndex diskIndex = new DiskPositionalIndex();

		RankedQuery rq = new RankedQuery(query, corpus.getCorpusSize(), rankmode);
		List<Posting> result = rq.getPostings(diskIndex, directory);
		System.out.println("=======================");
		System.out.println("Query at line number " + lineNo + " is \"" + query + "\"");
		for (Posting p : result) {
			System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + " accumulator: "
					+ p.getAccumulator());
		}
		int totalDocs = qrelDocs.size();
		Map<Integer, Double> relDocs = precisionRecallCalc(result, qrelDocs, corpus, lineNo, totalDocs);
		double stop = System.currentTimeMillis();
		double responseTime = stop - start;
		System.out.println("responsetime:" + responseTime / 1000 + "seconds");
		responseTimeList.add(responseTime);
		averagePrecision(result, relDocs, totalDocs, corpus, lineNo);
	}

	private String userInput() {
		Scanner sc = new Scanner(System.in);

		System.out.println("Select Weighting method:");
		System.out.println("1.Default");
		System.out.println("2.TF-IDF");
		System.out.println("3.OkapiBM25");
		System.out.println("4.Wacky");
		String rankmode = sc.nextLine();

		return rankmode;
	}

	private Map<Integer, Double> precisionRecallCalc(List<Posting> result, List<Integer> qRel, DocumentCorpus corpus,
			int lineNo, int totalDocs) {
		List<Integer> docs = new ArrayList<Integer>();
		Map<Integer, Double> relDocs = new HashMap<Integer, Double>();
		ArrayList<Integer> docTitle = new ArrayList<Integer>();
		ArrayList<Double> precisionList = new ArrayList<>();
		ArrayList<Double> recList = new ArrayList<>();

		for (Posting p : result) {
			int doc = Integer.parseInt(corpus.getDocument(p.getDocumentId()).getTitle().split(".json")[0]);
			docs.add(doc);
		}
		double numerator = 0;
		double precision = 0;
		double recall = 0;
		int i = 0;
		int j = 0;
		System.out.println(totalDocs);

		for (i = 0; i < docs.size(); i++) {
			for (j = 0; j < qRel.size(); j++) {
				if (docs.get(i) < qRel.get(j)) {
					precision = (numerator / (i + 1));
					recall = numerator / totalDocs;
					precRecallList(docs, docTitle, precisionList, recList, precision, recall, i);
					break;
				} else if (docs.get(i) > qRel.get(j)) {
					precision = (numerator / (i + 1));
					recall = numerator / totalDocs;
					precRecallList(docs, docTitle, precisionList, recList, precision, recall, i);
				} else {
					numerator++;
					precision = (numerator / (i + 1));
					recall = numerator / totalDocs;
					relDocs.put(docs.get(i), precision);
					precRecallList(docs, docTitle, precisionList, recList, precision, recall, i);
					break;
				}
			}
		}
//		for (Double preci : precisionList) {
//			System.out.println("precision: " + preci);
//		}
//		for (Double rec : recList) {
//			System.out.println("recall: " + rec);
//		}
		return relDocs;
	}

	private void precRecallList(List<Integer> docs, ArrayList<Integer> docTitle, ArrayList<Double> precisionList,
			ArrayList<Double> recList, double precision, double recall, int i) {
		if (docTitle.contains(docs.get(i))) {
			precisionList.remove(i);
			recList.remove(i);
		} else {
			docTitle.add(docs.get(i));
		}
		precisionList.add(i, precision);
		recList.add(i, recall);
	}

	private void averagePrecision(List<Posting> result, Map<Integer, Double> relDocs, int totalDocs,
			DocumentCorpus corpus, int lineNo) {
		double addPrec = 0.0;
		List<Integer> docs = new ArrayList<Integer>();
		List<Integer> relDocIndex = new ArrayList<Integer>();

		for (Posting p : result) {
			int doc = Integer.parseInt(corpus.getDocument(p.getDocumentId()).getTitle().split(".json")[0]);
			docs.add(doc);
		}

		for (Integer doc : relDocs.keySet()) {
			relDocIndex.add(docs.indexOf(doc));
			addPrec += relDocs.get(doc);
		}
		Collections.sort(relDocIndex);
		double avgPrec = addPrec / totalDocs;

		map.put(lineNo, avgPrec);

		for (int i = 0; i < relDocIndex.size(); i++) {
			System.out.println(
					"Relevant: " + docs.get(relDocIndex.get(i)) + ".json at index " + (relDocIndex.get(i) + 1));
		}
		System.out.println("Average precision for this query: " + avgPrec);
	}

	private void meanResponseTime() {
		int i;
		double totalResponseTime = 0;
		for (i = 0; i < responseTimeList.size(); i++) {
			totalResponseTime += responseTimeList.get(i);
		}

		double meanResponseTime = totalResponseTime / responseTimeList.size();
		double throughput = 1 / meanResponseTime;
		System.out.println("Mean Response Time:" + meanResponseTime / 1000 + " seconds");
		System.out.println("Throughput:" + 1 / (meanResponseTime / 1000));
	}

	private void mapCalc(Map<Integer, Double> avgPrec) {
		double map = 0;
		int totalQueries = avgPrec.size();
		double addAvgPrec = 0.0;

		for (Integer queryNo : avgPrec.keySet()) {
			addAvgPrec += avgPrec.get(queryNo);
		}

		map = addAvgPrec / totalQueries;

		System.out.println("Mean average precision for " + totalQueries + " queries: " + map);
	}
}
