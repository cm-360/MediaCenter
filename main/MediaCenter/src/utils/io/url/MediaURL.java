package utils.io.url;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaURL implements Comparable<MediaURL> {
	
	private URL url;
	private String filename;
	private HashMap<String, Object> tags;
	
	private String imageQuality = "";
	
	public MediaURL(URL url, String filename, HashMap<String, Object> tags) {
		this.url = url;
		this.filename = filename;
		this.tags = tags;
	}
	
	// Access methods
	public URL getURL() {
		return url;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public HashMap<String, Object> getTags() {
		return tags;
	}
	
	@Override
	public String toString() {
		String name = filename.substring(0, filename.lastIndexOf("."));
		Object tagsListO = tags.get("tags");
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
