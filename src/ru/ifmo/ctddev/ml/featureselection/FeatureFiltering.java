package ru.ifmo.ctddev.ml.featureselection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.ctddev.drozdov.machinelearning.Instance;
import ru.ifmo.ctddev.drozdov.machinelearning.LearningAlgorithm;
import ru.ifmo.ctddev.drozdov.machinelearning.LogisticRegression;
import ru.ifmo.ctddev.drozdov.machinelearning.ML;

public class FeatureFiltering {
	public static void main(String[] args) throws IOException {
		BufferedReader inTrainData = new BufferedReader(new FileReader("random_forest/arcene_train.data"));
		BufferedReader inTrainLabels = new BufferedReader(new FileReader("random_forest/arcene_train.labels"));
		BufferedReader inTestData = new BufferedReader(new FileReader("random_forest/arcene_valid.data"));
		BufferedReader inTestLabels = new BufferedReader(new FileReader("random_forest/arcene_valid.labels"));
		
		String line;
		List<Instance> instances = new ArrayList<>();
		while ((line = inTrainData.readLine()) != null) {
			String[] strNumbers = line.split("\\s+");
			List<Double> numbers = new ArrayList<>();
			for (int i = 0; i < strNumbers.length; i++) {
				numbers.add(Double.parseDouble(strNumbers[i]));
			}
			int label = Integer.parseInt(inTrainLabels.readLine());
			instances.add(new Instance(numbers, label));
		}
		
		List<Instance> tests = new ArrayList<>();
		while ((line = inTestData.readLine()) != null) {
			String[] strNumbers = line.split("\\s+");
			List<Double> numbers = new ArrayList<>();
			for (int i = 0; i < strNumbers.length; i++) {
				numbers.add(Double.parseDouble(strNumbers[i]));
			}
			int label = Integer.parseInt(inTestLabels.readLine());
			tests.add(new Instance(numbers, label));
		}
		
		LearningAlgorithm algo = new LogisticRegression(10000, false);
		algo.teach(instances);
		ML.test(algo, tests);
		System.exit(0);
		
		FeatureFiltering featureFiltering = new FeatureFiltering();
		int[] mask = featureFiltering.getMask(instances);
		List<Instance> filtered = featureFiltering.filterByMask(instances, mask);		
		List<Instance> filteredTests = featureFiltering.filterByMask(tests, mask);		
		
		algo = new LogisticRegression(filtered.size(), true);
		algo.teach(filtered);
		ML.test(algo, filteredTests);
		
		inTrainData.close();
		inTrainLabels.close();
		inTestData.close();
		inTestLabels.close();	
	}
	
	public int[] getMask(List<Instance> instances) {
		int size = instances.size();
		int averageLabel = 0;
		for (Instance inst : instances) {
			averageLabel += inst.value;
		}
		averageLabel /= size;
		int length = instances.get(0).vector.size();
		double[] ranks = new double[length];
		
		for (int i = 0; i < length; i++) {
			double aver = 0;
			for (Instance inst : instances) {
				aver += inst.vector.get(i);
			}
			aver /= size;
			double rank = 0;
			for (Instance inst : instances) {
				rank += (inst.vector.get(i) - aver) * (inst.value - averageLabel);
			}
			ranks[i] = Math.abs(rank);
		}
		
		double[] ranksSorted = Arrays.copyOf(ranks, ranks.length);
		Arrays.sort(ranksSorted);
		
		int threshold = 1000;
		int[] indices = new int[threshold];
		int j = 0;
		double d = ranksSorted[threshold - 1];
		for (int i = 0; i < length; i++) {
			if (ranks[i] <= d) {
				indices[j++] = i;
			}
		}
		return indices;
		
	}
	
	public List<Instance> filterByMask(List<Instance> instances, int[] mask) {
		List<Instance> result = new ArrayList<>(instances.size());
		for (Instance inst : instances) {
			List<Double> vec = new ArrayList<>(mask.length);
			for (int i : mask) {
				vec.add(inst.vector.get(i));
			}
			result.add(new Instance(vec, inst.value));
		}
		return result;
	}
	

}
