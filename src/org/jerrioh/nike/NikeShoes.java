package org.jerrioh.nike;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class NikeShoes {
	
	private enum Direction {
		LEFT, RIGHT, UP, DOWN; 
		
		public Point movedPoint(Point point, int movePixel) {
			if (this == Direction.LEFT) {
				return new Point(point.x - movePixel, point.y);
			} else if (this == Direction.RIGHT) {
				return new Point(point.x + movePixel, point.y);
			} else if (this == Direction.UP) {
				return new Point(point.x, point.y - movePixel);
			} else if (this == Direction.DOWN) {
				return new Point(point.x, point.y + movePixel);
			}
			
			System.out.println("unknown direction");
			return new Point(point.x, point.y);
		}
		
		public Point diagonalMovedPoint(Point point, int movePixel) {
			if (this == Direction.LEFT) {
				Point movedPoint = Direction.LEFT.movedPoint(point, movePixel);
				return Direction.UP.movedPoint(movedPoint, movePixel);
			} else if (this == Direction.RIGHT) {
				Point movedPoint = Direction.RIGHT.movedPoint(point, movePixel);
				return Direction.DOWN.movedPoint(movedPoint, movePixel);
			} else if (this == Direction.UP) {
				Point movedPoint = Direction.UP.movedPoint(point, movePixel);
				return Direction.RIGHT.movedPoint(movedPoint, movePixel);
			} else if (this == Direction.DOWN) {
				Point movedPoint = Direction.DOWN.movedPoint(point, movePixel);
				return Direction.LEFT.movedPoint(movedPoint, movePixel);
			}
			
			System.out.println("unknown direction");
			return new Point(point.x, point.y);
		}
	}
	
	private static final Point LOADING_ICON_POINT = new Point(20, 20);
	private static final int BUTTON_CENTER_X = 1500;

	private Robot robot;
			
	public NikeShoes() throws AWTException {
		robot = new Robot();
	}
	
	private static final Color GRAY = new Color(209, 209, 209);
	private static final Color BLACK = new Color(0, 0, 0);
	private static final Color WHITE = new Color(255, 255, 255);
	private static final Color RED = new Color(255, 0, 0);

	public boolean buy() {
		int x = findStandXPoint();

		boolean readyToBuy = true;
		while (readyToBuy) {
			Rectangle buyButton = findRectangle(x, BLACK, BLACK, WHITE); // find buy button
			if (buyButton == null) {
				if (!isWaiting()) {
					System.out.println("press F5");
					robot.keyPress(KeyEvent.VK_F5); // refresh
				}
				sleep(50);
				continue;
			}
			Rectangle selectBox = findRectangle(x, GRAY, WHITE, WHITE); // find size select box
			if (selectBox == null) {
				System.out.println("selectBox is null");
				sleep(50);
				continue;
			}
			
			if (!isClickable(buyButton)) {
				System.out.println("not yet clickable");
				sleep(50);
				continue;
			}
			
			startBuying(selectBox, buyButton);
			readyToBuy = false;
		}
		
		return true;
	}

	private int findStandXPoint() {
		return BUTTON_CENTER_X;
	}

	private boolean isWaiting() {
		for (int i = 1; i < 10; i++) {
			if (similarColor(RED, getPixelColor(LOADING_ICON_POINT.x - i, LOADING_ICON_POINT.y - i))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x - i, LOADING_ICON_POINT.y))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x - i, LOADING_ICON_POINT.y + i))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x, LOADING_ICON_POINT.y - i))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x, LOADING_ICON_POINT.y + i))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x + i, LOADING_ICON_POINT.y - i))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x + i, LOADING_ICON_POINT.y))
					|| similarColor(RED, getPixelColor(LOADING_ICON_POINT.x + i, LOADING_ICON_POINT.y + i))) {
				return false;
			}
		}
		return true;
	}

	private boolean isClickable(Rectangle rectangle) {
		mouseMove((int) rectangle.getCenterX(), (int) rectangle.getCenterY()); sleep(20);
		Color mouseOnColor = getPixelColor(rectangle.x + 10, rectangle.y + 10);
		
		mouseMove(0, 0); sleep(20);
		Color mouseOffColor = getPixelColor(rectangle.x + 10, rectangle.y + 10);
		
		return !mouseOnColor.equals(mouseOffColor);
	}

	private Rectangle findRectangle(int x, Color lineColor, Color innerColor, Color outerColor) {
		for (int y = 500; y < 1000; y++) {
			if (similarColor(lineColor, getPixelColor(x, y))
					&& similarColor(innerColor, getPixelColor(Direction.DOWN.movedPoint(new Point(x, y), 3)))
					&& similarColor(outerColor, getPixelColor(Direction.UP.movedPoint(new Point(x, y), 3)))) {
				System.out.println("try find rectangle (" + x + ", " + y + ") ...");
				Rectangle buyButton = makeRectangle(x, y, outerColor);
				if (buyButton != null) {
					System.out.println("found!");
					return buyButton;
				}
			}
		}
		return null;
	}

	private Rectangle makeRectangle(int x, int y, Color outerColor) {
		int widthMinLimit = 300;
		int widthMaxLimit = 1000;
		
		int heightMinLimit = 30;
		int heightMaxLimit = 100;

		Point startPoint = moveAndNextPoint(Direction.LEFT, new Point(x, y), outerColor, 0, widthMaxLimit, false);
		
		Point rightTop = moveAndNextPoint(Direction.RIGHT, startPoint, outerColor, widthMinLimit, widthMaxLimit, true);
		Point rightBottom = moveAndNextPoint(Direction.DOWN, rightTop, outerColor, heightMinLimit, heightMaxLimit, true);
		Point leftBottom = moveAndNextPoint(Direction.LEFT, rightBottom, outerColor, widthMinLimit, widthMaxLimit, true);
		Point leftTop = moveAndNextPoint(Direction.UP, leftBottom, outerColor, heightMinLimit, heightMaxLimit, true);
		
		if (rightTop == null || rightBottom == null || leftBottom == null || leftTop == null) {
			return null;
		}
		return new Rectangle(leftTop.x, leftTop.y, rightBottom.x - leftTop.x, rightBottom.y - leftTop.x);
	}

	private Point moveAndNextPoint(Direction direction, Point startPoint, Color outerColor, int minLimit, int maxLimit, boolean roundMargin) {
		if (startPoint == null) {
			return null;
		}
		
		Point movingPoint = startPoint;
		Color startPointColor = getPixelColor(startPoint.x, startPoint.y);
		Color movingPointColor = getPixelColor(movingPoint.x, movingPoint.y);
		
		// keep going
		while (startPointColor.equals(movingPointColor)) {
			movingPoint = direction.movedPoint(movingPoint, 1);
			movingPointColor = getPixelColor(movingPoint.x, movingPoint.y);
			
			// check maxLmit
			int movedPixel = Math.abs(startPoint.x - movingPoint.x) + Math.abs(startPoint.y - movingPoint.y);
			if (movedPixel > maxLimit) {
				return null;
			}
		}
		movingPoint = direction.movedPoint(movingPoint, -1);

		// check minLmit
		int movedPixel = Math.abs(startPoint.x - movingPoint.x) + Math.abs(startPoint.y - movingPoint.y);
		if (movedPixel < minLimit) {
			return null;
		}
		
		// outer color is not match. cannot make rectangle
		// 그라데이션이 있을 수 있으니 3 pixel 이동
		Point pointForGradation = direction.movedPoint(movingPoint, 3);
		Color colorForGradation = getPixelColor(pointForGradation.x, pointForGradation.y);
		if (!similarColor(outerColor, colorForGradation)) {
			return null;	
		}
		
		// roundMargin으로 인한 point이동은 시계방향으로 한다.
		if (roundMargin) {
			movingPoint = direction.movedPoint(movingPoint, -8);
			movingPoint = direction.diagonalMovedPoint(movingPoint, 3);
			movingPointColor = getPixelColor(movingPoint.x, movingPoint.y);
			
			while (!startPointColor.equals(movingPointColor)) {
				movingPoint = direction.movedPoint(movingPoint, 1);
				movingPointColor = getPixelColor(movingPoint.x, movingPoint.y);
			}
		}

		return movingPoint;
	}

	private boolean similarColor(Color color1, Color color2) {
		int red = Math.abs(color1.getRed() - color2.getRed());
		int green = Math.abs(color1.getGreen() - color2.getGreen());
		int blue = Math.abs(color1.getBlue() - color2.getBlue());

		return red + green + blue < 50;
	}

	private void startBuying(Rectangle selectBox, Rectangle button) {
		System.out.println("start buying!");

	}

	private void sleep(int millisec) {
		try {
			Thread.sleep(millisec);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private Color getPixelColor(int x, int y) {
		return robot.getPixelColor(x, y);
	}
	
	private Color getPixelColor(Point point) {
		return robot.getPixelColor(point.x, point.y);
	}

	
	private void mouseMove(int x, int y) {
		robot.mouseMove(x, y);
	}
	
	private void mouseMove(Point point) {
		robot.mouseMove(point.x, point.y);
	}
}
