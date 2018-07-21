package mediacenter.lib.types.media;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.types.io.file.TextFile;
import mediacenter.lib.types.simple.SimpleList;
import mediacenter.lib.types.simple.SimpleMap;
import mediacenter.lib.utils.media.MediaPlayer;

public class Playlist extends SimpleList<MediaFile> {
	
	private int currentIndex = 0;
	private boolean loop = false;
	
	private SimpleMap<String, MediaPlayer> players = new SimpleMap<String, MediaPlayer>();
	private String currentPlayer = "image"; // Default
	
	private Runnable whilePlaying;
	
	private Gson gson;
	
	// Constructor
	public Playlist(final SimpleList<MediaPlayer> players, final Runnable whilePlaying) {
		for (MediaPlayer p : players) {
			this.players.put(p.getName(), p);
			p.setVolume(1.0); // Default
		}
		this.whilePlaying = whilePlaying;
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	// IO methods
	public void load(SimpleList<MediaFile> files) {
		clear();
		add(files);
	}
	
	@SuppressWarnings("unchecked")
	public void loadFrom(final TextFile f) {
		String json = f.readContents();
		if (!json.isEmpty()) {
			clear();
			ArrayList<MediaFile> read = gson.fromJson(json, ArrayList.class);
			if (read != null)
				add(read);
			else
				; // TODO json could not be read
		}
	}
	
	public void writeTo(final TextFile f) {
		if (!f.writeContents(gson.toJson(asList(), ArrayList.class)))
			; // TODO json could not be written
	}
	
	// Control methods
	public void play(Runnable onReady) {
		if (currentIndex == length()) // End boundary
			currentIndex = 0; // Reset index
		Runnable onFinish = new Runnable() {	
			public void run() { // Runs on media finish
				skipForward(onReady);
			}
		};
		if (!isPaused())
			stop();
		// Play file with appropriate player
		MediaFile toPlay = get(currentIndex);
		for (MediaPlayer mp : players.getValues()) {
			String name = toPlay.getMediaFile().getName();
			if (new SimpleList<String>(mp.getSupportedFileExts())
					.contains(name.toLowerCase().substring(name.lastIndexOf(".") + 1))) {
				currentPlayer = mp.getName(); // Change current player string
				mp.play(toPlay, new Runnable() {
					public void run() {
						onReady.run(); // Runnable from arguments
						new Thread(null, new Runnable() {
							public void run() {
								while (!isStopped()) {
									try {
										Thread.sleep(100);
										whilePlaying.run();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						}, "ControlSeekPanel-Refresher").start();
					}
				}, onFinish);
			}
		}
	}
	
	public void pause() {
		players.get(currentPlayer).pause();
	}
	
	public void skipForward(Runnable onReady) {
		stop();
		currentIndex++;
		if (currentIndex == length()) { // End boundary
			currentIndex = 0; // Reset index
			if (loop)
				play(onReady); // Resume playback from beginning
		} else
			play(onReady);
	}
	
	public void skipBackward(Runnable onReady) {
		stop();
		if (currentIndex != 0) // Front boundary
			currentIndex--;
		play(onReady);
	}
	
	public void stop() {
		players.get(currentPlayer).stop();
	}
	
	public void shuffle() {
		// TODO shuffle the playlist
	}
	
	public void setLooping(boolean loop) {
		this.loop = loop;
	}
	
	public void seek(int millis) {
		players.get(currentPlayer).seek(millis);
	}
	
	// Access methods
	public SimpleList<MediaPlayer> getPlayers() {
		return players.getValues();
	}
	
	// Info methods
	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public MediaFile getCurrentMedia() {
		return players.get(currentPlayer).getMedia();
	}
	
	public String getCurrentPlayer() {
		return currentPlayer;
	}
	
	public int getCurrentTime() {
		return players.get(currentPlayer).getMediaTime();
	}
	
	public int getEndTime() {
		return players.get(currentPlayer).getMediaLength();
	}
	
	public boolean isPaused() {
		return players.get(currentPlayer).isPaused();
	}
	
	public boolean isStopped() {
		return players.get(currentPlayer).isStopped();
	}
	
	public boolean getLooping() {
		return loop;
	}
	
}
