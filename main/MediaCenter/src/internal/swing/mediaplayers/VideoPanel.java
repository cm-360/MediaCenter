package internal.swing.mediaplayers;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import utils.io.file.Download;
import utils.io.file.MediaFile;
import utils.io.file.text.TextReader;

public class VideoPanel extends JPanel implements PlayerPanel {
	
	private static final long serialVersionUID = 1L;
	
	private String[] supportedExtensions;
	
	private CardLayout cl;
	private Container parent;
	
	public VideoPanel(CardLayout cl, Container parent) {
		this.cl = cl;
		this.parent = parent;
		final File extFile = new File(System.getProperty("user.dir") + "/config/mediaplayers/video/extensions.txt");
		if (extFile.exists())
			supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
		else {
			try {
				extFile.getParentFile().mkdirs();
				Download d = new Download(toString(), getClass().getResource("/resources/files/config/mediaplayers/video/extensions.txt"), extFile);
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
	}
	
	// Control methods
	@Override
	public void play(MediaFile m) {
		if (Desktop.isDesktopSupported()) {
			// TODO dont always use the system default
			try {
				Desktop.getDesktop().open(m.getFilePath());
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		cl.show(parent, "video");
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	public void setVolume(double percent) {
		// TODO set volume
	}
	
	// Status methods
	@Override
	public boolean isPaused() {
		return false;
	}

	@Override
	public boolean isStopped() {
		return true;
	}
	
	// Access methods
	@Override
	public String[] getSupportedExtensions() {
		return supportedExtensions;
	}

}
