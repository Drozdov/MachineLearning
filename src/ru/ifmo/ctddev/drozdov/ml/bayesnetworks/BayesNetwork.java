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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class BayesNetwork {
	
	public static void main(String[] args) {
		try {
			BayesNetwork bayesNetwork = new BayesNetwork("bayesian_network");
			bayesNetwork.computeAllProbabilities();
			
			double[] vals = bayesNetwork.createEvidences(
					new Evidence(0, true),
					new Evidence(1, true)
					);
			
			for (int i = 0; i < vals.length; i++) {
				System.out.println(vals[i]);
			}
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
			nodes[i] = new Node(i);
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
			node.probabilities = new HashMap<>(1 << node.parents.size());
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
						bitmap += 1 << pid;
					}
					j++;
				}
				double value = Double.parseDouble(after[0]);
				node.probabilities.put(bitmap, value);
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
		Map<Integer, Double> probabilities;
		Factor factor;
		int mask;
		
		public Node(int id) {
			this.mask = 1 << id;
		}
		
		double getProbability(int event) {
			return event > 0 ? getProbability() : 1 - getProbability();
		}
		
		double getProbability() {
			if (probability < 0) {
				Map<Integer, Double> map = new HashMap<>();
				probability = 0;
				
				for (Entry<Integer, Double> entry : probabilities.entrySet()) {
					double p = 1;
					Integer mask = entry.getKey();
					for (int j = 0; j < parents.size(); j++) {
						int pid = parents.get(j);
						p *= nodes[pid].getProbability((mask >> pid) & 1);
					}
					Double p0 = probabilities.get(entry.getKey());
					probability += p * p0;
					
					p = 1;
					
					map.put(mask, p * (1 - p0));
					map.put(mask + this.mask, p * p0);
				}
				
				int mask = this.mask;
				for (Integer i : parents) {
					mask |= 1 << i;
				}
				factor = new Factor(mask, map);
			}
			if (factor == null) { // this is a node with no parents
				Map<Integer, Double> mapping = new HashMap<>();
				mapping.put(0, 1 - probability);
				mapping.put(mask, probability);
				factor = new Factor(mask, mapping);
			}
			return probability;
		}
		
		Factor getFactor() {
			getProbability();
			return factor;
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

	public double[] eliminate(int mask, int val) {
		double[] res = new double[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			if ((nodes[i].mask & mask) == nodes[i].mask) {
				res[i] = (nodes[i].mask & val) == nodes[i].mask ? 1 : 0;
			} else {
				res[i] = eliminate(i, mask, val);
			}
		}
		return res;
	}

	private double eliminate(int v, int mask, int val) {
		List<Factor> factors = new ArrayList<Factor>();
		for (int i = 0; i < nodes.length; i++) {
			Factor f = nodes[i].getFactor();
			f = f.fix(mask, val);
			if (f.varMask == 0)
				continue;
			factors.add(f);
		}
        int fixedVars = mask;
        int freeVariables = nodes.length - Integer.bitCount(mask) - 1;
        for (int i = 0; i < freeVariables; i++) {
            int u = minInflVariable(v, fixedVars, factors);
            fixedVars ^= nodes[u].mask;
            List<Factor> infl = inflFactors(factors, u);
            if (infl.isEmpty())
                continue;
            Factor newFactor = fold(infl).marginal(nodes[u].mask);
            factors.removeAll(infl);
            if (!(newFactor.varMask == 0))
                factors.add(newFactor);
        }
        
        
        Factor res = fold(factors);
        
        //System.err.println(res.varMask);
        
        double p = res.get(nodes[v].mask);
        return p / (p + res.get(0));
	}
	
    private Factor fold(Iterable<Factor> factors) {
        Factor factor = null;
        for (Factor f : factors) {
        	//System.err.println(f.varMask);
        	//f.printDebug();
        	if (factor == null)
        		factor = f;
        	else {
        		//System.err.println("Mult " + factor.varMask + " " + f.varMask);
        		factor = factor.compose(f);
        		//factor.printDebug();
        	}
        }
        return factor;
    }
    
    private List<Factor> inflFactors(Iterable<Factor> factors, int u) {
    	List<Factor> affected = new ArrayList<>();
    	for (Factor f : factors) {
    		if (f.isSubMask(1 << u))
    			affected.add(f);
    	}
        return affected;
    }

    private int countInflFactors(int v, Iterable<Factor> factors) {
        return inflFactors(factors, v).size();
    }

    private int minInflVariable(int v, int mask, Iterable<Factor> factors) {
        int minAffect = Integer.MAX_VALUE;
        int minInd = -1;
        for (int u = 0; u < nodes.length; u++) {
            if (u == v || (mask & nodes[u].mask) != 0)
                continue;
            int affect = countInflFactors(u, factors);
            if (affect < minAffect) {
                minAffect = affect;
                minInd = u;
            }
        }
        return minInd;
    }
    
    public double[] createEvidences(Evidence...evidences) {
    	int mask = 0;
    	int value = 0;
    	for (Evidence evidence : evidences) {
    		mask += 1 << evidence.id;
    		if (evidence.value) {
    			value += 1 << evidence.id;
    		}
    	}
    	return eliminate(mask, value);
    }

}
