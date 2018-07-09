package utils.applets.grabber;

import java.net.URL;
import java.util.ArrayList;

import internal.utils.library.Library;
import utils.io.url.MediaURL;

public interface DomainGrabber {
	
	/** Returns the name of the website(s) that this {@code DomainGrabber} works on */
	public String getSiteName();
	
	/** Returns the regex that this {@code DomainGrabber} JAR will match (the site address) */
	public String getSiteRegex();
	
	
	/** This method should return all important media found on a page when given a URL */
	public ArrayList<MediaURL> search(URL url); 
	
	
	/** The library to use for automatic tag finding */
	public void setLibrary(Library lib);
	
}
