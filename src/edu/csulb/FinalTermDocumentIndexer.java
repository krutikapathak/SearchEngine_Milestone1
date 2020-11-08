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
import cecs429.query.RankedQuery;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.GetTokenProcessorFactory;
import cecs429.text.TokenProcessor;
import static java.lang.Math.sqrt;
import java.util.ArrayList;

public class FinalTermDocumentIndexer {
    
         public static List<Double> weights = new ArrayList<>();

	private static final String url = "jdbc:mysql://localhost:3306/Milestone2?serverTimezone=UTC";
	private static final String user = "root";
	private static final String password = "root";

	public static void main(String[] args) throws FileNotFoundException {

		// Code to run the project on console. Use JSP files to run on web.
		Scanner sc = new Scanner(System.in);
		Gson gson = new Gson();

		System.out.println("Enter a directory/corpus to index");
		String dir = sc.nextLine();
		String soundexDir = "C:/Users/15625/Documents/NetBeansProjects/SearchEngine_Milestone1/mlb-articles-4000";
		
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
                
                System.out.println("Enter a query mode");
                System.out.println("1.Boolean Query");
                System.out.println("2.Ranked Query");
		String mode = sc.nextLine();
                
                if("1".equals(mode)){
                    System.out.println("You have selected boolean Query mode");
                }else {
                    System.out.println("You have selected Ranked Query mode");
                }

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
			} else if("1".equals(mode)){
				List<Posting> result = qc.getPostings(docIndex, dir);
//				List<Posting> result = qc.getPostings(index);
				for (Posting p : result) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
				System.out.println("Word found in " + result.size() + " documents!!");
			}else{
                          
                            RankedQuery rq = new RankedQuery(query, corpus.getCorpusSize());
                            List<Posting> result = rq.getPostings(docIndex, dir);
                            for (Posting p : result) {
				System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
                                System.out.println("accumulator: " + p.getAccumulator());
                                System.out.println("==================");
                            }
                        }
			System.out.println("Enter a word to search");
			query = sc.nextLine();
		}
		sc.close();
	}

	private static void insertToDB(File indexDir, DiskIndexWriter diskIndex, Index index, Index biwordIndex)
			throws FileNotFoundException {
		HashMap<String, Integer> bytePositionsList = diskIndex.writeIndex(index, weights, indexDir.getAbsolutePath());
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
					+ "  id INT NOT NULL AUTO_INCREMENT," + "  Term VARCHAR(1500) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL,"
					+ "  BytePosition INT NOT NULL," + "  PRIMARY KEY (id));");
			preparedStatement.execute();

			preparedStatement = conn.prepareStatement("insert into Milestone2.positions values (default, ?, ?)");

			for (String entry : bytePositionsList.keySet()) {
				Integer byteLoc = bytePositionsList.get(entry);
				//System.out.println("Key: " + entry + ", Value: " + byteLoc);
				preparedStatement.setString(1, entry);
				preparedStatement.setInt(2, byteLoc);
				preparedStatement.execute();
			}
			for (String entry : BiwordBytePosList.keySet()) {
				Integer byteLoc = BiwordBytePosList.get(entry);
				//System.out.println("Key: " + entry + ", Value: " + byteLoc);
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

//		if (processorType == "soundex") {
//			// indexing for soundex
//			SoundexIndex docindex = new SoundexIndex();
//
//			try {
//				for (Document d : list) {
//
//					content = d.getContent();
//					Gson gson = new Gson();
//					JsonObject doc = gson.fromJson(content, JsonObject.class);
//
//					String authorName = doc.get("author").getAsString();
//
//					if (!authorName.isEmpty()) {
//						List<String> words = (List<String>) processor.processToken(authorName);
//						for (String word : words) {
//							docindex.addTerm(word, d.getId());
//						}
//					}
//					content.close();
//				}
//				return docindex;
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				System.out.println(ex.toString());
//			}
//
//		} else 
{
			PositionalInvertedIndex docindex = new PositionalInvertedIndex();

			try {
				for (Document d : list) {
					int i = 0;
                                        
                                        HashMap<String, Integer> weightDoc = new HashMap<>();
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
                                                            Integer tf = weightDoc.get(term);
                                                            if (tf == null) {
                                                                 weightDoc.put(term,1);
                                                            } else {
                                                                   ++tf;
                                                                 weightDoc.put(term,tf);
                                                            }
                                                            docindex.addTerm(term, d.getId(), i);
                                                            i = i + 1;
							}
						}
						for (String word : titletoken) {
							List<String> words = (List<String>) processor.processToken(word);
							for (String term : words) {
                                                            Integer tf = weightDoc.get(term);
                                                            if (tf == null) {
                                                                 weightDoc.put(term,1);
                                                            } else {
                                                                   ++tf;
                                                                 weightDoc.put(term,tf);
                                                            }
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
                                                            Integer tf = weightDoc.get(term);
                                                            if (tf == null) {
                                                                 weightDoc.put(term,1);
                                                            } else {
                                                                   ++tf;
                                                                 weightDoc.put(term,tf);
                                                            }
                                                            docindex.addTerm(term, d.getId(), i);
                                                            i = i + 1;
							}
						}
						txtReader.close();
					}
					content.close();
                                        calculateDocWeight(weightDoc, d.getId());
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

					//System.out.println("Body starts");
					addBiWord(processorType, processorFactory, biwordIndex, d, bodytoken);
					//System.out.println("Title starts");
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
				if (term.isEmpty())
					continue;
				if (firstWord == null && secondWord == null) {
					firstWord = term;
					continue;
				}
				if (firstWord != null && secondWord == null) {
					secondWord = term;
				}
				term = (firstWord + " " + secondWord);
				//System.out.println("Body: " + term);
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
        
        
        public static void processBooleanQuery(){
            
        }
        
        public static void processRankedQuery(){
            
        }
        
         public static void calculateDocWeight(HashMap<String, Integer> map, Integer docID) {
            
            double weightSummation = 0;
            double total_tftd = 0;
            
            for (Integer value : map.values()) {
                double wdt = 1 + Math.log(value);
                weightSummation += (wdt * wdt);
                total_tftd += value;
            }
            
            double docLd = sqrt(weightSummation); 
            double docLength = map.size();
            double byteSize = 0.12;
            double avg_tftd = total_tftd/map.size();
            System.out.println(docID + ":" + docLd);
            weights.add(docLd);
            weights.add(docLength);
            weights.add(byteSize);
            weights.add(avg_tftd);
        }
}
