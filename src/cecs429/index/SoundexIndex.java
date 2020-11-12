/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author atandel
 */
public class SoundexIndex implements Index {

	private Statement statement = null;
	private ResultSet resultSet = null;

	private static final String url = "jdbc:mysql://localhost:3306/Milestone2?serverTimezone=UTC";
	private static final String user = "root";
	private static final String password = "password";

	private HashMap<String, List<Integer>> soundexMap;

	public SoundexIndex() {
		soundexMap = new HashMap<>();
	}

	public void addTerm(String term, int documentId) {
		// System.out.println(term);
		String hashcode = soundex(term);
		List<Integer> docList = soundexMap.get(hashcode);

		// if list does not exist create it
		if (docList == null) {
			List<Integer> new_docList = new ArrayList<>();
			new_docList.add(documentId);
			soundexMap.put(hashcode, new_docList);
		} else {
			// add if docID is not already in list
			if (docList.get(docList.size() - 1) < documentId)
				docList.add(documentId);
		}
	}

	public int seekSoundexByteLoc(String hashcode, DataInputStream din) {
		int number = 0;
		try {
			// load the MySQL driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Setup the connection with the DB
			Connection conn = DriverManager.getConnection(url, user, password);
			statement = conn.createStatement();
			resultSet = statement.executeQuery(
					"SELECT Hashcode, BytePosition FROM Milestone2.soundex WHERE Hashcode='" + hashcode + "'");
			while (resultSet.next()) {
				String diskTerm = resultSet.getString("Hashcode");
				Integer diskPos = resultSet.getInt("BytePosition");
				din.skipBytes(diskPos);
				number = din.readInt();
			}
			System.out.println(number);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return number;
	}

	public List<Posting> getSoundexPostings(String term, String directory) {
		String normalizedTerm = term;
		if (directory == null)
			normalizedTerm = term.replaceAll("\\W", "").toLowerCase();
		String hashcode = soundex(normalizedTerm);
		List<Posting> results = new ArrayList<>();
		List<Integer> docList;

		if (soundexMap.get(hashcode) != null) {
			docList = soundexMap.get(hashcode);

			for (int i = 0; i < docList.size(); i++) {
				results.add(new Posting(docList.get(i)));
			}
		}
		return results;
	}

	@Override
	public List<Posting> getPostings(String term, String directory) {
		String normalizedTerm = term.replaceAll("\\W", "").toLowerCase();
		String hashcode = soundex(normalizedTerm);
		List<Posting> result = new ArrayList<>();
		DataInputStream din;
		try {
			din = new DataInputStream(new FileInputStream(directory + "/index/Soundexpostings.bin"));
			int totalDocs = seekSoundexByteLoc(hashcode, din);
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
		ArrayList<String> Hashcodes = new ArrayList<String>(soundexMap.keySet());
		Collections.sort(Hashcodes);
		return Collections.unmodifiableList(Hashcodes);
	}

	public static String soundex(String s) {
		char[] input = s.toUpperCase().toCharArray();
		// retain first letter
		String output = input[0] + "";

		// replace consonants with digits
		for (int i = 0; i < input.length; i++) {
			switch (input[i]) {
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
			case 'H':
			case 'W':
			case 'Y':
				input[i] = '0';
				break;

			case 'B':
			case 'F':
			case 'P':
			case 'V':
				input[i] = '1';
				break;

			case 'C':
			case 'G':
			case 'J':
			case 'K':
			case 'Q':
			case 'S':
			case 'X':
			case 'Z':
				input[i] = '2';
				break;

			case 'D':
			case 'T':
				input[i] = '3';
				break;

			case 'L':
				input[i] = '4';
				break;

			case 'M':
			case 'N':
				input[i] = '5';
				break;

			case 'R':
				input[i] = '6';
				break;
			}
		}

		// remove duplicates
		for (int i = 1; i < input.length; i++)
			if (input[i] != input[i - 1] && input[i] != '0')
				output += input[i];

		// pad with zeros if length is < 4
		for (int i = output.length(); i < 4; i++) {
			output = output + "0";
		}
		return output;
	}

	@Override
	public List<Posting> getPostingsDocandPos(String term, String directory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getBiwordVocabulary() {
		// TODO Auto-generated method stub
		return null;
	}

}
