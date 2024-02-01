package tech.octopusdragon.cursordodge.game.classes;

/**
 * Represents a set of X and Y coordinates
 * @author Alex Gill
 *
 */
public class Position {
	
	private double x;	// The X coordinate
	private double y;	// The Y coordinate

	/**
	 * Constructs a set of coordinates
	 */
	public Position() {
		x = Double.NaN;
		y = Double.NaN;
	}

	/**
	 * Constructs a set of coordinates
	 * @param x X coordinate
	 * @param y Y coordinate
	 */
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param x the x coordinate
	 * @throws IllegalArgumentException if the number is negative
	 */
	public void setX(double x) throws IllegalArgumentException {
		if (x < 0.0)
			throw new IllegalArgumentException("x cannot be negative");
		this.x = x;
	}

	/**
	 * @param y the y coordinate
	 * @throws IllegalArgumentException if the number is negative
	 */
	public void setY(double y) throws IllegalArgumentException {
		if (y < 0.0)
			throw new IllegalArgumentException("y cannot be negative");
		this.y = y;
	}
	
	/**
	 * @return whether the position is invalid or uninitialized
	 */
	public boolean isEmpty() {
		return x == Double.NaN || y == Double.NaN; 
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("x: %s\n", x));
		sb.append(String.format("y: %s\n", y));
		return sb.toString();
	}
	
}
