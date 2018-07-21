package mediacenter.lib.types.io.file;

import java.io.File;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mediacenter.lib.types.simple.SimpleMap;

public class MediaFile {
	
	private File mediaFile;
	private TextFile dataFile;
	private SimpleMap<String, String> data = new SimpleMap<String, String>();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	// Constructors
	public MediaFile(final File filePath) {
		this(filePath, new File(filePath.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json")));
	}
	
	public MediaFile(final File filePath, final File tagsPath) {
		mediaFile = filePath;
		dataFile = new TextFile(tagsPath);
		load();
	}
	
	public MediaFile(final File filePath, final SimpleMap<String, String> data) {
		mediaFile = filePath;
		dataFile = new TextFile(new File(filePath.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json")));
		this.data = data;
		save();
	}
	
	// Info methods
	public File getMediaFile() {
		return mediaFile;
	}
	
	public TextFile getDataFile() {
		return dataFile;
	}
	
	// IO methods
	@SuppressWarnings("unchecked")
	public void load() {
		File dataIOFile = dataFile.getFile();
		if (dataIOFile.exists()) {
			String json = dataFile.readContents();
			if (!json.isEmpty()) {
				data.clear();
				HashMap<String, Object> read;
				if ((read = gson.fromJson(json, HashMap.class)) != null)
					for (String key : read.keySet()) // For compatibility
						data.put(key, read.get(key).toString().replaceAll("[^\\w\\s,\\.]", ""));
				else
					System.out.println("JSON could not be parsed"); // TODO json could not be read
			} 
		} else {
			System.out.println("JSON file was not found");
			dataIOFile.getParentFile().mkdirs();
			save();
		}
	}
	
	public void save() {
		if (!dataFile.writeContents(gson.toJson(data.getHashMap(), HashMap.class)))
			System.out.println("JSON could not be written"); // TODO json could not be written
	}
	
	public void setTag(String key, String value) {
		data.put(key, value);
	}
	
	public String getTag(final String key) {
		String value = data.get(key);
		if (value == null) {
			data.put(key, (value = "unknown"));
			save();
		}
		return value;
	}
	
	public String getInfo() {
		return getClass().getSimpleName() + "[path=\"" + mediaFile.getPath() + "\", data=" + data.toString() + "]";
	}

	// Override methods
	@Override
	public String toString() {
		String name = mediaFile.getName();
		return String.format("%s (%s)", getTag("title"), name.toLowerCase().substring(name.lastIndexOf(".")));
	}
	
}
