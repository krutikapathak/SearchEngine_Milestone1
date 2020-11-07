package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cecs429.snowball.SnowballStemmer;

/**
 * 
 * @author Krutika Pathak
 *
 */

public class AdvanceTokenProcessor implements TokenProcessor {

	@Override
	public List<String> processToken(String token) {
		List<String> stringTokens = new ArrayList<>();
		List<String> splitTokens = new ArrayList<>();
		// splitting hyphenated words and stemming
		token = removeHyphens(token);
		splitTokens = Arrays.asList(token.split(" "));
		for (String split : splitTokens) {
			if (split.contains("-")) {
				String[] splitToken = split.split("-");
				for (int i = 0; i < splitToken.length; i++) {
					stringTokens.add(processTerm(splitToken[i]));
				}
				if (splitToken.length == 0)
					continue;
				String combinedtoken = splitToken[0];
				for (int i = 1; i < splitToken.length; i++) {
					combinedtoken = combinedtoken + splitToken[i];
				}
				stringTokens.add(processTerm(combinedtoken));
			} else {
				// stemming normal tokens(without hyphens)
				stringTokens.add(processTerm(split));
			}
		}
		return stringTokens;
	}

	private String removeHyphens(String token) {
		token = token.replace("—", "-").replace("–", "-").replace(" ", " ");
		return token;
	}

	public static String stemWord(String token) {
		SnowballStemmer stemmer = null;
		// stem the token and return stemmed word
		try {
			Class stemClass = Class.forName("cecs429.snowball.ext.englishStemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		stemmer.setCurrent(token.toLowerCase());
		stemmer.stem();
		return stemmer.getCurrent();

	}

	public static String removenonAlphanumeric(String token) {
		// remove the non-alphanumeric characters from beginning and end
		if (token.isBlank()) {
			return "";
		}
		for (int i = 0; i < token.length(); i++) {
			for (int j = (token.length()) - 1; j > 0; j--) {

				char startIndex = token.charAt(i);
				char endIndex = token.charAt(j);
				if (Character.isLetterOrDigit(startIndex) && Character.isLetterOrDigit(endIndex)) {
					return token.substring(i, j + 1);
				}
			}
		}
		return Character.isLetterOrDigit(token.charAt(0)) ? token.substring(0, 1) : "";
	}

	public static String processTerm(String query) {
		List<String> mTerms = new ArrayList<>();
		if (query.contains(" ")) {
			List<String> termList = Arrays.asList(query.split(" "));
			for (String term : termList) {
				if (term.isBlank())
					continue;
				mTerms.add(termProcessor(term));
			}
			if (mTerms.size() == 2) {
				query = mTerms.get(0) + " " + mTerms.get(1);
			}
		} else {
			query = termProcessor(query);
		}
		return query;
	}

	private static String termProcessor(String query) {
		String alphanumeric_mterm = AdvanceTokenProcessor.removenonAlphanumeric(query);
		String processedmTerm = alphanumeric_mterm.replaceAll("\'", "").replaceAll("\"", "");
		query = AdvanceTokenProcessor.stemWord(processedmTerm);
		return query;
	}
}
