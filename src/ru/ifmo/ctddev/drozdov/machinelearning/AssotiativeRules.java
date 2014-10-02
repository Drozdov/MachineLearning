package ru.ifmo.ctddev.drozdov.machinelearning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class AssotiativeRules {
	
	public static void main(String[] args) throws IOException {
		AssotiativeRules ar = new AssotiativeRules();
		BufferedReader in = new BufferedReader(new FileReader("supermarket.arff"));
		String line;
		while (!in.readLine().equals("@data"));
		while ((line = in.readLine()) != null) {
			String[] vals = line.split(",");
			String product = vals[0];
			Long basket = Long.parseLong(vals[3]);
			ar.add(product, basket);
		}
		ar.calculate();
		in.close();
	}
	
	private Map<String, List<Long>> stat = new HashMap<>();
	private Map<String, List<Long>> sortedStat = new TreeMap<>(new ValueComparator());
	private Map<String, Integer> ids = new HashMap<>();
	private List<Map<AssotiativeElement, List<Long>>> candidates = new ArrayList<>();
	
	public int MIN_SUPPORT = 5;
	
	private class ValueComparator implements Comparator<String> {

	    public int compare(String a, String b) {
	        int res = stat.get(b).size() - stat.get(a).size();
	        if (res == 0)
	        	return a.compareTo(b);
	        return res;
	    }
	}
	
	public void add(String name, Long basket) {
		if (stat.containsKey(name)) {
			stat.get(name).add(basket);
		} else {
			List<Long> list = new ArrayList<>();
			list.add(basket);
			stat.put(name, list);
		}
	}

	public void calculate() {
		for (Entry<String, List<Long>> entry : stat.entrySet()) {
			if (entry.getValue().size() < MIN_SUPPORT)
				break;
			Collections.sort(entry.getValue());
			sortedStat.put(entry.getKey(), entry.getValue());
		}
		int i = 0;
		int dim = sortedStat.size();
		candidates.add(new HashMap<AssotiativeElement, List<Long>>());
		for (Entry<String, List<Long>> entry : sortedStat.entrySet()) {
			AssotiativeElement elem = new AssotiativeElement(dim);
			candidates.get(0).put(elem, entry.getValue());
			ids.put(entry.getKey(), i++);
		}
		i = 1;
		boolean changing = true;
		while (changing) {
			changing = false;
			candidates.add(new HashMap<AssotiativeElement, List<Long>>());
			for (Entry<AssotiativeElement, List<Long>> entry : candidates.get(i - 1).entrySet()) {
				for (int j = 0; j < dim; j++) {
					if (!entry.getKey().get(j)) {
						int k = 0;
						int sum = 0;
						AssotiativeElement ae = new AssotiativeElement(dim, i);
						List<Long> list = candidates.get(0).get(ae);
						for (long id : entry.getValue()) {
							if (k < list.size())
								break;
							while (list.get(k) < id)
								k++;
							if (list.get(k) == id)
								sum++;
						}
						if (sum > MIN_SUPPORT) {
							
						}
					}
				}
			}
			i++;
		}
	}
	
	class AssotiativeElement {
		private byte[] elements;
		private int size = 8;
		
		public AssotiativeElement(int dim) {
			elements = new byte[dim / size + (dim % size == 0 ? 0 : 1)];
		}
		
		public AssotiativeElement(int dim, int i) {
			this(dim);
			set(i);
		}
		
		public boolean get(int i) {
			return (elements[i / size] & 1 << (i % size)) > 0;
		}
		
		public void set(int i) {
			elements[i / size] |= 1 << (i % size);
		}
		
		@Override
		public int hashCode() {
			int hash = 0;
			for (int i = 0; i < elements.length; i++) {
				hash += elements[i] * (256 << i);
			}
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			if (! (o instanceof AssotiativeElement))
				return false;
			AssotiativeElement e = (AssotiativeElement)o;
			return Arrays.equals(e.elements, elements);
		}
		
	}

	class FPTree {
		int dim;

		private class FPNode {
			FPNode parent, next;
			FPNode[] children;
			int fid;
			int count = 0;

			public FPNode(int id) {
				this.fid = id;
				children = new FPNode[dim - fid - 1];
			}
			
			public FPNode get(int id) {
				int i = dim - id - 1;
				if (children[i] == null) {
					children[i] = FPTree.this.get(id);
					children[i].parent = this;
				}
				return children[i];
			}
		}
		
		FPNode root;
		FPNode[] first, last;
		public FPTree(int dim) {
			this.dim = dim;
			root = new FPNode(0);
			first = new FPNode[dim];
			last = new FPNode[dim];
		}
		
		public FPNode get(int i) {
			FPNode node = new FPNode(i);
			if (first[i] == null) {
				first[i] = node;
				last[i] = node;
			} else {
				last[i].next = node;
				last[i] = node;
			}
			return node;
		}
		
		public FPNode getFree(int id) {
			for (FPNode node = first[id]; node != null; node = node.next) {
				if (node.parent == null)
					return node;
			}
			return get(id);
		}
		
		public void add(int[] features) {
			FPNode current = get(features[0]);
			current.count++;
			for (int i = 1; i < features.length; i++) {
				current = current.get(features[i]);
				current.count++;
			}
		}
		
		public int get(int[] features) {
			int res = 0;
			for (FPNode node = first[features[0]]; node != null; node = node.next) {
				FPNode current = node;
				for (int i = 1; i < features.length; i++) {
					current = current.get(features[i]);
				}
				res += current.count;
			}
			return res;
		}
	}
}
