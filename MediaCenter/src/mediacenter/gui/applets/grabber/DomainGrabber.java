package mediacenter.gui.applets.grabber;

import java.net.URL;

import mediacenter.lib.types.io.url.MediaURL;
import mediacenter.lib.types.media.Library;
import mediacenter.lib.types.simple.SimpleList;

public interface DomainGrabber {
	
	/** Returns the name of the website(s) that this {@code DomainGrabber} works on */
	public String getSiteName();
	
	/** Returns the regex that this {@code DomainGrabber} JAR will match (the site address) */
	public String getSiteRegex();
	
	
	/** This method should return all important media found on a page when given a URL */
	public SimpleList<MediaURL> search(URL url); 
	
	
	/** The library to use for automatic tag finding */
	public void setLibrary(Library lib);
	
}
