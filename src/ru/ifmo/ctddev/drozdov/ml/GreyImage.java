package ru.ifmo.ctddev.drozdov.ml;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

public class GreyImage implements Recognizable {
	byte[] pixels;
	int width;
	int height;
	
	private final static int MAX_PIX = 3 * Byte.MAX_VALUE / 2;

	public GreyImage(byte[][] pixels) {
		height = pixels.length;
		width = pixels[0].length;
		this.pixels = new byte[height * width];
		for (int i = 0; i < height; i++) {
			if (pixels[i].length != width) {
				throw new IllegalArgumentException(
						"The argument pixels should contain rectangular matrix");
			}
			for (int j = 0; j < width; j++) {
				this.pixels[width * i + j] = pixels[i][j];
			}
		}
	}
	
	@Override
	public int getSize() {
		return height * width;
	}

	public GreyImage(int width, byte[] pixels) throws IllegalArgumentException {
		this.width = width;
		this.pixels = pixels;
		if (pixels.length % width != 0)
			throw new IllegalArgumentException(
					"Could not represent pixels as a rectangular "
							+ "matrix which width is equal to " + width);
		this.height = pixels.length / width;
	}
	
	public GreyImage(BufferedImage image) {
		this(getPixelsFromBufImage(image));
	}

	private static byte[][] getPixelsFromBufImage(BufferedImage image) {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
		ColorConvertOp op = new ColorConvertOp(cs, null);  
		image = op.filter(image, null); 
		int width = image.getWidth();
		int height = image.getHeight();
		byte[][] pixels = new byte[height][width];
		Raster raster = image.getRaster();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int[] res = raster.getPixel(j, i, new int[4]);
				pixels[i][j] = (byte) (res[0] + res[1] + res[2] / 3);
			}
		}
		return pixels;
	}

	public int getPixel(int i, int j) {
		return getElement(i * width + j);
	}

	@Deprecated
	public int getPixel(int i) {
		return getElement(i);
	}
	
	@Override
	public int getElement(int i) {
		byte pix = pixels[i];
		return pix >= 0 ? pix : 256 + pix;
	}

	public void printAsPNG(String fileName) throws FileNotFoundException,
			IOException {
		BufferedImage bi = new BufferedImage(width, height, 10);
		DataBuffer db = bi.getRaster().getDataBuffer();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				db.setElem(i * width + j, 255 - getPixel(i, j));
			}
		}
		ImageIO.write(bi, "png", new FileImageOutputStream(new File(fileName)));
	}
	
	public Set<Pixel> getPixels() throws IOException {
		Set<Pixel> set = new HashSet<Pixel>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int b = getPixel(i, j);
				if (b < MAX_PIX) {
					set.add(new Pixel(j, i));
				}
				
			}
		}
		return set;
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
}
