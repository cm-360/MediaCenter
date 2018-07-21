package mediacenter.lib.utils.text;

public class StringTools {
	
	public static String toTitleCase(String input) {
		// Convert the first letter of each word to uppercase
		char[] stringChars = input.toCharArray();
		for (int i = 0; i < input.length(); i++) {
			if (i == 0 || !Character.isLetter(stringChars[i - 1]))
				stringChars[i] = Character.toUpperCase(stringChars[i]);
			else
				stringChars[i] = Character.toLowerCase(stringChars[i]);
		}
		return new String(stringChars);
	}
	
	public static String removeSpecialChars(String input) {
		return input.replaceAll("[^\\w\\s]", "");
	}
	
	public static String reverse(String input) {
		return new StringBuilder(input).reverse().toString();
	}
	
}
