package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.List;

public class LogisticRegression implements FuzzyLearningAlgorithm {

	private int dim;
	private double[] teta;
	private final static double MIN_DIFF = 1e-8;
	private final static double ALPHA = 0.05;
	private boolean free;
	private final static int MAX_ITERS = 10000000;
	
	public LogisticRegression(int dim, boolean free) {
		this.dim = dim;
		this.free = free;
		if (free)
			this.dim++;
		teta = new double[this.dim];
	}
	
	private double get(List<Double> list, int i) {
		if (free) {
			return i == 0 ? 1 : list.get(i - 1);
		} else {
			return list.get(i);
		}
	}
	
	@Override
	public void teach(List<Instance> examples) {
		double dx = Double.MAX_VALUE;
		int j = 0;
		while (dx > MIN_DIFF) {
			dx = 0;
			double[] dt = gradientDescent(examples);
			for (int i = 0; i < dim; i++) {
				teta[i] += dt[i];
				dx += dt[i] * dt[i];
			}
			if (j++ > MAX_ITERS)
				break;
		}
	}
	
	private double[] gradientDescent(List<Instance> examples) {
		double[] res = new double[dim];
		for (Instance instance : examples) {
			double coeff = ALPHA * 
					(instance.value - getProbability(instance.vector));
			for (int i = 0; i < dim; i++) {
				res[i] += coeff * get(instance.vector, i);
			}
		}
		return res;
	}

	@Override
	public int getResult(List<Double> vector) {
		return getProbability(vector) >= 0.5 ? 1 : 0;
	}
	
	private double getProbability(double value) {
		return 1. / (1 + Math.exp(-value));
	}
	
	@Override
	public double getProbability(List<Double> vector) {
		double res = 0;
		for (int i = 0; i < dim; i++) {
			res += teta[i] * get(vector, i);
		}
		return getProbability(res);
	}
	
}
