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
		//BinaryDecisionTree tree = new BinaryDecisionTree();
		RandomForest tree = new RandomForest(11, 100, category.get(0).length);
		tree.teach(category.toArray(new int[category.size()][]),
				complement.toArray(new int[category.size()][]));
		
		
		int rightpos = 0;
		int wrongpos = 0;
		int rightneg = 0;
		int wrongneg = 0;
		
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
					rightpos++;
				else
					wrongpos++;
				break;
			case -1:
				if (res)
					rightneg++;
				else
					wrongneg++;
				break;
			default:
				System.err.println("Invalid label");	
			}
		}
		
		inTrainData.close();
		inTrainLabels.close();
		inTestData.close();
		inTestLabels.close();
		
		System.out.println("Accuracy: " + 100 * (rightpos + wrongneg) / (rightpos + wrongpos + rightneg + wrongneg));
		int prec = 100 * rightpos / (rightpos + rightneg);
		System.out.println("Precision: " + prec);
		int rec = 100 * rightpos / (rightpos + wrongpos);
		System.out.println("Recall: " + rec);
		System.out.println("F1: " + Math.sqrt(prec * rec));

		
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
