package ru.ifmo.ctddev.drozdov.ml;

import java.io.IOException;

public class UbyteLabelReader extends UbyteReader {

	public UbyteLabelReader(String fileName) throws IOException {
		super(fileName);
		if (type != LABEL) {
			throw new IOException("This file does not contain labels.");
		}
	}
	
	public byte getLabel() throws IOException {
		check();
		return in.readByte();
	}
	
	public byte[] getAllLabels() throws IOException {
		if (mode == LOUD)
			System.out.println("Reading labels from " + fileName);
		byte[] labels = new byte[itemsNumber];
		int i = 0;
		while (hasNext()) {
			labels[i++] = getLabel();
		}
		if (mode == LOUD)
			System.out.println("All labels read. Total: " + i);
		return labels;
	}

}
