package edu.csulb;

import static java.lang.Math.sqrt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SoundexIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.query.RankedQuery;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.GetTokenProcessorFactory;
import cecs429.text.TokenProcessor;

public class FinalTermDocumentIndexer {

	public static List<Double> weights = new ArrayList<>();

	private static final String url = "jdbc:mysql://localhost:3306/Milestone2?serverTimezone=UTC";
	private static final String user = "root";
	private static final String password = "password";

	public static void main(String[] args) throws FileNotFoundException {

		// Code to run the project on console. Use JSP files to run on web.
		Scanner sc = new Scanner(System.in);
		Gson gson = new Gson();

		System.out.println("Enter a query mode");
		System.out.println("1.Build Index");
		System.out.println("2.Query Index");
		String indexMode = sc.nextLine();

		System.out.println("Enter a directory/corpus to index");
		String dir = sc.nextLine(); // "/Users/krutikapathak/eclipse-workspace/SEHomework4/chapters";
		String soundexDir = "mlb-articles-4000";

		// Index corpus for Boolean Queries
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(dir).toAbsolutePath(), getExtension(dir));
		// Index corpus for Soundex
		DocumentCorpus corpusSoundex = DirectoryCorpus.loadTextDirectory(Paths.get(soundexDir).toAbsolutePath(),
				getExtension(soundexDir));

		DiskIndexWriter diskIndexWriter = new DiskIndexWriter();

		long Start = System.currentTimeMillis();
		Index index = indexCorpus(corpus, "advance", dir, "build");
		Index soundexIndex = indexCorpus(corpusSoundex, "soundex", soundexDir, indexMode);
		long Stop = System.currentTimeMillis();
		long timeTaken = Stop - Start;
		System.out.println("Indexing time: " + timeTaken);

		System.out.println("Enter a query mode");
		System.out.println("1.Boolean Query");
		System.out.println("2.Ranked Query");
		String mode = sc.nextLine();

		if ("1".equals(mode)) {
			System.out.println("You have selected boolean Query mode");
		} else {
			System.out.println("You have selected Ranked Query mode");
		}

		System.out.println("Select Weighting method:");
		System.out.println("1.Default");
		System.out.println("2.TF-IDF");
		System.out.println("3.OkapiBM25");
		System.out.println("4.Wacky");
		String rankmode = sc.nextLine();

		String query = "";
		String[] splittedString;

		System.out.println("Enter a word to search");
		query = sc.nextLine();

		while (!query.equalsIgnoreCase("quit")) {
			DiskPositionalIndex diskIndex = new DiskPositionalIndex();
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
					File indexDir = new File(splittedString[1] + "/index");
					indexDir.mkdirs();
					corpus = DirectoryCorpus.loadTextDirectory(Paths.get(splittedString[1]).toAbsolutePath(), ".json");
					Start = System.currentTimeMillis();
					index = indexCorpus(corpus, "advance", splittedString[1], "build");
					Stop = System.currentTimeMillis();
					timeTaken = Stop - Start;
					System.out.println("Indexing time: " + timeTaken);
					break;
				}

