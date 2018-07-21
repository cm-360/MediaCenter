package mediacenter.lib.utils.io;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;

import javax.naming.OperationNotSupportedException;
import javax.swing.JDialog;

import mediacenter.gui.popups.frames.ComfirmPrompt;
import mediacenter.lib.types.io.Download;

public class DownloadManager {
	
	private static HashMap<String, Download> downloads = new HashMap<String, Download>();
	
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
	public static String start(final URL sourceFile, final File destinationFile, boolean directWrite, final Runnable onFinish)
			throws OperationNotSupportedException {
		// Check parameters
		if (destinationFile.toString().contains("%HASH%") && directWrite)
			throw new OperationNotSupportedException("");
		// Create a thread ID
		String threadID = String.format("download[%s]", sourceFile.hashCode()); // Default
		do {
			try {
				byte[] bytes = MessageDigest.getInstance("SHA-256").digest(threadID.getBytes());
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < bytes.length; i++)
					sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
				threadID = String.format("download[%s]", sb.toString());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} while (downloads.containsKey(threadID)); // Prevent key conflicts
		final String threadIDClone = new String(threadID);
		// Start downloading
		final Download d;
		if (directWrite) { // Write directly to output file
			if (destinationFile.exists()) { // Confirm overwrite
				final ComfirmPrompt cp = new ComfirmPrompt("Confirm Overwrite",
						String.format("\"%s\" will be overwritten.", destinationFile.toString()));
				cp.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				// OK button
				cp.getOKButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Download d = new Download(threadIDClone, sourceFile, destinationFile);
						d.start(new Runnable[] { onFinish });
						cp.dispose();
					}
				});
				cp.setAlwaysOnTop(true);
				cp.setVisible(true);
			} else {
				d = new Download(threadID, sourceFile, destinationFile);
				d.start(new Runnable[] { onFinish });
				downloads.put(threadID, d);
			}
		} else { // Write to a temporary file and rename when done
			final File tempFile = new File("./documents/temp/" + threadID + ".tmp"),
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
					if (newFile.exists()) { // Confirm overwrite
						if (newFile.length() == tempFile.length()) {
							final ComfirmPrompt cp = new ComfirmPrompt("Confirm Overwrite",
									String.format("\"%s\" will be overwritten.",
											destinationFile.toString().replaceAll("%HASH%", hash)));
							cp.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							// OK button
							cp.getOKButton().addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									tempFile.renameTo(newFile);
									cp.dispose();
								}
							});
							cp.setAlwaysOnTop(true);
							cp.setVisible(true);
						} else { // Hash conflict with different files
							int i = 0; File nf2;
							do {
								nf2 = new File(newFile.toString() + "-" + (i++));
							} while (nf2.exists());
							tempFile.renameTo(nf2);
						}
					} else {
						File newFileParent = newFile.getParentFile();
						if (!newFileParent.exists())
							newFileParent.mkdirs();
						tempFile.renameTo(newFile);
					}
				}
			};
			d.start(new Runnable[] { onFinish, r });
			downloads.put(threadID, d);
		}
		return threadID;
	}
	
	// Access methods
	public static Download getDownload(String threadID) {
		return downloads.get(threadID);
	}
	
	public static Download getDownload(URL url) {
		for (Download d : downloads.values())
			if (d.getSource().sameFile(url))
				return d;
		return null;
	}
	
	public static Collection<Download> getDownloads() {
		return downloads.values();
	}
	
}
