package mediacenter.lib.utils.io.url;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import mediacenter.lib.types.simple.SimpleList;

public class JARLoader {
	
	@SuppressWarnings("unchecked") // IDE doesn't see the casting check
	public static <T> SimpleList<T> getInstances(Class<T> type, String className, SimpleList<File> jarFiles,
			Object[] constructorParams) {
		SimpleList<T> results = new SimpleList<T>();
		URLClassLoader classLoader; // Class loader
		for (File j : jarFiles) // Check one at a time
			try {
				URL jarURL = j.toURI().toURL(); // Convert File to URL
				classLoader = new URLClassLoader(new URL[] { jarURL }, JARLoader.class.getClassLoader());
				Class<?> newClass = (Class<?>) Class.forName(className, true, classLoader);
				// Get class array from object array
				Class<?>[] constructorClasses = new Class<?>[constructorParams.length];
				for (int i = 0; i < constructorParams.length; i++)
					constructorClasses[i] = constructorParams[i].getClass();
				// Create and cast the object
				Object instance = newClass.getDeclaredConstructor(constructorClasses).newInstance(constructorParams);
				if (type.isInstance(instance)) // Check before casting
					results.add((T) instance);
			} catch (Exception e) { // Class couldn't be loaded
				e.printStackTrace();
			}
		System.out.println(String.format("Added class \"%s\" from %d of %d jar(s) to the classpath", className,
				results.length(), jarFiles.length()));
		return results;
	}
	
}
