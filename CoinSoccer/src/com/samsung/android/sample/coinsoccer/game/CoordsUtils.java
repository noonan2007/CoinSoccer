package com.samsung.android.sample.coinsoccer.game;

public class CoordsUtils {

	public static boolean isPointInside(ICircle circle, float x, float y) {
		return circle.getRadius() <= Math.sqrt(
				Math.pow(circle.getCenterX() - x, 2) +
						Math.pow(circle.getCenterY() - y, 2));
	}

	public static boolean overlaps(ICircle circle, ICircle other) {
		return circle.getRadius() + other.getRadius() >= Math.sqrt(
				Math.pow(circle.getCenterX() - other.getCenterX(), 2) +
						Math.pow(circle.getCenterY() - other.getCenterY(), 2));
	}

	public static boolean overlaps(ICircle circle, RectangularArea rectangle) {
		return circle.getCenterX() >= rectangle.getLeftX() - circle.getRadius() &&
				circle.getCenterX() <= rectangle.getLeftX() + rectangle.getWidth() + circle.getRadius() &&
				circle.getCenterY() >= rectangle.getBottomY() - circle.getRadius() &&
				circle.getCenterY() <= rectangle.getBottomY() + rectangle.getHeight() + circle.getRadius();
	}

	public static boolean contains(RectangularArea container, RectangularArea containment) {
		return contains(container, containment.getLeftX(), containment.getBottomY())
				&& contains(container, containment.getLeftX() + containment.getWidth(), containment.getBottomY() + containment.getHeight());
	}

	public static boolean contains(RectangularArea r, float x, float y) {
		return x >= r.getLeftX() && x <= r.getRightX() && y >= r.getBottomY() && y <= r.getTopY();
	}
}
