package org.jerrioh;

import java.awt.AWTException;

import org.jerrioh.nike.NikeShoes;

public class Main {
	public static void main(String[] ar) throws AWTException {
		boolean success = new NikeShoes().buy();
		System.out.println("result=" + success);
	}
}
