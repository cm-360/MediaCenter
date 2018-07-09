package internal.utils.library;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;

import internal.swing.mediaplayers.PlayerPanel;
import utils.io.file.FileSearcher;
import utils.io.file.MediaFile;
import utils.swing.ComponentMap;

public class Library {
	
	private File[] libraryDirs;
	private FileFilter libraryFilter;
	private ArrayList<MediaFile> files;
	private JList<MediaFile> filesList;
	
	private HashSet<String> authorList = new HashSet<String>();
	private HashSet<String> tagList = new HashSet<String>();
	
	private Thread searchThread;
	
	private ComponentMap mediaPlayers;
	
	
	// Constructors
	public Library(File[] dirs, FileFilter filter, ComponentMap map) {
		this(dirs, filter, map, null);
	}
	
	public Library(File[] dirs, FileFilter filter, ComponentMap map, JList<MediaFile> list) {
		libraryDirs = dirs;
		libraryFilter = filter;
		filesList = list;
		mediaPlayers = map;
		refresh();
	}
	
	// Control methods
	public void sort() {
		if (files != null) {
			Collections.sort(files);
			filesList.setListData(files.toArray(new MediaFile[files.size()]));
		}
	}
	
	public void search(final String query, int mode) {
		long startTime = System.nanoTime(); // For debugging
		
		// Prepare everything for search
		sort();
		ArrayList<MediaFile> results = new ArrayList<MediaFile>();
		String sq = query.toLowerCase(); // Private query string
		// Find phrases in query string
		ArrayList<String> phrases = new ArrayList<String>();
		Matcher phraseMatcher = Pattern.compile("(?<=\\\")[^@#\\\"]+(?=\\\")").matcher(sq);
		while (phraseMatcher.find())
			phrases.add(phraseMatcher.group().toLowerCase());
		sq = sq.replaceAll("\\\"[^@#\\\"]+\\\"", ""); // Cleanup
		// Find tags in query string
		ArrayList<String> tags = new ArrayList<String>();
		Matcher tagMatcher = Pattern.compile("(?<=#)\\w+").matcher(sq);
		while (tagMatcher.find())
			tags.add(tagMatcher.group().toLowerCase());
		sq = sq.replaceAll("#\\w+", ""); // Cleanup
		// Find authors in query string
		ArrayList<String> authors = new ArrayList<String>();
		Matcher authorMatcher = Pattern.compile("(?<=@)\\w+").matcher(sq);
		while (authorMatcher.find())
			authors.add(authorMatcher.group().toLowerCase());
		sq = sq.replaceAll("@\\w+", ""); // Cleanup
		
		// Search the library
		String[] filterTypes = { null, "image", "music", "video" };
		if (mode == filterTypes.length) // Playlist
			; // TODO search playlist
		else
			for (MediaFile m : files) {
				if (!(mode == 0)) { // Specific types
					String ext = ""; // File extension
					Matcher extMatcher = Pattern.compile("(?<=\\.)[^\\\\\\/\\.]+(?=$)").matcher(m.getFilePath().toString());
					boolean accepted = false;
					if (extMatcher.find()) {
						ext = extMatcher.group();
						for (Component c : mediaPlayers.getComponents().values()) { // List of possible players
							PlayerPanel p = (PlayerPanel) c;
							for (String regex : p.getSupportedExtensions())
								if (ext.matches(regex) && (filterTypes[mode].equals(c.getName())))
									accepted = true;
						}
					}
					if (!accepted)
						continue;
				}
				Object authorO = m.getTag("author");
				if (authors.isEmpty() || authors.contains((authorO == null) ? "unknown" : authorO.toString().toLowerCase())) { // Author check
					boolean containsAll = true;
					for (String p : phrases) // Match exact phrases
						if (!m.toString().toLowerCase().contains(p)) {
							containsAll = false;
							break; // End the loop to save time
						}
					if (containsAll) { // Phrase check
						try {
							Object mTagsO = m.getTag("tags");
							@SuppressWarnings("unchecked")
							ArrayList<String> mTags = (mTagsO == null) ? new ArrayList<String>() : ((ArrayList<String>) mTagsO);
							if (tags.isEmpty() || mTags.containsAll(tags)) { // Tags check
								sq = sq.replaceAll("\\s+", " "); // Final query cleanup
								if (sq.replaceAll("\\s", "").isEmpty())
									results.add(m);
								else if (Arrays.asList(m.toString().toLowerCase().split("\\s"))
										.containsAll(Arrays.asList(sq.split("\\s"))))
									results.add(m);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		// Finalize results
		Collections.sort(results);
		filesList.setListData(results.toArray(new MediaFile[results.size()]));
		
		// For debugging
		long endTime = System.nanoTime();
		System.out.println("Finished search for \"" + query + "\" in " + (endTime - startTime) + " nanoseconds");
	}
	
	public void refresh() {
		searchThread = new Thread(null, new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				synchronized (searchThread) {
					files = new ArrayList<MediaFile>();
					for (File libraryDir : libraryDirs) {
						if (libraryDir.exists()) {
							ArrayList<File> results = FileSearcher.listFiles(libraryDir, libraryFilter, true);
							for (File f : results) {
								MediaFile m = new MediaFile(f);
								// Add tags to the global list
								Object mTags = m.getTag("tags");
								if (mTags != null && ArrayList.class.isInstance(mTags))
									tagList.addAll((List<String>) mTags);
								// Add author to the global list
								Object mAuthor = m.getTag("author");
								if (mAuthor != null)
									authorList.add(mAuthor.toString());
								files.add(m);
							}
						} else
							libraryDir.mkdirs(); // Create a library folder if it doesn't exist
						if (filesList != null)
							filesList.setListData(files.toArray(new MediaFile[files.size()]));
					}
					sort();
				}
			}
		}, "Library-SearchThread#" + new Random().nextInt());
		synchronized (searchThread) {
			try {
				searchThread.start(); // Search for files in a separate thread
				searchThread.wait(10000); // Wait 10 seconds at maximum
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.gc();
	}
	
	// Access methods
	public ArrayList<MediaFile> getFiles() {
		return files;
	}
	
	public JList<MediaFile> getJList() {
		return filesList;
	}
	
	/** A list of every search tag used on media files in this library */
	public HashSet<String> getTagsList() {
		return tagList;
	}
	
	/** A list of every author of media files in this library */
	public HashSet<String> getAuthorList() {
		return authorList;
	}
	
	public ComponentMap getMediaPlayers() {
		return mediaPlayers;
	}
	
}
