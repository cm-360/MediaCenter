package utils.swing;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JComponent;

public class ComponentMap {
	
	private HashMap<String, JComponent> componentMap = new HashMap<String, JComponent>();
	
	// Constructor
	public ComponentMap(JComponent parent) {
		componentMap = find(parent);
	}
	
	// Access methods
	public HashMap<String, JComponent> getComponents() {
		return componentMap;
	}
	
	public JComponent getComponent(String name) {
		return componentMap.get(name);
	}
	
	private HashMap<String, JComponent> find(JComponent parent) {
		HashMap<String, JComponent> results = new HashMap<String, JComponent>();
		for (Component c : parent.getComponents()) {
			JComponent jc = (JComponent) c;
			results.put(jc.getName(), jc);
			results.putAll(find((JComponent) jc));
		}
		return results;
	}
	
}
