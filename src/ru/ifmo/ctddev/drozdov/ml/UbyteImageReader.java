package ru.ifmo.ctddev.drozdov.ml;

import java.io.IOException;

public class UbyteImageReader extends UbyteReader {
	private int width, height;
	
	public UbyteImageReader(String fileName) throws IOException {
		super(fileName);
		if (type != SET) {
			throw new IOException("This file is not a set of images.");
		}
		height = in.readInt();
		width = in.readInt();
	}
	
	public GreyImage getImage() throws IOException {
		check();
		byte[] pixels = new byte[height * width];
		for (int i = 0; i < height * width; i++) {
			pixels[i] = in.readByte();
		}
		GreyImage im = new GreyImage(width, pixels);
		return im;
	}
	
	public GreyImage[] getAllImages() throws IOException {
		if (mode == LOUD)
			System.out.println("Reading images from " + fileName + "...");
		GreyImage[] images = new GreyImage[itemsNumber];
		int i = 0;
		while (hasNext()) {
			if (i % 1000 == 0 && mode == LOUD) {
				System.out.println(i + " images read.");
			}
			images[i++] = getImage();
		}
		if (mode == LOUD)
			System.out.println("All images read. Total: " + i);
		return images;
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

}
