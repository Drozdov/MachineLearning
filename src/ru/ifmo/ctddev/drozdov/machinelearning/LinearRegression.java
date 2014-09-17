package ru.ifmo.ctddev.drozdov.machinelearning;

import java.util.List;

public class LinearRegression implements LearningAlgorithm {
	
	private double[] c;
	private int dim;
	private boolean free = false;

	public LinearRegression() {
	}

	public LinearRegression(boolean free) {
		this.free = free;
	}
	
	@Override
	public void teach(List<Instance> instances) {
		dim = instances.get(0).vector.size();
		if (free)
			dim++;
		double[][] a = new double[dim][dim];
		double[] b = new double[dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				for (Instance inst : instances) {
					List<Double> vector = inst.vector;
					a[i][j] += get(vector, i) * get(vector, j);
				}
			}
			for (Instance inst : instances) {
				b[i] += get(inst.vector, i) * inst.value;
			}
		}
		a = inverse(a);
		c = gauss(a, b);
	}
	
	private double get(List<Double> list, int i) {
		if (free) {
			return i == 0 ? 1 : list.get(i - 1);
		} else {
			return list.get(i);
		}
	}
	
	public static double[][] inverse(double[][] A) {
		double determinant, invdet;
		double[][] result;
		switch (A.length) {
		case 1:
			return A;
		case 2:
			determinant = A[0][0] * A[1][1] - A[0][1] * A[1][0];
			invdet = 1 / determinant;
			result = new double[2][2];
			result[0][0] = A[1][1] * invdet;
			result[0][1] = -A[1][0] * invdet;
			result[1][0] = -A[0][1] * invdet;
			result[1][1] = A[1][1] * invdet;
			return result;
		case 3:
			determinant = A[0][0] * (A[1][1] * A[2][2] - A[2][1] * A[1][2])
						- A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0])
						+ A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);
			invdet = 1 / determinant;
			result = new double[3][3];
			result[0][0] = (A[1][1] * A[2][2] - A[2][1] * A[1][2]) * invdet;
			result[0][1] = -(A[0][1] * A[2][2] - A[0][2] * A[2][1]) * invdet;
			result[0][2] = (A[0][1] * A[1][2] - A[0][2] * A[1][1]) * invdet;
			result[1][0] = -(A[1][0] * A[2][2] - A[1][2] * A[2][0]) * invdet;
			result[1][1] = (A[0][0] * A[2][2] - A[0][2] * A[2][0]) * invdet;
			result[1][2] = -(A[0][0] * A[1][2] - A[1][0] * A[0][2]) * invdet;
			result[2][0] = (A[1][0] * A[2][1] - A[2][0] * A[1][1]) * invdet;
			result[2][1] = -(A[0][0] * A[2][1] - A[2][0] * A[0][1]) * invdet;
			result[2][2] = (A[0][0] * A[1][1] - A[1][0] * A[0][1]) * invdet;
			return result;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static double[] gauss(double[][] a, double[] b) {
		int n = a.length;
		for (int row = 0; row < n; row++) {
			int best = row;
			for (int i = row + 1; i < n; i++)
				if (Math.abs(a[best][row]) < Math.abs(a[i][row]))
					best = i;
			double[] tt = a[row];
			a[row] = a[best];
			a[best] = tt;
			double t = b[row];
			b[row] = b[best];
			b[best] = t;
			for (int i = row + 1; i < n; i++)
				a[row][i] /= a[row][row];
			b[row] /= a[row][row];
			// a[row][row] = 1;
			for (int i = 0; i < n; i++) {
				double x = a[i][row];
				if (i != row && x != 0) {
					// row + 1 instead of row is an optimization
					for (int j = row + 1; j < n; j++)
						a[i][j] -= a[row][j] * x;
					b[i] -= b[row] * x;
				}
			}
		}
		return b;
	}

	@Override
	public int getResult(List<Double> vector) {
		double res = 0;
		for (int i = 0; i < dim; i++) {
			res += get(vector, i) * c[i];
		}
		return (int)res;
	}
}
