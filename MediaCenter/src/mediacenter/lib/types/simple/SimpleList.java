package mediacenter.lib.types.simple;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class SimpleList<E> implements Iterable<E> {
	
	protected ArrayList<E> values = new ArrayList<E>(0);
	
	// Constructors
	public SimpleList() {
		
	}
	
	public SimpleList(E[] es) {
		add(es);
	}
	
	public SimpleList(SimpleList<E> es) {
		add(es);
	}
	
	public SimpleList(Collection<E> es) {
		add(es);
	}
	
	// Utility methods
	public void add(E e) {
		values.add(e);
	}
	
	public void add(E[] es) {
		values.addAll(Arrays.asList(es));
	}
	
	public void add(SimpleList<E> es) {
		add(es.asList());
	}
	
	public void add(Collection<E> es) {
		values.addAll(es);
	}
	
	public void insert(int index, E e) {
		values.add(index, e);
	}
	
	public void insert(int index, E[] es) {
		values.addAll(index, Arrays.asList(es));
	}
	
	public void insert(int index, SimpleList<E> es) {
		values.addAll(index, es.asList());
	}
	
	public void insert(int index, Collection<E> es) {
		values.addAll(index, es);
	}
	
	public void remove(int index) {
		values.remove(index);
	}
	
	public void removeFirst(E e) {
		values.remove(e);
	}
	
	public void removeLast(E e) {
		for (int i = (length() - 1); i >= 0; i++)
			if (get(i) == e) {
				remove(i);
				return;
			}
	}
	
	public void removeAll(E e) {
		while (contains(e))
			removeFirst(e);
	}
	
	public void clear() {
		values.clear();
	}
	
	public void replace(int index, E e) {
		values.set(index, e);
	}
	
	public void replaceFirst(E toReplace, E element) {
		if (contains(toReplace))
			for (int i = 0; i < length(); i++)
				if (get(i) == toReplace) {
					replace(i, element);
					return;
				}
	}
	
	public void replaceLast(E toReplace, E element) {
		if (contains(toReplace))
			for (int i = (length() - 1); i >= 0; i--)
				if (get(i) == toReplace) {
					replace(i, element);
					return;
				}
	}
	
	public void replaceAll(E toReplace, E element) {
		if (contains(toReplace) && !(toReplace == element))
			while (contains(toReplace))
				replaceFirst(toReplace, element);
	}
	
	public SimpleList<E> slice(int startIndex, int endIndex) {
		SimpleList<E> result = new SimpleList<E>();
		for (int i = startIndex; i < endIndex; i++)
			result.add(get(i));
		return result;
	}
	
	// Access method
	public E get(int index) {
		return values.get(index);
	}
	
	// Info methods
	public int length() {
		return values.size();
	}
	
	public boolean isEmpty() {
		return length() == 0;
	}
	
	public boolean contains(E e) {
		return values.contains(e);
	}
	
	public boolean containsAll(SimpleList<E> es) {
		return containsAll(es.asList());
	}
	
	public boolean containsAll(Collection<E> es) {
		for (E e : es)
			if (!contains(e))
				return false;
		return true;
	}
	
	public int firstIndexOf(E e) {
		for (int i = 0; i < length(); i++)
			if (get(i).equals(e))
				return i;
		return -1; // Not contained
	}
	
	public int lastIndexOf(E e) {
		for (int i = (length() - 1); i >= 0; i--)
			if (get(i).equals(e))
				return i;
		return -1; // Not contained
	}
	
	// Conversion methods
	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private E[] asArray(Class<E> classOfList) {
		return (E[]) Array.newInstance(classOfList, length());
	}
	
	public ArrayList<E> asList() {
		return values;
	}
	
	// Override methods
	@Override
	/** Override of standard Java method */
	public Iterator<E> iterator() {
		return values.iterator();
	}
	
	@Override
	/** Override of standard Java method */
	public String toString() {
		return values.toString();
	}
	
}
