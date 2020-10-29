package edu.csulb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.BiWordIndex;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
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

	private static final String url = "jdbc:mysql://localhost:3306/Milestone2?serverTimezone=UTC";
	private static final String user = "root";
	private static final String password = "password";

	public static void main(String[] args) throws FileNotFoundException {

		// Code to run the project on console. Use JSP files to run on web.
		Scanner sc = new Scanner(System.in);
		Gson gson = new Gson();

		System.out.println("Enter a directory/corpus to index");
		String dir = sc.nextLine();
		String soundexDir = "mlb-articles-4000";
		// /Users/krutikapathak/eclipse-workspace/SEHomework4/chapters
//		/Users/krutikapathak/eclipse-workspace/Milestone1/test1

		File indexDir = new File(dir + "/index");
		indexDir.mkdirs();

		// Index corpus for Boolean Queries
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(dir).toAbsolutePath(), getExtension(dir));
		// Index corpus for Soundex
		DocumentCorpus corpusSoundex = DirectoryCorpus.loadTextDirectory(Paths.get(soundexDir).toAbsolutePath(),
				getExtension(soundexDir));

		DiskIndexWriter diskIndex = new DiskIndexWriter();

		long Start = System.currentTimeMillis();
		
		Index index = indexCorpus(corpus, "advance");
		Index biwordIndex = biWordIndexCorpus(corpus, "advance");
		Index soundexIndex = indexCorpus(corpusSoundex, "soundex");
		
		long Stop = System.currentTimeMillis();
		long timeTaken = Stop - Start;
		System.out.println("Indexing time: " + timeTaken);
		
		insertToDB(indexDir, diskIndex, index, biwordIndex);

		String query = "";
		String[] splittedString;
		System.out.println("Enter a word to search");
		query = sc.nextLine();

		while (!query.equalsIgnoreCase("quit")) {
			DiskPositionalIndex docIndex = new DiskPositionalIndex();
			BiWordIndex bwIndex = new BiWordIndex();
			BooleanQueryParser bqp = new BooleanQueryParser();
			QueryComponent qc = bqp.parseQuery(query);
			// Special Queries
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
					index = indexCorpus(corpus, "advance");
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

						String docName = corpusSoundex.getDocument(p.getDocumentId()).getTitle();
						System.out.println("Document " + docName);
						Reader reader;
						reader = corpusSoundex.getDocument(p.getDocumentId()).getContent();
						JsonObject doc = gson.fromJson(reader, JsonObject.class);
						System.out.println("author: " + doc.get("author").getAsString());
						System.out.println(" ");
					}
				}
				}
			} else {
				List<Posting> result = qc.getPostings(docIndex, dir);
//				List<Posting> result = qc.getPostings(index);
				for (Posting p : result) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
				System.out.println("Word found in " + result.size() + " documents!!");
			}
			System.out.println("Enter a word to search");
			query = sc.nextLine();
		}
		sc.close();
	}

	private static void insertToDB(File indexDir, DiskIndexWriter diskIndex, Index index, Index biwordIndex)
			throws FileNotFoundException {
		HashMap<String, Integer> bytePositionsList = diskIndex.writeIndex(index, indexDir.getAbsolutePath());
		HashMap<String, Integer> BiwordBytePosList = diskIndex.writeBiwordIndex(biwordIndex,
				indexDir.getAbsolutePath());

		try {
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);

			PreparedStatement preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS Milestone2.positions");
			preparedStatement.execute();

			preparedStatement = conn.prepareStatement("CREATE TABLE Milestone2.positions ("
					+ "  id INT NOT NULL AUTO_INCREMENT," + "  Term VARCHAR(1500) NOT NULL,"
					+ "  BytePosition INT NOT NULL," + "  PRIMARY KEY (id));");
			preparedStatement.execute();

			preparedStatement = conn.prepareStatement("insert into Milestone2.positions values (default, ?, ?)");

			for (String entry : bytePositionsList.keySet()) {
				Integer byteLoc = bytePositionsList.get(entry);
				System.out.println("Key: " + entry + ", Value: " + byteLoc);
				preparedStatement.setString(1, entry);
				preparedStatement.setInt(2, byteLoc);
				preparedStatement.execute();
			}
			for (String entry : BiwordBytePosList.keySet()) {
				Integer byteLoc = BiwordBytePosList.get(entry);
				System.out.println("Key: " + entry + ", Value: " + byteLoc);
				preparedStatement.setString(1, entry);
				preparedStatement.setInt(2, byteLoc);
				preparedStatement.execute();
			}
			System.out.println("Done");
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Index indexCorpus(DocumentCorpus corpus, String processorType) {
		EnglishTokenStream bodyTokens = null;
		EnglishTokenStream titleTokens = null;
		GetTokenProcessorFactory processorFactory = new GetTokenProcessorFactory();
		TokenProcessor processor = processorFactory.GetTokenProcessor(processorType);

		Reader content;
		Iterable<Document> list = corpus.getDocuments();

		if (processorType == "soundex") {
			// indexing for soundex
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

		} else {
			PositionalInvertedIndex docindex = new PositionalInvertedIndex();

			try {
				for (Document d : list) {
					int i = 0;
					content = d.getContent();
					Gson gson = new Gson();
					try {
						// indexing .json file's body and title for positional inverted
						JsonObject doc = gson.fromJson(content, JsonObject.class);
						JsonElement bodycontent = doc.get("body");
						JsonElement titlecontent = doc.get("title");
						Reader bodyReader = new StringReader(bodycontent.toString());
						Reader titleReader = new StringReader(titlecontent.toString());

						bodyTokens = new EnglishTokenStream(bodyReader);
						titleTokens = new EnglishTokenStream(titleReader);

						Iterable<String> bodytoken = bodyTokens.getTokens();
						Iterable<String> titletoken = titleTokens.getTokens();

						for (String word : bodytoken) {
							List<String> words = (List<String>) processor.processToken(word);
							for (String term : words) {
								docindex.addTerm(term, d.getId(), i);
								i = i + 1;
							}
						}
						for (String word : titletoken) {
							List<String> words = (List<String>) processor.processToken(word);
							for (String term : words) {
								docindex.addTerm(term, d.getId(), i);
								i = i + 1;
							}
						}
					} catch (JsonSyntaxException e) {
						// indexing .txt file content for positional inverted
						Reader txtReader = d.getContent();
						EnglishTokenStream txtTokens = new EnglishTokenStream(txtReader);
						Iterable<String> txtToken = txtTokens.getTokens();

						for (String word : txtToken) {
							List<String> words = (List<String>) processor.processToken(word);
							for (String term : words) {
								docindex.addTerm(term, d.getId(), i);
								i = i + 1;
							}
						}
						txtReader.close();
					}
					content.close();
				}
				return docindex;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ex.toString());
			}
		}
		return null;
	}

	public static Index biWordIndexCorpus(DocumentCorpus corpus, String processorType) {
		EnglishTokenStream bodyTokens = null;
		EnglishTokenStream titleTokens = null;
		GetTokenProcessorFactory processorFactory = new GetTokenProcessorFactory();

		Reader content;
		Iterable<Document> list = corpus.getDocuments();
		BiWordIndex biwordIndex = new BiWordIndex();

		try {
			for (Document d : list) {
				int i = 0;
				content = d.getContent();
				Gson gson = new Gson();
				try {
					// indexing .json file's body and title for positional inverted
					JsonObject doc = gson.fromJson(content, JsonObject.class);
					JsonElement bodycontent = doc.get("body");
					JsonElement titlecontent = doc.get("title");
					Reader bodyReader = new StringReader(bodycontent.toString());
					Reader titleReader = new StringReader(titlecontent.toString());

					bodyTokens = new EnglishTokenStream(bodyReader);
					titleTokens = new EnglishTokenStream(titleReader);

					Iterable<String> bodytoken = bodyTokens.getTokens();
					Iterable<String> titletoken = titleTokens.getTokens();

					System.out.println("Body starts");
					addBiWord(processorType, processorFactory, biwordIndex, d, bodytoken);
					System.out.println("Title starts");
					addBiWord(processorType, processorFactory, biwordIndex, d, titletoken);
				} catch (JsonSyntaxException e) {
					// indexing .txt file content for positional inverted
					Reader txtReader = d.getContent();
					EnglishTokenStream txtTokens = new EnglishTokenStream(txtReader);
					Iterable<String> txtToken = txtTokens.getTokens();

					addBiWord(processorType, processorFactory, biwordIndex, d, txtToken);
					txtReader.close();
				}
				content.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		biwordIndex.showVocab();
		return biwordIndex;
	}

	private static void addBiWord(String processorType, GetTokenProcessorFactory processorFactory,
			BiWordIndex biwordIndex, Document doc, Iterable<String> tokens) {
		TokenProcessor processor = processorFactory.GetTokenProcessor(processorType);
		String firstWord = null;
		String secondWord = null;
		for (String word : tokens) {
			List<String> words = (List<String>) processor.processToken(word);
			for (String term : words) {
				if (term.isBlank())
					continue;
				if (firstWord == null && secondWord == null) {
					firstWord = term;
					continue;
				}
				if (firstWord != null && secondWord == null) {
					secondWord = term;
				}
				term = (firstWord + " " + secondWord);
				System.out.println("Body: " + term);
				biwordIndex.addBiwordTerm(term, doc.getId());
				firstWord = secondWord;
				secondWord = null;
			}
		}
	}

	public static String getExtension(String directory) {
		String fileName = new File(directory).listFiles()[0].getName();
		return fileName.substring(fileName.lastIndexOf('.'));
	}
}
