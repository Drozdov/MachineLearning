package ru.ifmo.ctddev.drozdov.ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public abstract class LinearMethod extends AbstractMLMethod {

	public LinearMethod(int possibleValsCount) {
		super(possibleValsCount);
	}
	
	public LinearMethod(File f) throws FileNotFoundException {
		super(0);
		Scanner in = new Scanner(f);
		possibleValsCount = in.nextInt();
		dimension = in.nextInt();
		initTeta();
		for (int i = 0; i < possibleValsCount; i++) {
			for (int j = 0; j < dimension; j++) {
				teta[i][j] = in.nextDouble();
			}
		}
		in.close();
	}
	
	public void printTeta(File f) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(f);
		out.println(possibleValsCount + " " + dimension);
		for (int i = 0; i < possibleValsCount; i++) {
			for (int j = 0; j < dimension; j++) {
				out.print((int)teta[i][j] + " ");
			}
			out.println();
		}
		out.close();
	}

	protected double[][] teta;
	protected double teta0 = 0;
	
	@Override
	public void teach(Recognizable[] examples, byte[] labels) {
		if (examples.length > 0) {
			dimension = examples[0].getSize();
		}
		initTeta();
		if (mode != QUIET) {
			System.out.println("Training...");
		}
		train(examples, labels);
	}

	protected abstract void train(Recognizable[] examples, byte[] labels);
	
	public double getMatching(Recognizable image, int digit) {
		double g = teta0;
		for (int i = 0; i < dimension; i++) {
			g += teta[digit][i] * image.getElement(i);
		}
		return g;
	}

	@Override
	public int getResult(Recognizable image) {
		double max = Integer.MIN_VALUE;
		int argmax = -1;
		double newMax;
		for (int i = 0; i < possibleValsCount; i++) {
			if ((newMax = getMatching(image, i)) > max) {
				max = newMax;
				argmax = i;
			}
		}
		return argmax;
	}
	
	public void release() {
		teta = null;
	}
	
	public void initTeta() {
		initTeta(new double[possibleValsCount][dimension]);
	}
	
	public void initTeta(int imageSize) {
		this.dimension = imageSize;
		initTeta();
	}
	
	public void initTeta(double[][] teta) {
		this.teta = teta;
	}

}
