package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.List;

public class Instance {
	public List<Double> vector;
	public int value;

	public Instance(List<Double> vector, int classId) {
		this.value = classId;
		this.vector = vector;
	}
}
