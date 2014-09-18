package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KNN implements LearningAlgorithm {

	private List<Instance> examples;
	private int dim;
	private int k;
	
	public KNN(int k, int dim) {
		this.k = k;
		this.dim = dim;
	}
	
	@Override
	public void teach(List<Instance> examples) {
		int size = examples.size();
		this.examples = examples.subList(0, 3 / 4 * size);
		chooseK(examples.subList(3 / 4 * size, size));
	}

	private void chooseK(List<Instance> list) {
		int k1 = 1, k4  = 100;
		int k2 = (k1 + k4) / 3;
		int k3 = (k1 + k4) * 2 / 3;
		int v2, v3;
		do {
			v2 = test(k2, list);
			v3 = test(k3, list);
			if (v2 >= v3) {
				k4 = k3;
			} else {
				k1 = k2;
			}
			k2 = k1 + (k4 - k1) / 3;
			k3 = k1 + (k4 - k1) * 2 / 3;
		} while (k3 - k2 > 1);
		k = (v2 > v3 ? k2 : k3);
		examples.addAll(list);
	}
	
	private int test(int k, List<Instance> list) {
		this.k = k;
		int res = 0;
		for (Instance inst : list) {
			res += getResult(inst.vector) == inst.value ? 1 : 0;
		}
		return res;
	}

	@Override
	public int getResult(final List<Double> vector) {
		List<Instance> sortedExamples = new ArrayList<>(examples);
		Collections.sort(sortedExamples, new Comparator<Instance>() {

			@Override
			public int compare(Instance o1, Instance o2) {
				double dist1 = 0, dist2 = 0;
				for (int i = 0; i < vector.size(); i++) {
					double d1 = o1.vector.get(i) - vector.get(i);
					double d2 = o2.vector.get(i) - vector.get(i);
					dist1 += d1 * d1;
					dist2 += d2 * d2;
				}
				return dist1 > dist2 ? 1 : -1;
			}
		});
		
		int[] classes = new int[dim];
		for (int i = 0; i < Math.min(k, sortedExamples.size()); i++) {
			Instance inst = sortedExamples.get(i);
			classes[inst.value]++;
		}
		int res = 0;
		for (int i = 1; i < dim; i++) {
			if (classes[i] > classes[res])
				res = i;
		}
		return res;
	}
}