				case ":vocab": {
					List<String> vocab = diskIndex.getVocabulary();
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
					List<Posting> result = soundexIndex.getPostings(splittedString[1], soundexDir);
					for (Posting p : result) {
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
			} else if ("1".equals(mode)) {
				List<Posting> result = qc.getPostings(diskIndex, dir);
				for (Posting p : result) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
				System.out.println("Word found in " + result.size() + " documents!!");
			} else {

				RankedQuery rq = new RankedQuery(query, corpus.getCorpusSize(), rankmode);
				List<Posting> result = rq.getPostings(diskIndex, dir);
				for (Posting p : result) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + " accumulator: "
							+ p.getAccumulator());
				}
			}
			System.out.println("Enter a word to search");
			query = sc.nextLine();
		}
		sc.close();
	}

	private static void diskIndex(File dir, DiskIndexWriter diskIndex, Index index, String processorType)
			throws FileNotFoundException {

		try {
			PreparedStatement preparedStatement;
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);

			if (processorType.equalsIgnoreCase("advance")) {
				HashMap<String, Integer> bytePositionsList = diskIndex.writeIndex(index, weights,
						dir.getAbsolutePath());
				HashMap<String, Integer> biwordBytePosList = diskIndex.writeBiwordSoundexIndex(index,
						dir.getAbsolutePath(), "advance");

				preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS Milestone2.disk");
				preparedStatement.execute();

				preparedStatement = conn
						.prepareStatement("CREATE TABLE Milestone2.disk (" + "  id INT NOT NULL AUTO_INCREMENT,"
								+ "  Term VARCHAR(1500) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL,"
								+ "  BytePosition INT NOT NULL," + "  PRIMARY KEY (id));");
				preparedStatement.execute();

				preparedStatement = conn.prepareStatement("insert into Milestone2.disk values (default, ?, ?)");

				insertionToDB(preparedStatement, bytePositionsList);
				insertionToDB(preparedStatement, biwordBytePosList);
				System.out.println("Done");
				conn.close();
			} else {
				HashMap<String, Integer> soundexBytePosList = diskIndex.writeBiwordSoundexIndex(index,
						dir.getAbsolutePath(), "soundex");
				preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS Milestone2.soundex");
				preparedStatement.execute();

				preparedStatement = conn
						.prepareStatement("CREATE TABLE Milestone2.soundex (" + "  id INT NOT NULL AUTO_INCREMENT,"
								+ "  Hashcode VARCHAR(100) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL,"
								+ "  BytePosition INT NOT NULL," + "  PRIMARY KEY (id));");
				preparedStatement.execute();

				preparedStatement = conn.prepareStatement("insert into Milestone2.soundex values (default, ?, ?)");

				insertionToDB(preparedStatement, soundexBytePosList);
				System.out.println("Done");
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void insertionToDB(PreparedStatement preparedStatement, HashMap<String, Integer> bytePositionsList)
			throws SQLException {
		for (String entry : bytePositionsList.keySet()) {
			Integer byteLoc = bytePositionsList.get(entry);
			preparedStatement.setString(1, entry);
			preparedStatement.setInt(2, byteLoc);
			preparedStatement.execute();
		}
	}

	public static Index indexCorpus(DocumentCorpus corpus, String processorType, String dir, String indexMode) {
		EnglishTokenStream bodyTokens = null;
		EnglishTokenStream titleTokens = null;
		GetTokenProcessorFactory processorFactory = new GetTokenProcessorFactory();
		TokenProcessor processor = processorFactory.GetTokenProcessor(processorType);
		DiskIndexWriter diskIndexWriter = new DiskIndexWriter();

		Reader content;
		int totalCorpusToken = 0;
		Iterable<Document> list = corpus.getDocuments();

		if (processorType.equalsIgnoreCase("soundex")) {
			File soundexIDir = new File(dir + "/index");
			soundexIDir.mkdirs();
			// indexing for soundex

			SoundexIndex docindex = new SoundexIndex();

			try {
				Path path = Paths.get(dir + "/index/Soundexpostings.bin");
				if (!Files.exists(path)) {
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
					diskIndex(soundexIDir, diskIndexWriter, docindex, "soundex");
				}
				return docindex;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ex.toString());
			}
		} else {
			PositionalInvertedIndex docindex = new PositionalInvertedIndex();

			if (indexMode.equalsIgnoreCase("build")) {
				File indexDir = new File(dir + "/index");
				indexDir.mkdirs();
				try {
					for (Document d : list) {
						int positionId = 0;
						HashMap<String, Integer> weightDoc = new HashMap<>();
						content = d.getContent();

						File document = new File(d.getFilePath().toString());
						double byteSize = (double) document.length();
						int tokenCount = 0;

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

							positionId = createIndex(processor, docindex, d, bodytoken, positionId, weightDoc);
							positionId = createIndex(processor, docindex, d, titletoken, positionId, weightDoc);
						} catch (JsonSyntaxException e) {
							// indexing .txt file content for biword inverted
							Reader txtReader = d.getContent();
							EnglishTokenStream txtTokens = new EnglishTokenStream(txtReader);
							Iterable<String> txtToken = txtTokens.getTokens();

							positionId = createIndex(processor, docindex, d, txtToken, positionId, weightDoc);
							txtReader.close();
						}
						content.close();
						totalCorpusToken += calculateDocWeight(weightDoc, d.getId(), tokenCount, byteSize);
					}
					weights.add((double) (totalCorpusToken / corpus.getCorpusSize()));
					diskIndex(indexDir, diskIndexWriter, docindex, "advance");
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println(ex.toString());
				}
			}
			return docindex;
		}
		return null;
	}

	private static void calculateTfTd(HashMap<String, Integer> weightDoc, String term) {
		Integer tf = weightDoc.get(term);
		if (tf == null) {
			weightDoc.put(term, 1);
		} else {
			++tf;
			weightDoc.put(term, tf);
		}
	}

	private static int createIndex(TokenProcessor processor, PositionalInvertedIndex docindex, Document doc,
			Iterable<String> tokens, int i, HashMap<String, Integer> weightDoc) {
		String firstWord = null;
		String secondWord = null;
		for (String word : tokens) {
			List<String> words = (List<String>) processor.processToken(word);
			for (String term : words) {
				if (term.isEmpty())
					continue;
				if (firstWord == null && secondWord == null) {
					calculateTfTd(weightDoc, term);
					docindex.addTerm(term, doc.getId(), i);
					i = i + 1;
					firstWord = term;
					continue;
				}
				if (firstWord != null && secondWord == null) {
					calculateTfTd(weightDoc, term);
					docindex.addTerm(term, doc.getId(), i);
					i = i + 1;
					secondWord = term;
				}
				term = (firstWord + " " + secondWord);
				docindex.addBiwordTerm(term, doc.getId());
				firstWord = secondWord;
				secondWord = null;
			}
		}
		return i;
	}

	public static String getExtension(String directory) {
		String fileName = new File(directory).listFiles()[0].getName();
		return fileName.substring(fileName.lastIndexOf('.'));
	}

	public static double calculateDocWeight(HashMap<String, Integer> map, Integer docID, int tokensize, double size) {

		double weightSummation = 0;
		double total_tftd = 0;

		for (Integer value : map.values()) {
			double wdt = 1 + Math.log(value);
			weightSummation += (wdt * wdt);
			total_tftd += value;
		}

		double docLd = sqrt(weightSummation);
		double docLength = tokensize;
		double byteSize = size;
		double avg_tftd = total_tftd / map.size();

		weights.add(docLd);
		weights.add(total_tftd);
		weights.add(byteSize);
		weights.add(avg_tftd);

		return total_tftd;
	}
}
