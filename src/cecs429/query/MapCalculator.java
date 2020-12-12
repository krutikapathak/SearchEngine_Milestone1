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
				for (Integer p : qrelDocs) {
					//System.out.println(p);
				}
				parseQuery(directory, corpus, rankmode, qrelDocs, query, lineNo);
			}
                        System.out.println("=======================");
			mapCalc(map);
                        meanResponseTime();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void parseQuery(String directory, DocumentCorpus corpus, String rankmode, List<Integer> qrelDocs,
			String query, int lineNo) {
                double start = System.currentTimeMillis();
		query = query.trim();
		DiskPositionalIndex diskIndex = new DiskPositionalIndex();
		BooleanQueryParser bqp = new BooleanQueryParser();
		QueryComponent qc = bqp.parseQuery(query);

		RankedQuery rq = new RankedQuery(query, corpus.getCorpusSize(), rankmode);
		List<Posting> result = rq.getPostings(diskIndex, directory);
                System.out.println("=======================");
		System.out.println("Query at line number " + lineNo + " is \"" + query + "\"");
		for (Posting p : result) {
			//System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + " accumulator: "
					//+ p.getAccumulator());
		}
		Map<Integer, Double> relDocs = precisionRecallCalc(result, qrelDocs, corpus, lineNo);
                double stop = System.currentTimeMillis();
                double responseTime = stop - start;
                System.out.println("responsetime:" + responseTime/1000 + "seconds");
                responseTimeList.add(responseTime);
		int totalDocs = rq.getTotalDocs();
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
			int lineNo) {
		List<Integer> docs = new ArrayList<Integer>();
		Map<Integer, Double> prec = new HashMap<Integer, Double>();
                Map<Integer, Double> recallList = new HashMap<Integer, Double>();
		Map<Integer, Double> relDocs = new HashMap<Integer, Double>();

		for (Posting p : result) {
			int doc = Integer.parseInt(corpus.getDocument(p.getDocumentId()).getTitle().split(".json")[0]);
			docs.add(doc);
		}
		double numerator = 0;
		double precision = 0;
                double recall = 0;
                double totalRelevantDoc = qRel.size();
		int i = 0;
		int j = 0;
		
		for(i = 0; i < docs.size(); i++) {
			for(j = 0; j < qRel.size(); j++) {
				if (docs.get(i) < qRel.get(j)) {
					precision = (numerator / (i + 1));
                                        recall = numerator/totalRelevantDoc;
					verifyPrecList(docs, prec, precision, i);
                                        verifyRecallList(docs, recallList, recall, i);
					break;
				} else if (docs.get(i) > qRel.get(j)) {
					precision = (numerator / (i + 1));
                                        recall = numerator/totalRelevantDoc;
					verifyPrecList(docs, prec, precision, i);
                                        verifyRecallList(docs, recallList, recall, i);
				} else {
					numerator++;
					precision = (numerator / (i + 1));
                                        recall = numerator/totalRelevantDoc;
					verifyPrecList(docs, prec, precision, i);
                                        verifyRecallList(docs, recallList, recall, i);
					relDocs.put(docs.get(i), precision);
					break;
				}
			}
		}
		
//		for (Integer doc : prec.keySet()) {
//			System.out.println("Doc " + doc + " precision " + prec.get(doc));
//		}
		return relDocs;
	}

	private void verifyPrecList(List<Integer> docs, Map<Integer, Double> prec, double precision, int i) {
		if (prec.containsKey(docs.get(i))) {
			prec.replace(docs.get(i), precision);
		} else {
			prec.put(docs.get(i), precision);
		}
	}

        private void verifyRecallList(List<Integer> docs, Map<Integer, Double> recallList, double recall, int i) {
		if (recallList.containsKey(docs.get(i))) {
			recallList.replace(docs.get(i), recall);
		} else {
			recallList.put(docs.get(i), recall);
		}
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
		//System.out.println(totalDocs);
		double avgPrec = addPrec / totalDocs;

		map.put(lineNo, avgPrec);

		for (int i = 0; i < relDocIndex.size(); i++) {
			//System.out.println("Relevant: " + docs.get(relDocIndex.get(i)) + ".json at index " + relDocIndex.get(i));
		}
		System.out.println("Average precision for this query: " + avgPrec);
	}
        
        private void meanResponseTime() {
            int i;
            double totalResponseTime = 0;
            for(i= 0 ; i < responseTimeList.size(); i++){
                totalResponseTime += responseTimeList.get(i);
            }
            
            double meanResponseTime = totalResponseTime/ responseTimeList.size();
            double thoughtput = 1/meanResponseTime;
            System.out.println("Mean Response Time:" + meanResponseTime/1000 + " seconds");
            System.out.println("Throughtput:" + 1/(meanResponseTime/1000));
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
