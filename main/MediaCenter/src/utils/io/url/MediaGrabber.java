package utils.io.url;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.OperationNotSupportedException;
import javax.swing.JComponent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import internal.swing.mediaplayers.PlayerPanel;
import internal.utils.library.Library;
import utils.applets.grabber.DomainGrabber;
import utils.io.DownloadManager;
import utils.io.file.FileSearcher;
import utils.io.file.MediaFile;
import utils.io.file.filters.*;
import utils.io.file.text.TextReader;
import utils.strings.StringUtils;

public class MediaGrabber {
	
	private Library lib;
	private DownloadManager downloads;
	private ArrayList<DomainGrabber> grabbers = new ArrayList<DomainGrabber>();
	
	// Constructor
	public MediaGrabber(Library lib, DownloadManager downloads, File addonsDir, boolean recursive) {
		this.downloads = downloads;
		if (addonsDir.exists())
			findPlugins(addonsDir, recursive);
		else
			addonsDir.mkdirs();
		setLibrary(lib);
	}
	
	// Control methods
	public ArrayList<MediaURL> search(URL site) {
		ArrayList<MediaURL> results = new ArrayList<MediaURL>();
		for (DomainGrabber g : grabbers)
			if (site.toString().matches(g.getSiteRegex()))
				results.addAll(g.search(site));
		if (results.isEmpty()) { // Fallback attempt to find media files
			try {
				String html = HTMLGrabber.grab(site);
				Matcher urlMatcher = Pattern
						.compile(
								"(?:https?:\\/\\/)(?:(?:[\\w-]+\\.)+[\\w-]+)(?:\\/[\\w-]+)+(?:\\.[\\w-]+)+(?:\\?(?:[\\w-]+(?:=[\\w-]+&?)?)+)?")
						.matcher(html);
				// Construct a list of recognized file extensions
				HashSet<String> exts = new HashSet<String>();
				for (JComponent p : lib.getMediaPlayers().getComponents().values())
					if (p instanceof PlayerPanel)
						exts.addAll(Arrays.asList(((PlayerPanel) p).getSupportedExtensions()));
				while (urlMatcher.find()) {
					try {
						// Make sure the file to download has a recognized extension
						String group = urlMatcher.group(), ext = group.substring(group.lastIndexOf(".") + 1);
						Matcher extMatcher = Pattern.compile("\\w+").matcher(ext);
						if (extMatcher.find()) {
							ext = extMatcher.group();
							for (String e : exts)
								if (ext.toLowerCase().matches(e)) {
									// Uses the html <title> tag and the filename as final title
									String title = "";
									Matcher titleMatcher = Pattern.compile("(?<=<title>)[^<>]+(?=<\\/title>)")
											.matcher(html);
									if (titleMatcher.find())
										title += titleMatcher.group().replaceAll("[^\\w\\s]", "");
									title += (" - "
											+ StringUtils.toTitleCase(
													group.substring(group.lastIndexOf("/") + 1, group.lastIndexOf(".")))
											+ group.substring(group.lastIndexOf(".")));
									HashMap<String, Object> tags = guessTags(lib, title);
									results.add(new MediaURL(new URL(group), title, tags));
									break;
								}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return results;
	}
	
	public ArrayList<MediaURL> search(File file) {
		ArrayList<MediaURL> results = new ArrayList<MediaURL>();
		if (file.isDirectory()) { // Search directory for files
			for (File f : FileSearcher.listFiles(file, new EverythingFilter(), true)) {
				if (new MediaFilter().accept(file)) // Media file
					try {
						URL fUrl = f.toURI().toURL();
						File tagFile = new File(f.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json"));
						String fileName = f.getName();
						if (!tagFile.exists())
							results.add(new MediaURL(fUrl, fileName, guessTags(lib, fileName)));
						else {
							// Read tags from file
							TextReader jsonReader = new TextReader(tagFile);
							Gson gson = new GsonBuilder().setPrettyPrinting().create();
							@SuppressWarnings("unchecked")
							HashMap<String, Object> importTags = gson.fromJson(jsonReader.read(), HashMap.class);
							results.add(new MediaURL(fUrl, fileName, importTags));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				else { // File containing more URLs
					results.addAll(search(f));
				}
			} 
		} else { // Search text file for entries
			String fText = new TextReader(file).read();
			String fName = file.getName().toLowerCase();
			if (fName.endsWith(".url")) { // Internet shortcut
				Matcher urlMatcher = Pattern.compile("(?<=URL=)[^\\n\\r]+").matcher(fText);
				while (urlMatcher.find())
					try {
						results.addAll(search(new URL(urlMatcher.group())));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
			} else if (fName.endsWith(".txt")) { // Text file
				String[] lines = fText.replaceAll("\r", "").split("\\n");
				for (String l : lines)
					try {
						results.addAll(search(new URL(l)));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
			}
		}
		return results;
	}
	
	public void download(final MediaURL file, final File dstDir) {
		System.out.println("Started downloading from " + file.getURL());
		String name = file.getFilename(), ext = name.substring(name.lastIndexOf("."));
		final File outputFile = new File(dstDir.toString() + "/%HASH%" + ext);
		Runnable r = new Runnable() {
			@Override
			public void run() { // Save tags on finish
				MediaFile.createFrom(new File(
						outputFile.toString().replaceAll("%HASH%", downloads.getDownload(file.getURL()).getHash())),
						file.getTags());
				System.out.println("Finished downloading from " + file.getURL());
				lib.refresh();
			}
		};
		try {
			downloads.start(file.getURL(), outputFile, false, r);
		} catch (OperationNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public void findPlugins(File addonsDir, boolean recursive) {
		ArrayList<File> jars = FileSearcher.listFiles(addonsDir, new JARFilter(), recursive);
		Object[] params = { }; // Add if necessary
		grabbers = JARLoader.getInstances(DomainGrabber.class, "mediagrabber.GrabMedia",
				jars.toArray(new File[jars.size()]), params);
		setLibrary(lib);
	}
	
	// Access methods
	public void setLibrary(Library lib) {
		this.lib = lib;
		for (DomainGrabber d : grabbers)
			d.setLibrary(lib);
	}
	
	public Library getLibrary() {
		return lib;
	}
	
	public DownloadManager getDownloadManager() {
		return downloads;
	}
	
	// Utility methods
	public static HashMap<String, Object> guessTags(Library lib, String title) {
		// Auto-find tags for the video
		HashMap<String, Object> objectTags = new HashMap<String, Object>();
		HashSet<String> searchTagSet = new HashSet<String>(); // Don't allow duplicates
		if (lib != null) {
			// Check if name contains an author
			StringBuilder urlAuthorsBuilder = new StringBuilder();
			for (String s : lib.getAuthorList()) {
				String s2 = s.replace("&", "and").replaceAll("[^\\w\\s]", ""); // Remove special characters
				if (title.toLowerCase().contains(s2.toLowerCase())) { // Remove author from the name
					urlAuthorsBuilder.append("," + s);
					title = Pattern.compile(s2, Pattern.CASE_INSENSITIVE).matcher(title).replaceAll("");
				}
			}
			String urlAuthors = urlAuthorsBuilder.toString();
			String author = (!urlAuthors.isEmpty()) ? urlAuthors.substring(1) : "unknown"; // Remove 1st comma
			objectTags.put("author", author);
			// Check if name contains a tag
			for (String s : lib.getTagsList()) {
				String s2 = s.replace("&", "and").replaceAll("[^\\w\\s]", ""); // Remove special characters
				if (title.toLowerCase().contains(s2.toLowerCase()))
					searchTagSet.add(s);
			}
		}
		// Final cleanup
		ArrayList<String> searchTags = new ArrayList<String>(searchTagSet);
		Collections.sort(searchTags);
		objectTags.put("tags", searchTags);
		return objectTags;
	}
	
}
