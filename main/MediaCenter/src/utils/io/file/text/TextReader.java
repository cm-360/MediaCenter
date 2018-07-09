package utils.io.file.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TextReader {
	
private File file;
	
	public TextReader(File f) {
		file = f;
	}
	
	public String read() {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			String text;
			br = new BufferedReader(new FileReader(file));
			while ((text = br.readLine()) != null)
				sb.append(text + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
}
