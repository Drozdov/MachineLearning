package ru.ifmo.ctddev.drozdov.ml;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class UbyteReader {
	protected DataInputStream in;
	protected int type;
	protected int itemsNumber;
	protected int itemsRead = 0;
	protected String fileName;
	
	public final static int LABEL = 0x00000801;
	public final static int SET = 0x00000803;
	
	public UbyteReader(String fileName) throws IOException {
		this.fileName = fileName;
		in = new DataInputStream(new FileInputStream(fileName));
		type = in.readInt();
		itemsNumber = in.readInt();itemsNumber /= 10; // =)
	}
	
	public int getType() {
		return type;
	}
	
	public int getSize() {
		return itemsNumber;
	}
	
	public boolean hasNext() {
		return itemsRead < itemsNumber;
	}
	
	protected void check() throws IOException {
		if (!hasNext())
			throw new IOException("End of file achieved.");
		itemsRead++;
	}
	
	public final static int QUIET = 0;
	public final static int LOUD = 1;
	public static int mode = QUIET;
	
	public static void setMode(int mode) {
		UbyteReader.mode = mode;
	}
}
