package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.Arrays;

import javax.lang.model.element.Element;

public class AssotiativeRules {
	
	public static void main(String[] args) {
		new AssotiativeRules().do1();
	}
	
	public void do1() {
		AssotiativeElement elem = new AssotiativeElement(13);
		elem.set(2);
		elem.set(5);
		elem.set(7);
		elem.set(12);
		for (int i = 0; i < 13; i++) {
			System.out.println(i + " " + elem.get(i));
		}
	}
	
	class AssotiativeElement {
		private byte[] elements;
		private int size = 8;
		
		public AssotiativeElement(int dim) {
			elements = new byte[dim / size + (dim % size == 0 ? 0 : 1)];
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
