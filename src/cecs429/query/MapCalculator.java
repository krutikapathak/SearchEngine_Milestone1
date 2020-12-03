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
import cecs429.index.Index;
import cecs429.index.Posting;

/**
 * 
 * @author Krutika Pathak
 *
 */

public class MapCalculator implements QueryComponent {

	public MapCalculator() {

	}

	public List<Integer> relDocs(String directory, String query) {
		List<Integer> qrelDocs = new ArrayList<>();
		File fr = new File(directory + "/relevance/queries");
		Scanner scanner;
		try {
			scanner = new Scanner(fr);
			int lineNo = 0;
			String line = "";
			while (scanner.hasNextLine()) {
				lineNo++;
				final String lineFromFile = scanner.nextLine();
				if (lineFromFile.contains(query)) {
					// a match
					System.out.println("Found: " + query + " at line: " + lineNo);
					fr = new File(directory + "/relevance/qrel");
					Stream<String> lines;
					try {
						lines = Files.lines(Paths.get(directory + "/relevance/qrel"));
						line = lines.skip(lineNo - 1).findFirst().get();
						// System.out.println(line);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
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
				System.out.println(p);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return qrelDocs;
	}

	public Map<Integer, Double> precisionCalc(List<Posting> result, List<Integer> qRel, DocumentCorpus corpus) {
		List<Integer> docs = new ArrayList<Integer>();
		Map<Integer, Double> prec = new HashMap<Integer, Double>();

		for (Posting p : result) {
			int doc = Integer.parseInt(corpus.getDocument(p.getDocumentId()).getTitle().split(".json")[0]);
			docs.add(doc);
		}
		Collections.sort(docs);
		double numerator = 0;
		double precision = 0;
		int i = 0;
		int j = 0;
		while (i < docs.size() && j < qRel.size()) {
			if (docs.get(i) < qRel.get(j)) {
				precision = (numerator / (i + 1));
				prec.put(docs.get(i), precision);
				i++;
			} else if (docs.get(i) > qRel.get(j)) {
				j++;
			} else {
				numerator++;
				precision = (numerator / (i + 1));
				prec.put(docs.get(i), precision);
				i++;
				j++;
			}
		}
		return prec;
	}

	@Override
	public List<Posting> getPostings(Index index, String directory) {
		
		return null;
	}
}
