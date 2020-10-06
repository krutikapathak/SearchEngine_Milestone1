package cecs429.text;

import java.util.ArrayList;
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
		List<String> Stringtokens = new ArrayList<>();
		String alphanumeric_string = removenonAlphanumeric(token);
		String processed = alphanumeric_string.replaceAll("\'", "").replaceAll("\"", "");

		if (token.contains("-")) {
			String[] splitToken = processed.split("-");
			String combinedtoken = processed.replaceAll("-", "").replaceAll(" ", "");
			for (int i = 0; i < splitToken.length; i++) {
				Stringtokens.add(stemWord(splitToken[i]));
			}
			Stringtokens.add(stemWord(combinedtoken));
		} else {
			Stringtokens.add(stemWord(processed));
		}
		return Stringtokens;
	}

	public static String stemWord(String token) {
		SnowballStemmer stemmer = null;
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
}
