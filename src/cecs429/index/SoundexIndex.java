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
		char[] input = s.toUpperCase().toCharArray();
		// retain first letter
		String output = input[0] + "";

		// replace consonants with digits
		for (int i = 0; i < input.length; i++) {
			switch (input[i]) {
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

			default:
				input[i] = '0';
				break;
			}
		}

		// remove duplicates
                for (int i = 1; i<input.length; i++)
                  if (input[i] != input[i - 1] && input[i] != '0')
                    output += input[i];

                // pad with zeros if length is < 4
                for (int i=output.length(); i<4; i++){
                    output = output + "0";
                }
                return output;
	}

}
