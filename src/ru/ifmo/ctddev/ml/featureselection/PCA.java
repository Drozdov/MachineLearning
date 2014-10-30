package ru.ifmo.ctddev.ml.featureselection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ArrayUtilities;

import testers.SampleXYDataset2;
import testers.ScatterPlotDemo1;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class PCA {
	public static void main(String[] args) throws IOException {
		PCA pca = new PCA();
		List<double[]> values = new ArrayList<>();
		BufferedReader in = new BufferedReader(new FileReader("pca/newBasis1"));
		while (in.ready()) {
			String line = in.readLine();
			if (line == null || line.isEmpty())
				continue;// just in case
			String vals[] = line.split("\\s+");
			double[] arr = new double[vals.length];
			int i = 0;
			for (String val : vals) {
				arr[i++] = Double.parseDouble(val);
			}
			values.add(arr);
		}
		in.close();
		pca.compute(values.toArray(new double[values.size()][]));
	}
	
	public void compute(double[][] vals) {
		int size = vals[0].length;
		int length = vals.length;
		
		double[][] valsT = new double[size][length];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < vals.length; j++) {
				valsT[i][j] = vals[j][i];
			}
		}
		
		double medians[] = new double[size];
		for (int i = 0; i < size; i++) {
			double m = 0;
			for (double v : valsT[i]) {
				m += v;
			}
			medians[i] = m / size;
			for (int j = 0; j < vals.length; j++) {
				valsT[i][j] -= medians[i];
			}
		}
		
		Matrix X = new Matrix(valsT);
		SingularValueDecomposition svd = X.svd();
		double[] singularValues = svd.getSingularValues();
		for (double d : singularValues) {
			System.out.println(d);
			
		}
		
		double[] ds0 = svd.getU().getArray()[0];
		double[] ds1 = svd.getU().getArray()[0];
		
		Double[] d0 = new Double[ds0.length];
		Double[] d1 = new Double[ds1.length];
		
		for (int i = 0; i < ds0.length; i++) {
			d0[i] = ds0[i];
			d1[i] = ds1[i];
		}
		
        ScatterPlotDemo1 demo = new ScatterPlotDemo1("Scatter Plot Demo 1", new SampleXYDataset2(1, d0, d1));
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
		
		
		/*double[][] matrix = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				matrix[i][j] = matrix[j][i] = covariation(valsT[i], medians[i], valsT[j], medians[j]);
			}
		}
		Matrix cov = new Matrix(matrix);*/
		
	}
	
	private double covariation(double[] vec1, double med1, double[] vec2, double med2) {
		double res = 0;
		for (int i = 0; i < vec1.length; i++) {
			res += (vec1[i] - med1) * (vec2[i] - med2);
		}
		return res / vec1.length;
	}
}
