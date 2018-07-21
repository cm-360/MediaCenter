package mediacenter.lib.types.io.file;

import java.io.File;

import mediacenter.lib.utils.io.file.text.SimpleTextReader;
import mediacenter.lib.utils.io.file.text.SimpleTextWriter;

public class TextFile {
	
	private File file;
	private SimpleTextReader tr;
	private SimpleTextWriter tw;
	
	// Constructor
	/** Creates text readers and writers for the {@code File} object */
	public TextFile(File f) {
		setFile(f);
	}
	
	// Access methods
	/** Gets the path of this {@code TextFile} */
	public File getFile() {
		return file;
	}
	
	/** Sets the path of this {@code TextFile} */
	public void setFile(File f) {
		file = f;
		tr = new SimpleTextReader(f);
		tw = new SimpleTextWriter(f);
	}
	
	// Utility methods
	/** Gets the contents of this {@code TextFile} */
	public String readContents() {
		return tr.read();
	}
	
	/** Creates a text file with the given contents (or overwrites) */
	public boolean writeContents(String contents) {
		return tw.write(contents);
	}
	
}
