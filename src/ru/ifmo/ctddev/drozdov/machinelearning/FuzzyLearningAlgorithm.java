package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.List;

public interface FuzzyLearningAlgorithm extends LearningAlgorithm {
	public double getProbability(List<Double> vector);
}
