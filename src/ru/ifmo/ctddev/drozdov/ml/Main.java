package ru.ifmo.ctddev.drozdov.ml;

import java.io.IOException;

import ru.ifmo.ctddev.drozdov.ml.perceptron.Perceptron;
//import ru.ifmo.ctddev.drozdov.ml.perceptron.Perceptron;

public class Main {
	public static void main(String[] args) throws IOException {
		AbstractMLMethod.setMode(AbstractMLMethod.LOUD);
		AbstractMLMethod[] methods = {
				new Perceptron(10)
		};
		for (AbstractMLMethod p : methods) {
			p.teach("train-images.idx3-ubyte", "train-labels.idx1-ubyte");
			double result = p.test("t10k-images.idx3-ubyte",
					"t10k-labels.idx1-ubyte");
			System.out.printf(
					"\n-------------------\nResult: %.2f%% successfull"
							+ "\n%.2f%% unseccessful\non " + p.getMethodInfo(),
					100 * result, 100 * (1 - result));
			System.out.println();
		}
	}
}
