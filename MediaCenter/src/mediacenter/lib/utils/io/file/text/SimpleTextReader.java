package mediacenter.lib.utils.io.file.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SimpleTextReader {
	
	private File file;
	
	/**
	 * Creates a text reader
	 * 
	 * @param f The file to read
	 */
	public SimpleTextReader(File f) {
		file = f;
	}
	
	/**
	 * Reads text from a file
	 * 
	 * @return Contents of the file
	 */
	public String read() {
		StringBuilder su = new StringBuilder();
		BufferedReader br = null;
		try {
			String text;
			br = new BufferedReader(new FileReader(file));
			while ((text = br.readLine()) != null)
				su.append(text + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return su.toString();
	}
	
}
