package ru.ifmo.ctddev.drozdov.ml;

import java.util.Collection;

public class Pixel {
	protected int x, y;

	public final static int COEFF = 3;

	public Pixel(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int distance(Pixel p) {
		return (x - p.x) * (x - p.x) + COEFF * COEFF * (y - p.y) * (y - p.y);
	}

	@Override
	public boolean equals(Object p) {
		if (!(p instanceof Pixel))
			return false;
		return x == ((Pixel) p).x && y == ((Pixel) p).y;
	}

	public double distanceFrom(final Pixel p) {
		return distance(p);
	}

	public Pixel centroidOf(final Collection<Pixel> points) {
		int x, y;
		x = y = 0;
		for (Pixel p : points) {
			x += p.x;
			y += p.y;
		}
		x /= points.size();
		y /= points.size();
		return new Pixel(x, y);
	}

}

