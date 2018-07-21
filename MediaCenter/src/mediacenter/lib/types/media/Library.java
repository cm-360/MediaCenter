package mediacenter.lib.types.media;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.types.simple.SimpleList;
import mediacenter.lib.utils.io.file.FileSearcher;
import mediacenter.lib.utils.io.file.filters.MediaFilter;

public class Library {
	
	public enum MediaType {
		All, Images, Music, Videos, Playlists,
	};
	
	private SimpleList<MediaFile> contents = new SimpleList<MediaFile>();
	
	// Utility methods
	/**
	 * Empties then refills this {@code Library} with media files found in the given
	 * directories
	 * 
	 * @param dirs
	 *            The directories to search
	 */
	public void refresh(final SimpleList<File> dirs) {
		contents.clear();
		SimpleList<File> results = new SimpleList<File>();
		for (File d : dirs)
			results.add(FileSearcher.listFiles(d, new MediaFilter(), true));
		for (File f : results)
			contents.add(new MediaFile(f));
		System.gc(); // Clean up artifacts
	}
	
	/**
	 * Searches this library for {@code MediaFile}s
	 * 
	 * @param query
	 *            The search query
	 * @return A list of all matching results
	 */
	public SimpleList<MediaFile> search(final String query) {
		SimpleList<MediaFile> results = new SimpleList<MediaFile>();
		// Break down query string
		String qClone = new String(query), group;
		String quoteRegex = "\"[\\w\\s]+\"", paramRegex = "%s(?:\\w+|(?:%s))", symbolRegex = "[^\\w\\s]";
		Matcher queryMatcher;
		// Find authors
		SimpleList<String> authors = new SimpleList<String>();
		queryMatcher = Pattern.compile(String.format(paramRegex, "@", quoteRegex)).matcher(query);
		while (queryMatcher.find()) {
			authors.add((group = queryMatcher.group()).toLowerCase().replaceAll(symbolRegex, ""));
			qClone = qClone.replace(group, ""); // Remove from query string
		}
		// Find tags
		SimpleList<String> tags = new SimpleList<String>();
		queryMatcher = Pattern.compile(String.format(paramRegex, "#", quoteRegex)).matcher(query);
		while (queryMatcher.find()) {
			tags.add((group = queryMatcher.group()).toLowerCase().replaceAll(symbolRegex, ""));
			qClone = qClone.replace(group, ""); // Remove from query string
		}
		// Find exact phrases
		SimpleList<String> phrases = new SimpleList<String>();
		queryMatcher = Pattern.compile(quoteRegex).matcher(query);
		while (queryMatcher.find()) {
			phrases.add((group = queryMatcher.group()).toLowerCase().replaceAll(symbolRegex, ""));
			qClone = qClone.replace(group, ""); // Remove from query string
		}
		qClone = qClone.replaceAll(symbolRegex, "").replaceAll("\\s+", " ").trim(); // Cleanup
		phrases.add(new SimpleList<String>(qClone.split("\\s"))); // Find remaining words
		// Search through
		for (MediaFile m : contents)
			// Check each search criteria
			if (authors.isEmpty() || authors.contains(m.getTag("author").toLowerCase().replaceAll(symbolRegex, "")))
				if (tags.isEmpty() || new SimpleList<String>(m.getTag("tags").toLowerCase().replaceAll(symbolRegex, "").split(",")).containsAll(tags)) {
					boolean containsAll = true;
					for (String p : phrases)
						if (!m.getTag("title").toLowerCase().replaceAll(symbolRegex, "").contains(p)) {
							containsAll = false;
							break; // More iterations are not needed
						}
					if (containsAll) // All criteria met
						results.add(m);
				}
		System.gc(); // Clean up artifacts
		return results;
	}
	
	/**
	 * Returns the list of media files housed in this library
	 * 
	 * @return A list of all {@code MediaFile}s in this {@code Library}
	 */
	public SimpleList<MediaFile> getContents() {
		return contents;
	}
	
	public SimpleList<String> getDataValues(String key) {
		SimpleList<String> results = new SimpleList<String>();
		for (MediaFile m : contents) {
			String value = m.getTag(key).toLowerCase();
			if (!results.contains(value))
				results.add(value);
		}
		return results;
	}

}