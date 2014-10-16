package ru.ifmo.ctddev.drozdov.ml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomForest {
	public static void main(String[] args) throws IOException {
		BufferedReader inTrainData = new BufferedReader(new FileReader("random_forest/arcene_train.data"));
		BufferedReader inTrainLabels = new BufferedReader(new FileReader("random_forest/arcene_train.labels"));
		BufferedReader inTestData = new BufferedReader(new FileReader("random_forest/arcene_valid.data"));
		BufferedReader inTestLabels = new BufferedReader(new FileReader("random_forest/arcene_valid.labels"));
		
		String line;
		List<int[]> category = new ArrayList<>(), complement = new ArrayList<>();
		while ((line = inTrainData.readLine()) != null) {
			String[] strNumbers = line.split("\\s+");
			int[] numbers = new int[strNumbers.length];
			for (int i = 0; i < strNumbers.length; i++) {
				numbers[i] = Integer.parseInt(strNumbers[i]);
			}
			int label = Integer.parseInt(inTrainLabels.readLine());
			switch (label)
			{
			case 1:
				category.add(numbers);
				break;
			case -1:
				complement.add(numbers);
				break;
			default:
				System.err.println("Invalid label");	
			}
		}
		RandomForest tree = new RandomForest(19, 1000, category.get(0).length);
		tree.teach(category.toArray(new int[category.size()][]),
				complement.toArray(new int[category.size()][]));
		
		
		int right = 0;
		int wrong = 0;
		while ((line = inTestData.readLine()) != null) {
			String[] strNumbers = line.split("\\s+");
			int[] numbers = new int[strNumbers.length];
			for (int i = 0; i < strNumbers.length; i++) {
				numbers[i] = Integer.parseInt(strNumbers[i]);
			}
			boolean res = tree.classify(numbers);
			int label = Integer.parseInt(inTestLabels.readLine());
			switch (label)
			{
			case 1:
				if (res)
					right++;
				else
					wrong++;
				break;
			case -1:
				if (res)
					wrong++;
				else
					right++;
				break;
			default:
				System.err.println("Invalid label");	
			}
		}
		
		inTrainData.close();
		inTrainLabels.close();
		inTestData.close();
		inTestLabels.close();
		
		System.out.println(right + " " + wrong);

		
	}
	
	private BinaryDecisionTree[] trees;
	
	public RandomForest(int n, int k, int max) {
		trees = new BinaryDecisionTree[n];
		Random rand = new Random();
		for (int i =  0; i < n; i++) {
			trees[i] = new BinaryDecisionTree();
			List<Integer> list = new ArrayList<>();
			for (int j = 0; j < k; j++) {
				list.add(rand.nextInt(max));
			}
			trees[i].signs = list;
		}
	}
	
	public void teach(int[][] category, int[][] complement) {
		for (BinaryDecisionTree tree : trees) {
			tree.teach(category, complement);
		}
	}
	
	public boolean classify(int[] vector) {
		int yes = 0;
		int no = 0;
		for (BinaryDecisionTree tree : trees) {
			boolean res = tree.classify(vector);
			if (res)
				yes++;
			else
				no++;
		}
		return yes > no;
	}
	
	
}
