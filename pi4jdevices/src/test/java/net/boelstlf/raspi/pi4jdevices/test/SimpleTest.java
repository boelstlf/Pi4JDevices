/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.test;

import net.boelstlf.raspi.pi4jdevices.LEDMatrix8x8;

/**
 * @author boelstlf
 *
 */
public class SimpleTest {
	
	public static String version = "0.1";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0 || args.length > 2) {
			SimpleTest.help();
			System.exit(0);
		}

		System.out.println("Starting Simple Test v" + version + "...");
		if (args[0].equals("/?") || args[0].equals("-h")) {
			SimpleTest.help();
		} else if (args[0].equalsIgnoreCase("LEDMatrix")) {
			SimpleTest.testLEDMatrix();
		}

	}

	/**
	 * Print out help message
	 */
	public static void help() {
		System.out.println("usage 'SimpleTest <device name> <options>'");
		System.out.println("");
		System.out.println("List of available device tests ([default address] <device name> <option>)");
		System.out.println(" I2C");
		System.out.println("\t[0x70] LEDMatrix");
		System.out.println("\t[0x3C] SSD1306 <string to display>");
	}

	/**
	 * @param args
	 */
	public static void testLEDMatrix() {
		LEDMatrix8x8 matrix = new LEDMatrix8x8(0x70);

		for (int x = 0; x < 8; x++)
			for (int y = 0; y < 8; y++) {
				matrix.setPixel(x, y, true);
				matrix.writeDisplay();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		matrix.clear();
	}
}
