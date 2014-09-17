package ru.ifmo.ctddev.drozdov.machinelearning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {
		testLinearRegression();	
	}

	public static void testKnn() throws FileNotFoundException, IOException {
		teachAndTest(new KNN(9, 2), "chips.txt", 110);
	}
	
	public static void testLinearRegression() throws FileNotFoundException, IOException {
		teachAndTest(new LinearRegression(true), "prices.txt", 40);
	}

	private static void teachAndTest(LearningAlgorithm classifier, String file, int testsCout)
			throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		List<Instance> instances = readInstances(in, testsCout);
		classifier.teach(instances);
		
		List<Instance> tests = readInstances(in);
		for (Instance test : tests) {
			int res = classifier.getResult(test.vector);
			System.out.println(res + " " + test.value);
		}
		in.close();
	}

	private static List<Instance> readInstances(BufferedReader in) throws IOException {
		return readInstances(in, Integer.MAX_VALUE);
	}

	private static List<Instance> readInstances(BufferedReader in, int count) throws IOException {
		String line;
		List<Instance> instances = new ArrayList<>();
		int i = 0;
		while ((line = in.readLine()) != null && !line.isEmpty() && i++ < count) {
			String[] vals = line.split(",");
			List<Double> vector = new ArrayList<>();
			vector.add(new Double(vals[0]));
			vector.add(new Double(vals[1]));
			int classId = new Integer(vals[2]);
			instances.add(new Instance(vector, classId));
		}
		return instances;
	}

}
