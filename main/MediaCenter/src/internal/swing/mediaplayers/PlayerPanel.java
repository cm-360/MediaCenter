package internal.swing.mediaplayers;

import utils.io.file.MediaFile;

public interface PlayerPanel {

	// Control methods
	public void play(MediaFile m);
	
	public void pause();
	
	public void stop();
	
	// Status methods
	public boolean isPaused();
	
	public boolean isStopped();
	
	// Access methods
	public String[] getSupportedExtensions();
	
}
