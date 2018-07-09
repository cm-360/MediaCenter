package utils.io.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class FileSearcher {
	
	public static ArrayList<File> listFiles(File dir, FileFilter filter, boolean recursive) {
		ArrayList<File> results = new ArrayList<File>();
		File[] found = dir.listFiles();
		if (found != null)
			for (File f : found) {
				if (f.isDirectory() && recursive)
					results.addAll(listFiles(f, filter, true));
				else if (filter.accept(f))
					results.add(f);
			} 
		return results;
	}
	
}
