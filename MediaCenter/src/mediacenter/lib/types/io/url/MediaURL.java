package mediacenter.lib.types.io.url;

import java.net.URL;
import java.util.ArrayList;

import mediacenter.lib.types.simple.SimpleMap;

public class MediaURL implements Comparable<MediaURL> {
	
	private URL url;
	private String filename;
	private SimpleMap<String, String> data;
	
	private String imageQuality = "";
	
	public MediaURL(final URL url, final String filename, final SimpleMap<String, String> data) {
		this.url = url;
		this.filename = filename;
		this.data = data;
	}
	
	// Access methods
	public URL getURL() {
		return url;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public SimpleMap<String, String> getData() {
		return data;
	}
	
	@Override
	public String toString() {
		String name = filename.substring(0, filename.lastIndexOf("."));
		Object tagsListO = data.get("tags");
		if (tagsListO != null) { // Can't check a null list
			if (imageQuality.isEmpty()) {
				if (ArrayList.class.isInstance(tagsListO)) {
					@SuppressWarnings("unchecked")
					ArrayList<String> tagsList = (ArrayList<String>) tagsListO;
					for (String s : tagsList)
						if (s.replaceAll("[_-]", "").matches("\\d+[PpKk]")) {
							name += (" [" + (imageQuality = s) + "]");
							break;
						}
				}
			} else
				name += (" [" + imageQuality + "]");
		}
		return name
				+ (filename.contains(".") ? String.format(" (%s)", filename.substring(filename.lastIndexOf("."))) : "");
	}
	
	public void setFilename(String name) {
		filename = name;
	}
	
	// Comparable method
	@Override
	public int compareTo(MediaURL arg0) {
		return filename.compareTo(arg0.getFilename());
	}
	
}
