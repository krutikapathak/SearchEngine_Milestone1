package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
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
		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(term);
		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
		term = AdvanceTokenProcessor.stemWord(processedmTerm);
		int number = 0;
		try {
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);
			statement = conn.createStatement();
			resultSet = statement
					.executeQuery("SELECT Term, BytePosition FROM Milestone2.positions WHERE Term='" + term + "'");
			Integer diskPos = null;
			while (resultSet.next()) {
				String diskTerm = resultSet.getString("Term");
				diskPos = resultSet.getInt("BytePosition");
			}
			din.skipBytes(diskPos);
			number = din.readInt();
			System.out.println(number);
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
			din = new DataInputStream(new FileInputStream(directory + "/index/postings.bin"));
			int totalDocs = seekByteLoc(term, din);
			int i = 0;
			do {
				int docIdGap = din.readInt();
				int prevDocId = 0;
				if (result.size() > 0) {
					prevDocId = result.get(i - 1).getDocumentId();
				}
				int docId = docIdGap + prevDocId;
				Posting p = new Posting(docId);
				result.add(p);
				int freq = din.readInt();
				din.skipBytes(freq * 4);
				i++;
			} while (i < totalDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Posting> getPostingsDocandPos(String term, String directory) {
		List<Posting> result = new ArrayList<>();
		DataInputStream din;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/postings.bin"));
			int totalDocs = seekByteLoc(term, din);
			int i = 0;
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
				System.out.println(docId + "<" + positions + ">");
				Posting p = new Posting(docId, positions);
				result.add(p);
				i++;
			} while (i < totalDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(String term) {
		return null;
	}

	@Override
	public List<String> getVocabulary() {
		return null;
	}
}
