package utils.io.file.filters;

import java.io.File;
import java.io.FileFilter;

public class JARFilter implements FileFilter {

	@Override
	public boolean accept(File arg0) {
		return (!arg0.isDirectory() && arg0.toString().toLowerCase().endsWith(".jar"));
	}

}
