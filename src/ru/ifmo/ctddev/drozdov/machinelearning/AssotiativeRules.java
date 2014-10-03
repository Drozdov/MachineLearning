package ru.ifmo.ctddev.drozdov.machinelearning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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
	private Map<Integer, String> revIds = new HashMap<>();
	private List<Map<BitSet, List<Long>>> candidates = new ArrayList<>();
	
	public int MIN_SUPPORT = 4;
	
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
	
	int dim;

	public void calculate() {
		for (Entry<String, List<Long>> entry : stat.entrySet()) {
			if (entry.getValue().size() < MIN_SUPPORT)
				break;
			Collections.sort(entry.getValue());
			sortedStat.put(entry.getKey(), entry.getValue());
		}
		int i = 0;
		dim = sortedStat.size();
		List<List<Long>> elements = new ArrayList<>();
		candidates.add(new HashMap<BitSet, List<Long>>());
		for (Entry<String, List<Long>> entry : sortedStat.entrySet()) {
			BitSet elem = new BitSet(dim);
			elem.set(i, true);
			candidates.get(0).put(elem, entry.getValue());
			elements.add(entry.getValue());
			revIds.put(i, entry.getKey());
			ids.put(entry.getKey(), i++);
		}
		i = 1;
		boolean changing = true;
		while (changing) {
			changing = false;
			candidates.add(new HashMap<BitSet, List<Long>>());
			for (Entry<BitSet, List<Long>> entry : candidates.get(i - 1).entrySet()) {
				int start = 0;
				BitSet key = entry.getKey();
				for (int j = dim - 1; j > 0; j--) {
					if (key.get(j))
					{
						start = j;
						break;
					}
				}
				for (int j = start; j < dim; j++) {
					if (!key.get(j)) {
						int k = 0;
						int sum = 0;
						List<Long> list = elements.get(j);
						List<Long> res = new ArrayList<>();
						for (long id : entry.getValue()) {
							while (k < list.size() && list.get(k) < id)
								k++;
							if (k < list.size() && list.get(k) == id) {
								res.add(id);
								sum++;
							}
						}
						if (sum > MIN_SUPPORT) {
							changing = true;
							BitSet element = new BitSet(dim);
							element.or(key);
							element.set(j, true);
							candidates.get(i).put(element, res);
						}
					}
				}
			}
			i++;
		}
		
		Map<BitSet, BitSet> result = new HashMap<>();
		
		for (int j = 1; j < candidates.size(); j++) {
			for (BitSet bs : candidates.get(j).keySet()) {
				assocRules(result, bs, new BitSet(dim));
			}
		}
		
		System.err.println(result.size());
		
		for (Entry<BitSet, BitSet> entry : result.entrySet()) {
			BitSet key = entry.getKey();
			BitSet value = entry.getValue();
			System.out.println("--------");
			System.out.println(key.cardinality());
			for (int j = key.nextSetBit(0); j != -1; j = key.nextSetBit(j + 1)) {
				System.out.println(revIds.get(j));
			}
			System.out.println("-->");
			for (int j = value.nextSetBit(0); j != -1; j = value.nextSetBit(j + 1)) {
				System.out.println(revIds.get(j));
			}
		}
		
	}
	
	private void assocRules(Map<BitSet, BitSet> r,
			BitSet fi, BitSet y) {
		int min = Math.max(0, y.previousSetBit(dim - 1));
		for (int j = fi.nextSetBit(min); j != -1; j = y.nextSetBit(j + 1)) {
				BitSet fi_ = new BitSet(dim);
				fi_.or(fi);
				fi_.set(j, false);
				BitSet y_ = new BitSet(dim);
				y_.or(y);
				y_.set(j, true);
				int s1 = candidates.get(fi.cardinality() - 1).get(fi).size();
				int s2 = candidates.get(fi_.cardinality() - 1).get(fi_).size();
				if (s1 * 6 < s2) {
					r.put(fi_, y_);
					if (fi_.cardinality() > 1) {
						assocRules(r, fi_, y_);
					}
				}
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
		
		public AssotiativeElement(AssotiativeElement element) {
			/*elements = new byte[element.elements.length];
			for (int i = 0; i < element.elements.length; i++) {
				elements[i] = element.elements[i];
			}*/
			elements = Arrays.copyOf(element.elements, element.elements.length);
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
		
		public boolean isEmpty() {
			for (byte b : elements)
				if (b == 0) {
					return false;
				}
			return true;
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
