package mediacenter.lib.utils.io.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mediacenter.lib.types.simple.SimpleList;

public class HTMLGrabber {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

	/** Gets the HTML of a given {@code URL} */
	public static String grab(URL url) throws IOException {
		StringBuilder output = new StringBuilder();

		// Stream webpage into BufferedReader
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", USER_AGENT);
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Transfer data into the ArrayList
		String currentLine;
		try {
			while ((currentLine = br.readLine()) != null) {
				output.append(currentLine + "\n");
			}
		} catch (IOException e) { /* Do Nothing */ }
//		new TextWriter(new File(System.getProperty("user.dir") + "/html.txt")).write(output.toString()); // Ouput HTML to file for debugging
		return output.toString();
	}

	/**
	 * Returns all {@code URL}s contained in the webpage at {@code url} matching the
	 * given regex
	 */
	public static SimpleList<URL> extractURLs(URL url, String regex) {
		SimpleList<URL> links = new SimpleList<URL>();
		try {
			// Read the webpage
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", USER_AGENT);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String currentLine, totalLines = "";
			while ((currentLine = br.readLine()) != null)
				totalLines += currentLine;
			// Find all links contained in the HTML
			Pattern linkPattern = Pattern.compile(regex);
			Matcher link = linkPattern.matcher(totalLines);
			while (link.find())
				links.add(new URL(link.group()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Return all links found
		return links;
	}

	/** Returns all {@code URL}s contained in the webpage at {@code url} */
	public static SimpleList<URL> extractURLs(URL url) {
		return extractURLs(url, "(?<=src[=]\\\"|href[=]\\\")(?:https?:\\/\\/)[^\\\"]*(?=\\\">?)");
	}

	/** Checks if a given {@code URL} exists */
	public static boolean exists(URL url) {
		try {
			// Open connection to site and get response code
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			int responseCode = huc.getResponseCode();
			// Say if site is available or not
			return !(responseCode == 404);
		} catch (IOException e) {
			return false;
		}
	}

}
