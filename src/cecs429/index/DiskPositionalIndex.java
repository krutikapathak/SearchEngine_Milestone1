package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cecs429.text.AdvanceTokenProcessor;

/**
 * 
 * @author Krutika Pathak
 *
 */
public class DiskPositionalIndex implements Index {

	private Statement statement = null;
	private ResultSet resultSet = null;

	private static final String url = "jdbc:mysql://localhost:3306/Milestone2?serverTimezone=UTC";
	private static final String user = "root";
	private static final String password = "password";

	public DiskPositionalIndex() {

	}

	public int seekByteLoc(String term, DataInputStream din) {
//		term = AdvanceTokenProcessor.processTerm(term);
		int number = 0;
		try {
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);
			statement = conn.createStatement();
			resultSet = statement
					.executeQuery("SELECT Term, BytePosition FROM Milestone2.disk WHERE Term='" + term + "'");
			while (resultSet.next()) {
				String diskTerm = resultSet.getString("Term");
				Integer diskPos = resultSet.getInt("BytePosition");
				din.skipBytes(diskPos);
				number = din.readInt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return number;
	}

	@Override
	public List<Posting> getPostings(String term, String directory) {
		List<Posting> result = new ArrayList<>();
		DataInputStream din;
		try {
			din = getDin(term, directory);
			term = AdvanceTokenProcessor.processTerm(term);
			int totalDocs = seekByteLoc(term, din);
			int i = 0;
			if (totalDocs != 0) {
				do {
					int docIdGap = din.readInt();
					int prevDocId = 0;
					if (result.size() > 0) {
						prevDocId = result.get(i - 1).getDocumentId();
					}
					int docId = docIdGap + prevDocId;
					Posting p = new Posting(docId);
					result.add(p);
					if (!term.contains(" ")) {
						int freq = din.readInt();
						din.skipBytes(freq * 4);
					}
					i++;
				} while (i < totalDocs);
			}
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private DataInputStream getDin(String term, String directory) throws FileNotFoundException {
		DataInputStream din;
		if (term.contains(" ")) {
			din = new DataInputStream(new FileInputStream(directory + "/index/Biwordpostings.bin"));
		} else
			din = new DataInputStream(new FileInputStream(directory + "/index/postings.bin"));
		return din;
	}

	@Override
	public List<Posting> getPostingsDocandPos(String term, String directory) {
		List<Posting> result = new ArrayList<>();
		DataInputStream din;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/postings.bin"));
			int totalDocs = seekByteLoc(term, din);
			int i = 0;
			if (totalDocs != 0) {
				do {
					List<Integer> positions = new ArrayList<>();
					int docIdGap = din.readInt();
					int prevDocId = 0;
					if (result.size() > 0) {
						prevDocId = result.get(i - 1).getDocumentId();
					}
					int docId = docIdGap + prevDocId;
					int freq = din.readInt();
					int prevPosId = 0;
					int j = 0;
					while (j < freq) {
						int posGap = din.readInt();
						int position = posGap + prevPosId;
						positions.add(position);
						if (positions.size() > 0) {
							prevPosId = positions.get(positions.size() - 1);
						}
						j++;
					}
					Posting p = new Posting(docId, positions);
					result.add(p);
					i++;
				} while (i < totalDocs);
			}
			din.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<String> getVocabulary() {
		List<String> vocab = new ArrayList<String>();
		try {
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);
			statement = conn.createStatement();
			// fetch first 1000 words in vocab
			resultSet = statement.executeQuery(
					"SELECT * FROM Milestone2.disk where Term not like '% %' order by Term asc limit 1000");
			while (resultSet.next()) {
				String term = resultSet.getString("Term");
				vocab.add(term);
			}
			// fetch total number of words in vocab
			resultSet = statement.executeQuery("SELECT COUNT(*)\n" + "FROM Milestone2.disk where Term not like '% %'");
			while (resultSet.next()) {
				String term = resultSet.getString("COUNT(*)");
				vocab.add(term);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vocab;
	}

	@Override
	public List<String> getBiwordVocabulary() {
		return null;
	}

}
