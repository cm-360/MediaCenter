package mediacenter.lib.utils.io.file.filters;

import java.io.File;
import java.io.FileFilter;

public class FolderFilter implements FileFilter {

	@Override
	public boolean accept(File arg0) {
		return arg0.isDirectory();
	}

}
