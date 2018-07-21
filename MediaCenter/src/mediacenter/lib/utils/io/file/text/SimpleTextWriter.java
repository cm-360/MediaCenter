package mediacenter.lib.utils.io.file.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleTextWriter {
	
	private File file;
	
	/**
	 * Creates a text writer
	 * 
	 * @param f The file for writing
	 */
	public SimpleTextWriter(File f) {
		file = f;
	}
	
	/**
	 * Writes text to a file
	 * 
	 * @param text
	 *            The text to write
	 * @return {@code true} if it was written successfully, {@code false} otherwise
	 */
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
