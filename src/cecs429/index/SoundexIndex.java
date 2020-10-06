/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author atandel
 */
public class SoundexIndex implements Index {

	private HashMap<String, List<Integer>> soundexMap = new HashMap<>();

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

	@Override
	public List<Posting> getPostings(String term) {
		String normalizedTerm = term.replaceAll("\\W", "").toLowerCase();
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
	public List<String> getVocabulary() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	public static String soundex(String s) {
		char[] x = s.toUpperCase().toCharArray();
		// retain first letter
		String output = x[0] + "";

		// replace consonants with digits
		for (int i = 0; i < x.length; i++) {
			switch (x[i]) {
			case 'B':
			case 'F':
			case 'P':
			case 'V':
				x[i] = '1';
				break;

			case 'C':
			case 'G':
			case 'J':
			case 'K':
			case 'Q':
			case 'S':
			case 'X':
			case 'Z':
				x[i] = '2';
				break;

			case 'D':
			case 'T':
				x[i] = '3';
				break;

			case 'L':
				x[i] = '4';
				break;

			case 'M':
			case 'N':
				x[i] = '5';
				break;

			case 'R':
				x[i] = '6';
				break;

			default:
				x[i] = '0';
				break;
			}
		}

		// remove duplicates
		for (int i = 1; i < x.length; i++)
			if (x[i] != x[i - 1] && x[i] != '0')
				output += x[i];

		// right pad with zeros or truncate
		output = output + "0000";
		return output.substring(0, 4);
	}

}
