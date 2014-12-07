package ru.ifmo.ctddev.drozdov.ml.bayesnetworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BayesNetwork {
	
	public static void main(String[] args) {
		try {
			BayesNetwork bayesNetwork = new BayesNetwork("bayesian_network");
			bayesNetwork.computeAllProbabilities();
			bayesNetwork.printToFile("out.dot");
		} catch (IOException | BayesNetworkException e) {
			e.printStackTrace();
		}
	}
	
	private int curLine = 1;
	private String[] names;
	public BayesNetwork(String fileName) throws IOException, BayesNetworkException {
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line = in.readLine();
		names = line.split("\\s");
		curLine++;
		nodes = new Node[names.length];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node();
		}
		
		while (readNextLine(in)) {
			int id = Integer.parseInt(before[0]);
			double p = Double.parseDouble(after[0]);
			nodes[id].probability = p;
		}
		
		while (readNextLine(in))
		{
			int id = Integer.parseInt(before[0]);
			Node node = nodes[id];
			node.children = new ArrayList<>(after.length);
			for (String c : after) {
				int cid = Integer.parseInt(c);
				node.children.add(cid);
				nodes[cid].parents.add(id);
			}
			Collections.sort(node.parents);
			node.probabilities = new double[1 << node.parents.size()];
			while (readNextLine(in)) {
				Arrays.sort(before, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						Integer i1 = Integer.parseInt(o1.startsWith("¬") ? o1.substring(1) : o1); 
						Integer i2 = Integer.parseInt(o2.startsWith("¬") ? o2.substring(1) : o2);
						return i1.compareTo(i2);
					}
				});
				if (before.length != node.parents.size()) {
					throw new BayesNetworkException("Incorrect number of parent nodes on line " + curLine);
				}
				int j = 0;
				int bitmap = 0;
				for (String arg : before) {
					if (!arg.startsWith("¬"))
					{
						int pid = Integer.parseInt(arg);
						if (pid != node.parents.get(j))
							throw new BayesNetworkException("Invalid parent index, line " + curLine);
						bitmap += 1 << j;
					}
					j++;
				}
				double value = Double.parseDouble(after[0]);
				node.probabilities[bitmap] = value;
			}
			if (before == null || after == null)
				break;
		}
		
		
		in.close();
	}
	
	private String[] before, after;
	private boolean readNextLine(BufferedReader in) throws IOException {
		String line = in.readLine();
		curLine++;
		if (line == null || !line.contains(":"))
			return false;
		String[] beforeAndAfter = line.split(":");
		before = beforeAndAfter[0].trim().split("\\s");
		after = beforeAndAfter.length > 1
				&& beforeAndAfter[1].trim().length() > 0 ? beforeAndAfter[1]
				.trim().split("\\s") : new String[0];
		return true;
	}
	
	private Node[] nodes;
	private class Node {
		double probability = -1;
		List<Integer> children = new ArrayList<>(), parents = new ArrayList<>();
		double[] probabilities;
		
		double getProbability(int event) {
			return event > 0 ? getProbability() : 1 - getProbability();
		}
		
		double getProbability() {
			if (probability < 0) {
				probability = 0;
				for (int i = 0; i < probabilities.length; i++) {
					double p = 1;
					for (int j = 0; j < parents.size(); j++) {
						Node parent = nodes[parents.get(j)];
						p *= parent.getProbability((i >> j) & 1);
					}
					probability += p * probabilities[i];
				}
			}
			return probability;
		}
	}
	
	public void computeAllProbabilities() {
		for (Node node : nodes) {
			node.getProbability();
		}
	}
	
	public double getProbability(int id) {
		return nodes[id].getProbability();
	}
	
	public void printToFile(String fileName) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new File(fileName));
		out.println("digraph Network {");
		for (int id = 0; id < nodes.length; id++) {
			out.printf(Locale.US, "A%d [label=\"%s [%.4f]\"];", id, names[id], nodes[id].probability);
			out.println();
			for (Integer ch : nodes[id].children) {
				out.printf("A%d -> A%d;", id, ch);
				out.println();
			}
		}
		out.println("}");
		out.close();
	}
}
