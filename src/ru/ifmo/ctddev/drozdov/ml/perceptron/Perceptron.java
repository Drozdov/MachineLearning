package ru.ifmo.ctddev.drozdov.ml.perceptron;

import ru.ifmo.ctddev.drozdov.ml.LinearMethod;
import ru.ifmo.ctddev.drozdov.ml.Recognizable;

public class Perceptron extends LinearMethod {

	protected int iterationsNumber = 10;

	public Perceptron(int possibleValsCount) {
		super(possibleValsCount);
	}

	public Perceptron(int possibleValsCount, int iterationsNumber) {
		this(possibleValsCount);
		this.iterationsNumber = iterationsNumber;
	}

	@Override
	protected void train(Recognizable[] images, byte[] labels) {
		int trainingSize = getSize(images, labels);
		for (int j = 0; j < iterationsNumber; j++) {
			if (mode >= LOUD)
				System.out.println("Current training iteration: " + j + " of "
						+ iterationsNumber);
			for (int val = 0; val < possibleValsCount; val++) {
				if (mode == DEBUG)
					System.out.println("Current didit: " + val);
				for (int i = 0; i < trainingSize; i++) {
					train(images[i], labels[i], val);
				}
			}
		}
	}

	private void train(Recognizable image, byte label, int val) {
		double g = getMatching(image, val);
		int y = val == label ? 1 : -1;
		if (g * y <= 0) {
			for (int i = 0; i < dimension; i++) {
				teta[val][i] += y * image.getElement(i);
			}
		}
	}

	public void setIterationsNumber(int iterationsNumber) {
		this.iterationsNumber = iterationsNumber;
	}

	public int getIterationsNumber() {
		return iterationsNumber;
	}

	@Override
	public String getMethodInfo() {
		return "perceptron trained on " + iterationsNumber + " iterations.";
	}

}
