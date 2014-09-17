package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.List;

public interface LearningAlgorithm {
	public void teach(List<Instance> examples);
	public int getResult(List<Double> vector);
}
