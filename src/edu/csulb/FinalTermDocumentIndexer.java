package edu.csulb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SoundexIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.GetTokenProcessorFactory;
import cecs429.text.TokenProcessor;

public class FinalTermDocumentIndexer {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Gson gson = new Gson();
		System.out.println("Enter a directory/corpus to index");
		String dir = sc.nextLine();
		String soundexDir = "/Users/krutikapathak/eclipse-workspace/Milestone1/mlb-articles-4000/";
		// "/Users/krutikapathak/eclipse-workspace/Milestone1/articles";
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(dir).toAbsolutePath(), getExtension(dir));
		DocumentCorpus corpus1 = DirectoryCorpus.loadTextDirectory(Paths.get(soundexDir).toAbsolutePath(), getExtension(soundexDir));
		long Start = System.currentTimeMillis();
		Index index = indexCorpus(corpus);
		Index soundexIndex = indexAuthorCorpus(corpus1);
		long Stop = System.currentTimeMillis();
		long timeTaken = Stop - Start;
		System.out.println("Indexing time: " + timeTaken);

		String query = "";
		String[] splittedString;
		System.out.println("Enter a word to search");
		query = sc.nextLine();
		while (!query.equalsIgnoreCase("quit")) {
			BooleanQueryParser bqp = new BooleanQueryParser();
			QueryComponent qc = bqp.parseQuery(query);
			if (query.startsWith(":")) {
				splittedString = query.split(" ");
				switch (splittedString[0]) {
				case ":q":
					System.out.println("Program ended!");
					System.exit(0);
					break;

				case ":stem":
					System.out.println("Your stemmed word is:" + AdvanceTokenProcessor.stemWord(splittedString[1]));
					break;

				case ":index": {
					corpus = DirectoryCorpus.loadTextDirectory(Paths.get(splittedString[1]).toAbsolutePath(), ".json");
					Start = System.currentTimeMillis();
					index = indexCorpus(corpus);
					Stop = System.currentTimeMillis();
					timeTaken = Stop - Start;
					System.out.println("Indexing time: " + timeTaken);
					break;
				}

				case ":vocab": {
					List<String> vocab = index.getVocabulary();
					int limit = 0;
					if (vocab.size() < 1000) {
						limit = vocab.size();
					} else
						limit = 1000;
					for (int i = 0; i < limit; i++) {
						System.out.println(vocab.get(i));
					}
					System.out.println("Total number of words in vocabulary- " + vocab.size());
					break;
				}

				case ":author": {
					for (Posting p : soundexIndex.getPostings(splittedString[1])) {

						String docName = corpus1.getDocument(p.getDocumentId()).getTitle();
						System.out.println("Document " + docName);
						Reader reader;
						try {
							reader = new FileReader(soundexDir + docName);
							JsonObject doc = gson.fromJson(reader, JsonObject.class);
							System.out.println("author: " + doc.get("author").getAsString());
							System.out.println(" ");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				}
			} else {
				List<Posting> result = qc.getPostings(index);
				for (Posting p : result) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
				System.out.println("Word found in " + result.size() + " documents!!");
//			System.out.println("Enter a word to search");
			}
			query = sc.nextLine();
		}
		sc.close();
	}

	public static Index indexCorpus(DocumentCorpus corpus) {
		EnglishTokenStream tokens = null;
		AdvanceTokenProcessor processor = new AdvanceTokenProcessor();
		Reader content;
		Iterable<Document> list = corpus.getDocuments();

		PositionalInvertedIndex docindex = new PositionalInvertedIndex();
		try {
			for (Document d : list) {
				int i = 0;
				content = d.getContent();
				tokens = new EnglishTokenStream(content);
				Iterable<String> token = tokens.getTokens();

				for (String word : token) {
					List<String> words = (List<String>) processor.processToken(word);
					for (String term : words) {
						docindex.addTerm(term, d.getId(), i);
						i = i + 1;
					}
				}
				content.close();
			}
			return docindex;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
		return null;
	}

	public static Index indexAuthorCorpus(DocumentCorpus corpus1) {
		EnglishTokenStream tokens = null;
		GetTokenProcessorFactory processorFactory = new GetTokenProcessorFactory();
		TokenProcessor processor = processorFactory.GetTokenProcessor("SOUNDEX");
		Reader content;
		Iterable<Document> list = corpus1.getDocuments();

		SoundexIndex docindex = new SoundexIndex();

		try {
			for (Document d : list) {

				content = d.getContent();
				Gson gson = new Gson();
				JsonObject doc = gson.fromJson(content, JsonObject.class);

				String authorName = doc.get("author").getAsString();

				if (!authorName.isEmpty()) {
					List<String> words = (List<String>) processor.processToken(authorName);
					for (String word : words) {
						docindex.addTerm(word, d.getId());
					}
				}
				content.close();
			}
			return docindex;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
		return null;
	}
	
	public static String getExtension(String directory) {
		String fileName = new File(directory).listFiles()[0].getName();
		return fileName.substring(fileName.lastIndexOf('.'));
	}
}
