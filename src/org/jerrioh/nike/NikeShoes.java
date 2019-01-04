package org.jerrioh.nike;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NikeShoes {
	
	private static final int[][] EIGHT_DIRECTION_MOVE = {
		{-1, -1}, {-1, +0}, {-1, 1},
		{+0, -1},           {+0, +1},
		{+1, -1}, {+1, +0}, {+1, +1}
	};

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
	}

	private static final int SIZE_OPTION_1ST = 3; // ex) 250
	private static final int SIZE_OPTION_2ST = 4; // ex) 255
	private static final int SIZE_OPTION_3RD = 5; // ex) 260
	
	private static final int MOVE_SPEED = 5;

	private static final int RECTANGLE_WIDTH = 100;
	private static final int RECTANGLE_HEIGHT = 30;
	
	private static final Point LOADING_ICON = new Point(20, 20);
	
	private static final Point DETAIL_BUY_BUTTON = new Point(1500, 600);
	private static final int DETAIL_BUY_RANGE = 400;
	
	private static final int DISTANCE_SIZE_TO_BUY = 50;
	private static final int DISTNACE_BETWEEN_SIZE_OPTIONS = 35;
	
	private static final Point CHECKOUT_NEXT_BUTTON = new Point(800, 600);
	private static final int CHECKOUT_NEXT_RANGE = 400;
	
	private static final Point CHECKOUT_AGREE = new Point(265, 1010);
	
	private static final Point CHEKCOUT_PAY_BUTTON = new Point(800, 1050);
	private static final int CHEKCOUT_PAY_BUTTON_RANGE = 250;

	private static final Point INICIS_LOAD_CHECK_COLOR1 = new Point(580, 400);
	private static final Point INICIS_LOAD_CHECK_COLOR2 = new Point(580, 500);
	private static final Point INICIS_LOAD_CHECK_COLOR3 = new Point(580, 600);
	
	private static final Point INICIS_ALL_AGREE = new Point(1070, 350);
	private static final Point INICIS_SAMSUNG_CARD = new Point(1000, 540);
	private static final Point INICIS_NEXT = new Point(1250, 840);
	private static final Point INICIS_SAMSUNG_APPCARD = new Point(860, 550);

	private Robot robot;
			
	public NikeShoes() throws AWTException {
		robot = new Robot();
	}
	
	private static final Color BLACK = new Color(0, 0, 0);
	private static final Color BRIGHT_BLACK = new Color(17, 17, 17);
	private static final Color WHITE = new Color(255, 255, 255);
	private static final Color RED = new Color(255, 0, 0);
	private static final Color BLUE = new Color(51, 153, 255);

	//https://www.nike.com/kr/ko_kr/t/boys-toddler/fw/young-athletes/BV0853-001/ifwv51/the-10-nike-force-1-td?c=snkrs
	public boolean buy() {
		
		String openTime = "11:34:00.750";
		while (true) {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss.SSS", Locale.ENGLISH);
			String time = dateFormat.format(date);
			System.out.println(time);
			
			if (Long.parseLong(time.replaceAll(":", "").replaceAll("\\.", "")) >= Long.parseLong(openTime.replaceAll(":", "").replaceAll("\\.", ""))) {
				break;
			}
		}
		
		boolean readyToBuy = true;
		while (readyToBuy) {
			Rectangle buyButton = findButton(DETAIL_BUY_BUTTON.x, DETAIL_BUY_BUTTON.y, DETAIL_BUY_RANGE, MOVE_SPEED); // find buy button
			if (buyButton == null) {
				if (!isWaiting()) {
					System.out.println("press F5");
					keyInput(KeyEvent.VK_F5); // refresh
				}
				sleep(50);
				continue;
			}
			System.out.println("found buy button: " + buyButton.x + ", " + buyButton.y);

			if (!isClickable(buyButton)) {
				System.out.println("not yet clickable");
				sleep(50);
				continue;
			}
			
			Rectangle selectBox = new Rectangle(buyButton.x, buyButton.y - DISTANCE_SIZE_TO_BUY, RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
			
			step1(selectBox, buyButton);
//			step2();
			readyToBuy = false;
		}
		
		return true;
	}

	private boolean isWaiting() {
		for (int i = 1; i < 10; i++) {
			for (int[] move : EIGHT_DIRECTION_MOVE) {
				int x = LOADING_ICON.x + move[0] * i;
				int y = LOADING_ICON.y + move[1] * i;
				Color pixelColor = getPixelColor(x, y);
				if (similarColor(RED, pixelColor)) {
					System.out.println("NOT waiting");
					return false;
				}
			}
		}
		System.out.println("waiting");
		return true;
	}
	
	private boolean isClickable(Rectangle rectangle) {
		mouseMove(rectangle);
		sleep(20);
		Color mouseOnColor = getPixelColor(rectangle.x + 10, rectangle.y + 10);
		
		mouseMove(0, 0);
		sleep(100);
		Color mouseOffColor = getPixelColor(rectangle.x + 10, rectangle.y + 10);
		
		return !mouseOnColor.equals(mouseOffColor);
	}

	private Rectangle findButton(int x, int startY, int searchRange, int moveSpeed) {
		for (int y = startY; y <= startY + searchRange; y += moveSpeed) {
			Point point = new Point(x, y);
			if (similarColor(BLACK, getPixelColor(point))
					&& similarColor(BLACK, getPixelColor(Direction.DOWN.movedPoint(point, moveSpeed)))
					&& similarColor(WHITE, getPixelColor(Direction.UP.movedPoint(point, moveSpeed)))) {
				return new Rectangle(x, y, RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
			}
		}
		return null;
	}

	private boolean similarColor(Color color1, Color color2) {
		int red = Math.abs(color1.getRed() - color2.getRed());
		int green = Math.abs(color1.getGreen() - color2.getGreen());
		int blue = Math.abs(color1.getBlue() - color2.getBlue());

		return red + green + blue < 30;
	}

	private void step1(Rectangle selectBox, Rectangle buyButton) {
		// size 선택
		System.out.print("find size blue point");
		int tryCount = 0;
		Point sizeBluePoint = null;
		while (sizeBluePoint == null && tryCount < 10) {
			System.out.print(".");
			mouseMove(selectBox);
			mouseClick();
			sleep(200);
			
			sizeBluePoint = sizeBluePoint(selectBox);
			tryCount++;
		}
		System.out.println();
		
		if (sizeBluePoint == null) {
			System.out.println("sizeBluePoint is null");
			return;
		}
		
		int x = sizeBluePoint.x;
		int y = sizeBluePoint.y + DISTNACE_BETWEEN_SIZE_OPTIONS * SIZE_OPTION_1ST;
		if (!similarColor(WHITE, getPixelColor(x, y))) {
			System.out.println("1st option is not available");
			y = sizeBluePoint.y + DISTNACE_BETWEEN_SIZE_OPTIONS * SIZE_OPTION_2ST;
			if (!similarColor(WHITE, getPixelColor(x, y))) {
				System.out.println("2st option is not available");
				y = sizeBluePoint.y + DISTNACE_BETWEEN_SIZE_OPTIONS * SIZE_OPTION_3RD;
				if (!similarColor(WHITE, getPixelColor(x, y))) {
					System.out.println("3rd option is not available");
					return;
				}
			}
		}
		mouseMove(x, y);
		mouseClick();
		sleep(200);
		
		mouseMove(buyButton);
		mouseClick();

		// next 버튼		
		System.out.print("find next button");
		Rectangle nextStepButton = null;
		while (nextStepButton == null) {
			sleep(200);
			System.out.print(".");
			nextStepButton = findButton(CHECKOUT_NEXT_BUTTON.x, CHECKOUT_NEXT_BUTTON.y, CHECKOUT_NEXT_RANGE, MOVE_SPEED); // next step buy button
		}
		System.out.println();
		
		mouseMove(nextStepButton);
		mouseClick();
		
		// payment 버튼
		System.out.print("find payment button");
		Rectangle paymentButton = null;
		while (paymentButton == null) {
			sleep(200);
			System.out.print(".");
			paymentButton = findButton(CHEKCOUT_PAY_BUTTON.x, CHEKCOUT_PAY_BUTTON.y, CHEKCOUT_PAY_BUTTON_RANGE, MOVE_SPEED); // payment button
		}
		System.out.println();
		
		mouseMove(CHECKOUT_AGREE);
		mouseClick();
		
		mouseMove(paymentButton);
		mouseClick();
	}

	private void step2() {
		System.out.print("inicis loading");
		boolean kgInicisLoaded = false;
		while (!kgInicisLoaded) {
			sleep(200);
			System.out.print(".");

			if (similarColor(BRIGHT_BLACK, getPixelColor(INICIS_LOAD_CHECK_COLOR1))
					&& similarColor(BRIGHT_BLACK, getPixelColor(INICIS_LOAD_CHECK_COLOR2))
					&& similarColor(BRIGHT_BLACK, getPixelColor(INICIS_LOAD_CHECK_COLOR3))) {
				kgInicisLoaded = true;
			}
		}
		System.out.println();
		
		mouseMove(INICIS_ALL_AGREE);
		mouseClick();
		sleep(100);
		
		mouseMove(INICIS_SAMSUNG_CARD);
		mouseClick();
		sleep(100);
		
		mouseMove(INICIS_NEXT);
		mouseClick();
		sleep(100);
		
		mouseMove(INICIS_SAMSUNG_APPCARD);
		for (int i = 0; i < 10; i++) {
			sleep(200);
			mouseClick();
		}
	}

	private Point sizeBluePoint(Rectangle selectBox) {
		Point bluePoint = null;
		
		int x = selectBox.x;
		for (int y = selectBox.y; y < selectBox.y + 200; y += MOVE_SPEED) {
			Point point = new Point(x, y);
			if (similarColor(BLUE, getPixelColor(point))) {
				bluePoint = point;		
				break;
			}
		}
		return bluePoint;
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
	
	private void mouseMove(Rectangle rectangle) {
		robot.mouseMove((int) rectangle.getCenterX(), (int) rectangle.getCenterY());
	}

	private void keyInput(int keyEvent) {
		robot.keyPress(keyEvent);
		robot.keyRelease(keyEvent);
	}
	
	private void mouseClick() {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		sleep(50);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		sleep(10);
	}
}
