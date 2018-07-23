package mediacenter.lib.types.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mediacenter.lib.utils.io.url.HTMLGrabber;

public class Download {
	
	private Thread thread;
	private boolean pause = false, stop = false;
	private String threadID, hash = "waiting";
	
	private URL source;
	private File destination;
	
	private int bytesCurrent, bytesTotal;
	
	public Download(final String threadID, final URL source, final File destination) {
		this.threadID = threadID;
		this.source = source;
		this.destination = destination;
	}
	
	// Control methods
	public void start(final Runnable[] onFinish) {
		thread = new Thread(null, new Runnable() {
			@Override
			public void run() {
				synchronized (thread) {
					try {
						hash = getChecksumAndWrite(MessageDigest.getInstance("SHA-256"), source, destination);
						if (onFinish != null) {
							for (Runnable r : onFinish)
								r.run();
						}
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
			}
		}, threadID);
		thread.start();
	}
	
	public void pause() {
		pause = true;
	}
	
	public void resume() {
		pause = false;
		synchronized (thread) {
			try {
				thread.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// Access methods
	public URL getSource() {
		return source;
	}
	
	public File getDestination() {
		return destination;
	}
	
	public String getThreadID() {
		return threadID;
	}
	
	public String getHash() {
		return hash;
	}
	
	public int getBytesCurrent() {
		return bytesCurrent;
	}
	
	public int getBytesTotal() {
		return bytesTotal;
	}
	
	// Utility methods
	public String getChecksumAndWrite(MessageDigest md, URL input, File output) {
		InputStream uis = null;
		BufferedOutputStream bos = null;
		try {
			// Open a connection and set the "User-Agent" property
			URLConnection conn = input.openConnection();
			conn.setRequestProperty("User-Agent", HTMLGrabber.USER_AGENT);
			bytesTotal = conn.getContentLength();
			uis = conn.getInputStream();
			bos = new BufferedOutputStream(new FileOutputStream(output));
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;
			// Read and update in digest
			while (((bytesCount = uis.read(byteArray, 0, 1024)) != -1) && !stop) {
				md.update(byteArray, 0, bytesCount);
				bos.write(byteArray, 0, bytesCount);
				bytesCurrent += bytesCount;
				if (pause) {
					synchronized (thread) {
						try {
							thread.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				uis.close();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Return the hash in hex
		byte[] bytes = md.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	// Makes the object show up properly in a list
	@Override
	public String toString() {
		if (bytesTotal != 0)
			return String.format("[%d%%]\t %s", ((int) (((double) bytesCurrent) / ((double) bytesTotal) * 100.0)),
					source.toString());
		else
			return "Waiting...";
	}

}
