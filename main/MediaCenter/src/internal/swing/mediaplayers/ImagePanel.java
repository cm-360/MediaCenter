package internal.swing.mediaplayers;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import utils.graphics.ImageTools;
import utils.graphics.ImageTools.ScaleMode;
import utils.io.file.Download;
import utils.io.file.MediaFile;
import utils.io.file.text.TextReader;

public class ImagePanel extends JPanel implements PlayerPanel {
	
	private static final long serialVersionUID = 1L;
	
	private String[] supportedExtensions;
	
	private CardLayout cl;
	private Container parent;
	
	private Image image;
	private Thread gifThread; // TODO fix gifs
	
	private ScaleMode scaleMode = ScaleMode.Fit; // Default
	
	public ImagePanel(CardLayout cl, Container parent) {
		this.cl = cl;
		this.parent = parent;
		final File extFile = new File(System.getProperty("user.dir") + "/config/mediaplayers/image/extensions.txt");
		if (extFile.exists())
			supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
		else {
			try {
				extFile.getParentFile().mkdirs();
				Download d = new Download(toString(), getClass().getResource("/resources/files/config/mediaplayers/image/extensions.txt"), extFile);
				d.start(new Runnable[] { new Runnable() {
					@Override
					public void run() {
						supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
					}
				}});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		if (image != null) {
			if (scaleMode == ScaleMode.Stretch) {
				g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			} else if (scaleMode == ScaleMode.Fill) {
				ImageTools.centerImage(image, parent, g, true);
			} else if (scaleMode == ScaleMode.Fit) {
				ImageTools.centerImage(image, parent, g, false);
			}
		}
	}
	
	// Control methods
	@Override
	public void play(MediaFile m) {
		if (m != null) {
			try {
				final File mFile = m.getFilePath();
				image = new ImageIcon(mFile.toURI().toURL()).getImage();
				gifThread = new Thread(null, new Runnable() {
					@Override
					public void run() {
						while (mFile.toString().toLowerCase().endsWith(".gif"))
							repaint();
					}
				}, toString() + "-GIFThread");
				gifThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Show panel when done
		cl.show(parent, "image");
	}
	
	@Override
	public void pause() {
		// Images can't be paused
	}
	
	@Override
	public void stop() {
		image = null;
	}
	
	// Status methods
	@Override
	public boolean isPaused() {
		return false; // Images can't be paused
	}
	
	@Override
	public boolean isStopped() {
		return image == null;
	}
	
	// Access methods
	@Override
	public String[] getSupportedExtensions() {
		return supportedExtensions;
	}
	
	// Custom methods
	public void setScaleMode(ScaleMode scaleMode) {
		this.scaleMode = scaleMode;
	}

}
