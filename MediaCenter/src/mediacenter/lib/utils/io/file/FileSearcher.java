package mediacenter.lib.utils.io.file;

import java.io.File;
import java.io.FileFilter;

import mediacenter.lib.types.simple.SimpleList;

public class FileSearcher {
	
	public static SimpleList<File> listFiles(File dir, FileFilter filter, boolean recursive) {
		SimpleList<File> results = new SimpleList<File>();
		File[] found = dir.listFiles();
		if (found != null)
			for (File f : found) {
				if (f.isDirectory() && recursive)
					results.add(listFiles(f, filter, true));
				else if (filter.accept(f))
					results.add(f);
			}
		return results;
	}
	
}
