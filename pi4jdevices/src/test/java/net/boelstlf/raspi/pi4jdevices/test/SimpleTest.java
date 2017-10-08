/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.test;

import java.util.List;

import net.boelstlf.raspi.pi4jdevices.i2c.LEDMatrix8x8;
import net.boelstlf.raspi.pi4jdevices.onewire.DS1820;

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
		System.out.println("");
		if (args[0].equals("/?") || args[0].equals("-h")) {
			SimpleTest.help();
		} else if (args[0].equalsIgnoreCase("LEDMatrix")) {
			SimpleTest.testLEDMatrix();
		} else if (args[0].equalsIgnoreCase("DS1820")) {
			SimpleTest.testDS1820();
		}
	}

	private static void testDS1820() {
		System.out.println("Test run for 'DS1820' one wire sensor.");
		DS1820 ds = new DS1820();
		// for testing only deviceDir = "X:/";
		List<String> slaves = ds.getConnectedSlaves();
		if(slaves.size()>0)
		{
			ds.getTempValue(slaves.get(0));
		}
	}

	/**
	 * Print out help message
	 */
	private static void help() {
		System.out.println("usage 'SimpleTest <device name> <options>'");
		System.out.println("");
		System.out.println("List of available device tests ([default address] <device name> <option>)");
		System.out.println(" I2C");
		System.out.println("\t[0x70] LEDMatrix");
		System.out.println("\t[0x3C] SSD1306 <string to display>");
		System.out.println(" One Wire");
		System.out.println("\tDS1820");
	}

	/**
	 * @param args
	 */
	private static void testLEDMatrix() {
		System.out.println("Test run for '8x8 LED Matrix' one wire sensor.");
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
