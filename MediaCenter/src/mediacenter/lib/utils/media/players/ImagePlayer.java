package mediacenter.lib.utils.media.players;

import javax.swing.JPanel;

import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.utils.media.MediaPlayer;

public class ImagePlayer extends JPanel implements MediaPlayer {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String[] getSupportedFileExts() {
		return new String[] {
				"bmp", "jpeg", "jpg", "png"
		};
	}

	@Override
	public void play(MediaFile m, Runnable onReady, Runnable onFinish) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seek(int time) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getMediaLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMediaTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MediaFile getMedia() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVolume(double volume) {
		// Images don't have sound
	}
	
	
	
}
