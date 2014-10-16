package ru.ifmo.ctddev.drozdov.ml;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinaryDecisionTree {
	
	public static void main(String[] args) {
		BinaryDecisionTree tree = new BinaryDecisionTree();
		tree.teach(new int[][] {{5, 1, 8}, {8, 0, 3}}, new int[][] {{2, 7, 3}, {1, 9, 11}});
		System.out.println(tree.classify(new int[] {9, 1, 0}));
		System.out.println(tree.classify(new int[] {1, 10, 13}));
	}
	
	private static final double LOG2 = Math.log(2);
	private static final double EPS = 1e-12;
	private static final double MIN_ENTROPY = 0.01;
	private final static int MAX_SIZE = 500;
	protected int[][] category, complement;
	
	private int chosenCount = 1;
	private int categoryCount = 0;
	private int complementCount = 0;
	protected int maxHeight = -1;
	protected BinaryDecisionTree left, right;
	protected int chosen = -1;
	protected boolean decision;
	private int size;
	
	private BinaryDecisionTree(int[][] category, int[][] complement) {
		this.category = category;
		this.complement = complement;
	}
	
	public BinaryDecisionTree() {
	}
	
	public void setMaxHeight(int max) {
		this.maxHeight = max;
	}

	public void teach(int[][] category, int[][] complement) {
		this.category = category;
		this.complement = complement;
		size = category[0].length;
		teach();
	}

	private void free() {
		category = null;
		complement = null;
	}
	
	public List<Integer> signs;

	protected void choose() {
		if (signs == null) {
			signs = new ArrayList<Integer>(size);
			for (int j = 0; j < size; j++) {
				signs.add(j);
			}
		}
		double min = 1;
		chosen = -1;
		int size1 = category.length;
		int size2 = complement.length;
		for (int j : signs) {
			int[] countsCat = new int[MAX_SIZE];
			int[] countsCompl = new int[MAX_SIZE];
			for (int[] cat : category) {
				for (int i = 0; i < cat[j]; i++) {
					if (i == MAX_SIZE)
						break;
					countsCat[i]++;
				}
			}
			for (int[] compl : complement) {
				for (int i = 0; i < compl[j]; i++) {
					if (i == MAX_SIZE)
						break;
					countsCompl[i]++;
				}
			}
			for (int c = 0; c < MAX_SIZE; c++) {
				min = updateChosen(j, c, size1, size2, countsCat[c], countsCompl[c], min);
			}
		}
	}
	
	protected double updateChosen(
			int val, int count, int size1, int size2, int catCount, int complCount, double min) {
		double res;
		if ((res = getH(size1, size2, catCount, complCount)) < min) {
			chosen = val;
			chosenCount = count;
			return res;
		}
		return min;
	}
	
	public boolean useGini = true;

	public double getH(int size1, int size2, int catCount, int complCount) {
		int notCatCount = size1 - catCount;
		int notComplCount = size2 - complCount;
		if (notCatCount + notComplCount == 0)
			return 1;
		double pl = (double) (catCount + complCount) / (size1 + size2);
		double pr = 1 - pl;
		double p1 = (double) catCount / (catCount + complCount);
		double p2 = (double) notCatCount / (notCatCount + notComplCount);
		if (catCount == 0 && complCount == 0)
			return 1024;
		if (notCatCount == 0 && notComplCount == 0)
			return 1024;
		
		if (useGini)
			return pl * gini(p1) + pr * gini(p2);
		return pl * entropy(p1) + pr * entropy(p2);
	}

	private static double entropy(double p) {
		if (Math.abs(p) < EPS || Math.abs(p - 1) < EPS)
			return 0;
		return -(p * Math.log(p) + (1 - p) * Math.log(1 - p)) / LOG2;
	}
	
	private static double gini(double p) {
		return 1 - p * p - (1 - p) * (1 - p);
	}

	public boolean devide() {
		updateDecision();
		choose();
		if (chosen == -1)
			return false;
		List<int[]> categoryL, categoryR, complementL, complementR;
		categoryL = new ArrayList<int[]>();
		categoryR = new ArrayList<int[]>();
		complementL = new ArrayList<int[]>();
		complementR = new ArrayList<int[]>();
		devideByChosen(category, categoryL, categoryR);
		devideByChosen(complement, complementL, complementR);
		left = new BinaryDecisionTree(categoryL.toArray(new int[categoryL.size()][]),
				complementL.toArray(new int[complementL.size()][]));
		left.signs = signs;
		right = new BinaryDecisionTree(categoryR.toArray(new int[categoryR.size()][]),
				complementR.toArray(new int[complementR.size()][]));
		right.signs = signs;
		if (maxHeight > 0) {
			left.setMaxHeight(maxHeight - 1);
			right.setMaxHeight(maxHeight - 1);
		}
		return true;
	}

	public void devideByChosen(int[][] devided,
			List<int[]> left, List<int[]> right) {
		for (int[] element : devided) {
			if (fits(element)) {
				left.add(element);
			} else {
				right.add(element);
			}
		}
	}

	private boolean fits(int[] arr) {
		return arr[chosen] > chosenCount;
	}

	public BinaryDecisionTree getLeft() {
		return left;
	}

	public BinaryDecisionTree getRight() {
		return right;
	}

	private void teach() {
		Queue<BinaryDecisionTree> queue = new LinkedList<BinaryDecisionTree>();
		queue.add(this);
		while (!queue.isEmpty()) {
			BinaryDecisionTree tree = queue.poll();
			if (tree.needsDeviding() && tree.devide()) {
				tree.free();
				queue.add(tree.left);
				queue.add(tree.right);
			} else {
				tree.updateDecision();
			}
		}
	}

	protected boolean needsDeviding() {
		//if (maxHeight == 0)
		//	return false;
		int size1 = category.length;
		int size2 = complement.length;
		//System.err.println(size1 + " " + size2);
		if (size1 == 0 || size2 == 0)
			return false;
		return entropy((double) size1 / (size1 + size2)) > MIN_ENTROPY;
		//return size1 > 0 && size2 > 0;
	}

	private void updateDecision() {
		int size1 = category.length;
		int size2 = complement.length;
		decision = size1 > size2;
	}

	public boolean classify(int[] vector, int answer) {
		if (chosen != -1) {
			if (fits(vector)) {
				return left.classify(vector, answer);
			} else {
				return right.classify(vector, answer);
			}
		}
		return category.length > complement.length;
	}
	
	public boolean classify(int[] vector) {
		return classify(vector, 0);
	}
	
	public void prune(int[][] category, int[][] complement) {
		for (int[] a : category) {
			classify(a, 1);
		}
		for (int[] a : complement) {
			classify(a, -1);
		}
		dfs();
	}
	
	private int dfs() {
		if (chosen != -1) {
			int c = left.dfs() + right.dfs();
			int c2 = decision ? categoryCount : complementCount;
			if (c >= c2) {
				return c;
			} else {
				chosen = -1;
				left = null;
				right = null;
				return c2;
			}
		} else {
			return decision ? categoryCount : complementCount;
		}
	}
	
	public int getTotalSize() {
		return 1 + (left == null ? 0 : left.getTotalSize()) + (right == null ? 0 : right.getTotalSize());
	}

	public void print(PrintStream out) {
		out.println(this.getClass().getSimpleName());
		printingDfs(out);
		out.flush();
	}
	
	protected void printingDfs(PrintStream out) {
		out.println(decision + " " + chosen);
		if (left != null) {
			left.printingDfs(out);
		} else {
			//out.println();
		}
		if (right != null) {
			right.printingDfs(out);
		} else {
			//out.println();
		}
	}

}

