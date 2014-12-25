package ru.ifmo.ctddev.drozdov.ml.bayesnetworks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Factor {
	public final int varMask;
	public final Map<Integer, Double> map;

	public Factor(int mask, Map<Integer, Double> map) {
		this.varMask = mask;
		this.map = map;
	}
	
	public void printDebug() {
		for (Entry<Integer, Double> entry : map.entrySet()) {
			out.println(entry.getKey() + " " + entry.getValue());
		}
	}
	
	public static PrintStream devNull = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});
	
	public static PrintStream out = devNull;//System.err;

	public double get(int vm) {
		return map.get(vm & varMask);
	}

	public Factor marginal(int mask) {
		out.println("Before margin " + varMask + " " + mask);
		printDebug();
		if (!isSubMask(mask))
			throw new IllegalStateException();
		Map<Integer, Double> newMapping = new HashMap<>(
				1 << Integer.bitCount(mask));
		for (Map.Entry<Integer, Double> e : map.entrySet()) {
			int ind = e.getKey() ^ (e.getKey() & mask);
			newMapping.put(ind, e.getValue() + getElement(newMapping, ind));
		}
		Factor factor = new Factor(varMask ^ mask, newMapping);
		out.println("After margin " + varMask);
		factor.printDebug();
		out.println("=========");
		return factor;
	}
	
	public boolean isSubMask(int mask) {
		return (mask | varMask) == varMask;
	}

	private double getElement(Map<Integer, Double> newMapping, int key) {
		//return newMapping.containsKey(key) ? newMapping.get(key) : 0;
		if (newMapping.containsKey(key))
			return newMapping.get(key);
		return 0;
	}

	public Factor compose(Factor f) {
		out.println("Before compose " + varMask + " " + f.varMask);
		printDebug();
		out.println();
		f.printDebug();
		int mask = varMask | f.varMask;
		int common = varMask & f.varMask;
		Map<Integer, Double> newMapping = new HashMap<>(
				1 << Integer.bitCount(mask));
		for (Map.Entry<Integer, Double> em : map.entrySet()) {
			for (Map.Entry<Integer, Double> ef : f.map.entrySet()) {
				if ((ef.getKey() & common) == (em.getKey() & common)) {
					int ind = ef.getKey() | em.getKey();
					newMapping.put(ind, ef.getValue() * em.getValue());
				}
			}
		}
		Factor factor = new Factor(mask, newMapping);
		out.println("After compose " + varMask);
		factor.printDebug();
		out.println("=========");
		return factor;
	}

	public Factor fix(int subMask, int val) {
		subMask &= varMask;
		if (subMask == 0)
			return this;
		
		out.println("Before fixing " + varMask);
		printDebug();
		
		val &= varMask;
		Map<Integer, Double> newMapping = new HashMap<>();
		for (Map.Entry<Integer, Double> e : map.entrySet()) {
			int ind = e.getKey() & subMask;
			if (ind == val)
				newMapping.put(e.getKey() ^ ind, e.getValue());
		}
		
		
		Factor factor = new Factor(varMask ^ subMask, newMapping);
		
		out.println("After fixing " + varMask);
		factor.printDebug();
		out.println("=========");
		
		
		return factor;
	}
}