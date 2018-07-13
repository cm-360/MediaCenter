package utils.io.file.filters;

import java.io.File;
import java.io.FileFilter;

public class MediaFilter implements FileFilter {
	
	private String[] bannedExts = {
			".json",
			".url",
			".txt",
	};

	@Override
	public boolean accept(File arg0) {
		if (arg0.isDirectory())
			return false;
		else
			for (String e : bannedExts)
				if (arg0.toString().toLowerCase().endsWith(e))
					return false;
		return true;
	}

}
