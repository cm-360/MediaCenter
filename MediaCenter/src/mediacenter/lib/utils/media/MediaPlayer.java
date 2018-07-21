package mediacenter.lib.utils.media;

import mediacenter.lib.types.io.file.MediaFile;

public interface MediaPlayer {
	
	// Info methods
	/**
	 * Gets the name of this {@code MediaPlayer}
	 * 
	 * @return The name of this player
	 */
	public String getName();
	
	/**
	 * Returns a list of file extensions this {@code MediaPlayer} supports
	 * 
	 * @return A list of supported file extensions
	 */
	public String[] getSupportedFileExts();
	
	// Utility methods
	/**
	 * Plays the given {@code MediaFile} or resumes playing if the file is
	 * {@code null}
	 * 
	 * @param m
	 *            The media to play
	 * @param onFinish
	 *            The runnable to run when finished playing
	 */
	public void play(final MediaFile m, final Runnable onReady, final Runnable onFinish);

	/**
	 * Pauses playback (or does nothing if already paused)
	 */
	public void pause();

	/**
	 * Stops playback
	 */
	public void stop();

	/**
	 * Seeks to the given time in the current {@code MediaFile}
	 * 
	 * @param time
	 *            The time in milliseconds
	 */
	public void seek(int time);
	
	/**
	 * Gets the length of the current {@code MediaFile}
	 * 
	 * @return The file duration in milliseconds
	 */
	public int getMediaLength();

	/**
	 * Gets the current position of playback in the current {@code MediaFile}
	 * 
	 * @return The current time in milliseconds
	 */
	public int getMediaTime();
	
	/**
	 * Gets the file currently being played
	 * 
	 * @return The {@code MediaFile} being played
	 */
	public MediaFile getMedia();
	
	/**
	 * Check if this player is paused
	 * 
	 * @return Whether the current media is paused or not
	 */
	public boolean isPaused();
	
	/**
	 * Check if this player is stopped
	 * 
	 * @return Whether the current media is stopped or not
	 */
	public boolean isStopped();
	
	/**
	 * Changes the volume of this player
	 * 
	 * @param volume
	 *            The volume to change to as a double (0.0-1.0)
	 */
	public void setVolume(double volume);
	
}
