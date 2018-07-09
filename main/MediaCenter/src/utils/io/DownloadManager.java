package utils.io;

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;

import javax.naming.OperationNotSupportedException;

import utils.io.file.Download;

public class DownloadManager {
	
	private HashMap<String, Download> downloads = new HashMap<String, Download>();
	
	// Constructor
	public DownloadManager() {
		
	}
	
	// Control methods
	/**
	 * Downloads a file from {@code sourceFile} to {@code destinationFile} and runs
	 * {@code onFinish} when done
	 * 
	 * @param sourceFile
	 *            The source file
	 * @param destinationFile
	 *            The destination file (Using "%HASH%" in the name will be replaced
	 *            by the file's hash, but only if {@code directWrite} is
	 *            {@code false})
	 * @param directWrite
	 *            Whether or not to use a temporary file for writing, then rename
	 *            when done
	 * @param onFinish
	 *            Code to run when finished or {@code null}
	 * @return The download's thread's ID
	 * @throws OperationNotSupportedException
	 *             If {@code destinationFile} contains "%HASH%" and
	 *             {@code directWrite} is {@code true}
	 */
	public String start(final URL sourceFile, final File destinationFile, boolean directWrite, Runnable onFinish)
			throws OperationNotSupportedException {
		// Check parameters
		if (destinationFile.toString().contains("%HASH%") && directWrite)
			throw new OperationNotSupportedException("");
		// Create a thread ID
		String threadID = String.format("download[%s]", sourceFile.hashCode());
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-512").digest(sourceFile.toString().getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++)
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			threadID = String.format("download[%s]", sb.toString());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// Start downloading
		final Download d;
		if (directWrite) { // Write directly to output file
			d = new Download(threadID, sourceFile, destinationFile);
			d.start(new Runnable[] { onFinish });
		} else { // Write to a temporary file and rename when done
			final File tempFile = new File(System.getProperty("user.dir") + "/temp/" + threadID.hashCode() + ".tmp"),
					tempFileParent = tempFile.getParentFile();
			if (!tempFileParent.exists())
				tempFileParent.mkdirs();
			tempFile.deleteOnExit();
			d = new Download(threadID, sourceFile, tempFile);
			// Rename when done
			Runnable r = new Runnable() {
				@Override
				public void run() {
					String hash = d.getHash();
					File newFile = new File(destinationFile.toString().replaceAll("%HASH%", hash));
					if (newFile.exists()) {
						// TODO ask about overwriting
					} else {
						File newFileParent = newFile.getParentFile();
						if (!newFileParent.exists())
							newFileParent.mkdirs();
						tempFile.renameTo(newFile);
					}
				}
			};
			d.start(new Runnable[] { r, onFinish });
		}
		downloads.put(threadID, d);
		return threadID;
	}
	
	// Access methods
	public Download getDownload(String threadID) {
		return downloads.get(threadID);
	}
	
	public Download getDownload(URL url) {
		for (Download d : downloads.values())
			if (d.getSource().sameFile(url))
				return d;
		return null;
	}
	
	public Collection<Download> getDownloads() {
		return downloads.values();
	}
	
}
