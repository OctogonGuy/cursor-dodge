package tech.octopusdragon.cursordodge.game.classes;

/**
 * Represents a point at which a circle can collide. This is always on an edge
 * of the scene. Contains which edge it is as well as its position
 * @author Alex Gill
 *
 */
public class CollisionPoint {
	
	private Edge edge;		// Edge of the scene where the point is located
	private Position pos;	// The X and Y coordinates

	/**
	 * Constructs a collision point
	 * @param edge Edge of the scene where the point is located
	 * @param pos The X and Y coordinates
	 */
	public CollisionPoint(Edge edge, Position pos) {
		this.edge = edge;
		this.pos = pos;
	}

	/**
	 * Constructs a collision point
	 * @param edge Edge of the scene where the point is located
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	public CollisionPoint(Edge edge, double x, double y) {
		this.edge = edge;
		pos = new Position(x, y);
	}

	/**
	 * @return Edge of the scene where the point is located
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * @return X-coordinate
	 */
	public double getX() {
		return pos.getX();
	}

	/**
	 * @return Y-coordinate
	 */
	public double getY() {
		return pos.getY();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Edge: %s\n", edge));
		sb.append(pos);
		return sb.toString();
	}
	
}
