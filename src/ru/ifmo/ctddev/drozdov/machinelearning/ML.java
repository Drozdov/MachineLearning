package ru.ifmo.ctddev.drozdov.machinelearning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ML {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("random_forest/arcene_train.data"));
		String line;
		while ((line = in.readLine()) != null) {
			System.out.println(line.split("\\s").length);
		}
		in.close();
		
		testLogisticRegression();	
		//testKnn();
		//testLinearRegression();
	}

	public static void testKnn() throws IOException {
		teachAndTest(new KNN(9, 2), "chips.txt", 80);
	}
	
	public static void testLinearRegression() throws IOException {
		teachAndTest(new LinearRegression(true), "prices.txt", 35);
	}
	
	public static void testLogisticRegression() throws IOException {
		teachAndTest(new LogisticRegression(5, true), "chips.txt", 80);
	}

	private static void teachAndTest(LearningAlgorithm classifier, String file, int testsCout)
			throws IOException {
		Collection<Instance> instances0 = 
				readInstances(file, new HashSet<Instance>());
		List<Instance> instances = new ArrayList<>(instances0);
		classifier.teach(instances.subList(0, testsCout));
		List<Instance> tests = instances.subList(testsCout, instances.size());
		test(classifier, tests);
	}
	
	public static void test(LearningAlgorithm classifier, List<Instance> tests) {
		if (classifier instanceof LinearRegression) {
			double diff = 0;
			for (Instance test : tests) {
				int res = classifier.getResult(test.vector);
				System.out.println(res + " " + test.value);
				long d = res - test.value;
				diff += d * d;
			}
			System.out.println(Math.sqrt(diff));
		} else {
			int rightpos = 0;
			int wrongpos = 0;
			int rightneg = 0;
			
			for (Instance test : tests) {
				int res = classifier.getResult(test.vector);
				if (test.value == 0) {
					if (res == 0)
						rightneg++;
					else
						;
				} else {
					if (res == 0)
						wrongpos++;
					else
						rightpos++;	
				}
			}
			System.out.println("Accuracy: " + 100 * (rightpos + rightneg) / tests.size());
			int prec = 100 * rightpos / (rightpos + rightneg);
			System.out.println("Precision: " + prec);
			int rec = 100 * rightpos / (rightpos + wrongpos);
			System.out.println("Recall: " + rec);
			System.out.println("F1: " + Math.sqrt(prec * rec));
		}
	}

	private static Collection<Instance> readInstances(String file, Collection<Instance> instances) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		while ((line = in.readLine()) != null && !line.isEmpty()) {
			String[] vals = line.split(",");
			List<Double> vector = new ArrayList<>();
			Double double1 = new Double(vals[0]);
			vector.add(double1);
			Double double2 = new Double(vals[1]);
			vector.add(double2);
			vector.add(double1 * double1);
			vector.add(double2 * double2);
			vector.add(double1 * double2);
			int classId = new Integer(vals[2]);
			instances.add(new Instance(vector, classId));
		}
		in.close();
		return instances;
	}

}
