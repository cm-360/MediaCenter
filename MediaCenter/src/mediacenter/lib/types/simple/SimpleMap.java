package mediacenter.lib.types.simple;

import java.util.HashMap;
import java.util.Map;

public class SimpleMap<K, V> {
	
	private SimpleList<K> keys = new SimpleList<K>();
	private SimpleList<V> objs = new SimpleList<V>();
	
	// Constructors
	public SimpleMap() {
		
	}
	
	public SimpleMap(Map<K, V> map) {
		put(map);
	}
	
	// Utility methods
	public void put(K key, V value) {
		int i = keys.firstIndexOf(key);
		if (i == -1) { // Not contained
			keys.add(key);
			objs.add(value);
		} else {
			objs.replace(i, value);
		}
	}
	
	public void put(SimpleMap<K, V> map) {
		for (K key : map.keys)
			put(key, map.get(key));
	}
	
	public void put(Map<K, V> map) {
		for (K key : map.keySet())
			put(key, map.get(key));
	}
	
	public V get(K key) {
		int i = keys.firstIndexOf(key);
		if (i != -1)
			return objs.get(i);
		else
			return null;
	}
	
	public void remove(K key) {
		int i = keys.firstIndexOf(key);
		if (i != -1) { // Not contained
			keys.remove(i);
			objs.remove(i);
		}
	}
	
	public void clear() {
		keys.clear();
		objs.clear();
	}
	
	// Access methods
	/**
	 * Gets the keys for this {@code SimpleMap}
	 * 
	 * @return A read-only array of the keys for this map
	 */
	public SimpleList<K> getKeys() {
		return new SimpleList<K>(keys);
	}
	
	/**
	 * Gets the values stored in this {@code SimpleMap}
	 * 
	 * @return A read-only array of the values in this map
	 */
	public SimpleList<V> getValues() {
		return new SimpleList<V>(objs);
	}
	
	// Conversion method
	/**
	 * Creates a new {@code HashMap} to represent this {@code SimpleMap} for compatibility
	 * 
	 * @return A new, filled {@code HashMap} with types already set
	 */
	public HashMap<K, V> getHashMap() {
		HashMap<K, V> result = new HashMap<K, V>();
		for (K key : keys)
			result.put(key, get(key));
		return result;
	}
	
	// Override methods
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (K key : keys) {
			sb.append(key.toString() + ":" + get(key));
			if (keys.lastIndexOf(key) != (keys.length() - 1))
				sb.append(", ");
		}
		return sb.toString() + "]";
	}
	
}
