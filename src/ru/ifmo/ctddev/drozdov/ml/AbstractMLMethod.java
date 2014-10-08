package ru.ifmo.ctddev.drozdov.ml;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractMLMethod {
	protected int dimension;
	protected int possibleValsCount;
	
	public AbstractMLMethod(int possibleValsCount) {
		this.possibleValsCount = possibleValsCount;
	}

	public abstract void teach(Recognizable[] examples, byte[] labels);

	public abstract int getResult(Recognizable value);
	
	public abstract String getMethodInfo();

	public void teach(String imagesFile, String labelsFile) throws IOException {
		if (mode != QUIET)
			System.out.println("Reading training files...");
		UbyteImageReader imagesReader = new UbyteImageReader(imagesFile);
		UbyteLabelReader labelsReader = new UbyteLabelReader(labelsFile);
		Recognizable[] images = imagesReader.getAllImages();
		byte[] labels = labelsReader.getAllLabels();
		dimension = imagesReader.width() * imagesReader.height();
		teach(images, labels);
	}

	public final static int QUIET = 0;
	public final static int NORMAL = 1;
	public final static int LOUD = 2;
	public final static int DEBUG = 3;
	
	public static int mode = NORMAL;

	public static void setMode(int mode) {
		AbstractMLMethod.mode = mode;
		if (mode >= LOUD)
			UbyteReader.setMode(UbyteReader.LOUD);
	}

	public int getSize(Recognizable[] images, byte[] labels) {
		int size = images.length;
		if (labels.length != size) {
			throw new IllegalArgumentException(
					"Files with images and labels contain different number of items.");
		}
		return size;
	}

	public double test(String imagesFile, String labelsFile) throws IOException {
		if (mode != QUIET)
			System.out.println("Reading testing files...");
		UbyteImageReader imagesReader = new UbyteImageReader(imagesFile);
		UbyteLabelReader labelsReader = new UbyteLabelReader(labelsFile);
		Recognizable[] testImages = imagesReader.getAllImages();
		byte[] testLabels = labelsReader.getAllLabels();
		return test(testImages, testLabels);

	}

	public double test(Recognizable[] testImages, byte[] testLabels) {
		int size = getSize(testImages, testLabels);
		if (mode != QUIET)
			System.out.println("Testing...");
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (getResult(testImages[i]) == testLabels[i])
				count++;
		}
		return (double) count / size;
	}

	public void teach(Map<Recognizable, Byte> map) {
		int length = map.size();
		Recognizable[] rec = new Recognizable[length];
		byte[] lab = new byte[length];
		int i = 0;
		for (Entry<Recognizable, Byte> entry : map.entrySet()) {
			rec[i] = entry.getKey();
			lab[i++] = entry.getValue();
		}
		teach(rec, lab);
		
	}

}
