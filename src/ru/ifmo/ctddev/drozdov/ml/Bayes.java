package ru.ifmo.ctddev.drozdov.ml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class Bayes {
	public static void main(String[] args) throws IOException {
		File root = new File("bayes/Bayes/pu1");
		Map<Integer, Integer> legit = new HashMap<>(), spam = new HashMap<>(), all = new HashMap<>();
		File[] listFiles = root.listFiles();
		File[] train = Arrays.copyOf(listFiles, 8);
		File[] test = Arrays.copyOfRange(listFiles, 8, 10);
		for (File directory : train)
		{
			for (File f : directory.listFiles()) {
				boolean isSpam = f.getName().contains("spmsg");
				Map<Integer, Integer> map = isSpam ? spam : legit;
				Scanner in = new Scanner(f);
				while (!in.hasNextInt())
					in.next();
				while (in.hasNextInt()) {
					int key = in.nextInt();
					updateMap(map, key);
					updateMap(all, key);
				}
				in.close();
			}
		}
		Map<Integer, Double> weights = new HashMap<>();
		for (Entry<Integer, Integer> e : all.entrySet()) {
			int key = e.getKey();
			double v1 = spam.containsKey(key) ? spam.get(key) : 0;
			double val = e.getValue();
			weights.put(key, 1. * (v1 + 1) / (val + 2));
		}
		
		int rightpos = 0;
		int wrongpos = 0;
		int rightneg = 0;
		int wrongneg = 0;
		
		for (File directory : test)
		{
			for (File f : directory.listFiles()) {
				boolean isSpam = f.getName().contains("spmsg");
				Scanner in = new Scanner(f);
				while (!in.hasNextInt())
					in.next();
				double sum = 0;
				while (in.hasNextInt()) {
					int key = in.nextInt();
					if (!weights.containsKey(key))
						continue;
					double weight = weights.get(key);
					sum += Math.log((1 - weight) / weight); 
				}
				boolean res = sum > 0;
				if (res && !isSpam) {
					rightpos++;
				} else if (res && isSpam) {
					rightneg++;
				} else if (!res && !isSpam) {
					wrongpos++;
				} else {
					wrongneg++;
				}
				in.close();
			}
		}
		System.out.println("Accuracy: " + 100 * (rightpos + wrongneg) / (rightpos + wrongpos + rightneg + wrongneg));
		int prec = 100 * rightpos / (rightpos + rightneg);
		System.out.println("Precision: " + prec);
		int rec = 100 * rightpos / (rightpos + wrongpos);
		System.out.println("Recall: " + rec);
		System.out.println("F1: " + Math.sqrt(prec * rec));

		
		
	}

	private static void updateMap(Map<Integer, Integer> map, int key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1);
		} else {
			map.put(key, 1);
		}
	}
}
