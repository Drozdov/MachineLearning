package ru.ifmo.ctddev.drozdov.ml.bayesnetworks;

public class Evidence {
	public int id;
	public String name;
	public boolean value;
	
	public Evidence(int id, boolean value, String[] names) {
		this.id = id;
		this.value = value;
		if (names != null)
			this.name = names[id];
	}
	
	public Evidence(int id, boolean value) {
		this(id, value, null);
	}
	
	public Evidence(String name, boolean value, String[] names) {
		this.value = value;
		this.name = name;
		if (names == null)
			return;
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				id = i;
				return;
			}
		}
	}
}
