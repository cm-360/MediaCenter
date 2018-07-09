package utils.io.file.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextWriter {
	
	private File file;
	
	public TextWriter(File f) {
		file = f;
	}
	
	public boolean write(String text) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}
