package utils.io.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import utils.io.file.text.TextReader;
import utils.io.file.text.TextWriter;

public class MediaFile implements Comparable<MediaFile> {
	
	private File filePath, tagsPath;
	private HashMap<String, Object> tags;
	private transient Gson gson;
	
	// Constructors
	public MediaFile(File filePath) {
		this(filePath, new File(filePath.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json")));
	}
	
	public MediaFile(File filePath, File tagsPath) {
		this(); // Initialize the gson object
		this.filePath = filePath;
		this.tagsPath = tagsPath;
		load();
	}
	
	private MediaFile() {
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	// Import method
	public static MediaFile createFrom(File filePath, HashMap<String, Object> tags) {
		MediaFile imported = new MediaFile();
		imported.filePath = filePath;
		imported.tagsPath = new File(filePath.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json"));
		imported.tags = tags;
		imported.save();
		return imported;
	}
	
	// Tag methods
	public void setTag(String key, Object value) {
		tags.put(key, value);
	}
	
	public Object getTag(String key) {
		return tags.get(key);
	}
	
	public HashMap<String, Object> getTags() {
		return tags;
	}
	
	// Control methods
	public boolean save() {
		String json = gson.toJson(tags, HashMap.class);
		return new TextWriter(tagsPath).write(json);
	}
	
	public void load() {
		try {
			load(false);
			// Backwards compatibility
			Object t = getTag("tags");
			if (t != null && !(t instanceof List)) {
				System.out.println("Updating tag data for \"" + filePath + "\"");
				List<String> tNew = Arrays.asList(t.toString().split(","));
				Collections.sort(tNew);
				setTag("tags", tNew);
				save();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void load(boolean failedAttempt) throws IOException {
		String json = new TextReader(tagsPath).read();
		if (!json.isEmpty()) {
			tags = gson.fromJson(json, HashMap.class);
		} else if (!failedAttempt) {
			tags = new HashMap<String, Object>();
			setTag("title", toString());
			setTag("author", "unknown");
			setTag("rating", 0);
			setTag("tags", new ArrayList<String>());
			save();
			load(true);
		} else {
			throw new IOException("Could not open file \"" + tagsPath + "\"");
		}
	}
	
	// Path methods
	public File getFilePath() {
		return filePath;
	}
	
	public File getTagsPath() {
		return tagsPath;
	}
	
	// Comparable method
	@Override
	public int compareTo(MediaFile arg0) {
		return getTag("title").toString().compareTo(arg0.getTag("title").toString());
	}
	
	// Naming method
	@Override
	public String toString() {
		Object nameTag = getTag("title");
		if (nameTag != null)
			return nameTag.toString();
		else {
			Matcher nameMatcher = Pattern.compile("[^\\\\\\/]+(?=\\.\\w*$)").matcher(filePath.toString());
			if (nameMatcher.find())
				return nameMatcher.group();
			else
				return filePath.getName();
		}
	}
	
}
