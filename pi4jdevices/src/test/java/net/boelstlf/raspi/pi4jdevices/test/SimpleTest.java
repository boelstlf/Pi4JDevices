/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.test;

import java.io.IOException;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import net.boelstlf.raspi.pi4jdevices.i2c.LEDMatrix8x8;
import net.boelstlf.raspi.pi4jdevices.onewire.DS1820;
import net.boelstlf.raspi.pi4jdevices.gpio.LED;
import net.boelstlf.raspi.pi4jdevices.i2c.MPL115A2;
import net.boelstlf.raspi.pi4jdevices.i2c.SSD1306_I2C_Display;

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

		if (args.length == 0 || args.length > 3) {
			SimpleTest.help();
			System.exit(0);
		}

		System.out.println("Starting Simple Test v" + version + "...");
		System.out.println("");
		if (args[0].equals("/?") || args[0].equals("-h")) {
			SimpleTest.help();
		} else if (args[0].equalsIgnoreCase("LEDMatrix")) {
			if (args.length == 1)
				SimpleTest.testLEDMatrix();
			if (args.length == 3)
				SimpleTest.testLEDMatrix(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else if (args[0].equalsIgnoreCase("DS1820")) {
			SimpleTest.testDS1820();
		} else if (args[0].equalsIgnoreCase("SSD1306")) {
			SimpleTest.testSSD1306_I2C(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("MPL115A2")) {
			SimpleTest.testMPL115A2(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("LED")) {
			SimpleTest.testLED(Integer.parseInt(args[1]));
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
		System.out.println("\t[0x70] LEDMatrix <posX> <posY>");
		System.out.println("\t[0x3C] SSD1306 <number of Hello>");
		System.out.println("\t[0x60] MPL115A2 <number measures>");
		System.out.println(" One Wire");
		System.out.println("\tDS1820");
		System.out.println(" GPIO");
		System.out.println("\tLED <pin>");
	}
	/**
	 * @param parseInt
	 */
	private static void testLED(int pin) {
		new LED(pin);

	}
	
	/**
	 * @param count
	 *            number of readings
	 */
	private static void testMPL115A2(int count) {
		System.out.println("Test run for 'MPL115A2' sensor");

		MPL115A2 test = new MPL115A2(0x60);

		for (int i = 0; i < count; i++) {
			System.out.println("values: " + test.readTP());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void testSSD1306_I2C(int count) {
		System.out.println("Test run for '128x64 OLED Display' based on SSD1306 controller.");
		SSD1306_I2C_Display display;
		try {
			display = new SSD1306_I2C_Display(0x3c);
			display.clearImage();

			display.setString(1, 1, "EQ: Cleaner");
			display.setString(2, 1, "Temp: 21.5C");
			display.setString(3, 1, "Lot: L_173900001");
			display.setString(4, 1, "Unit: E_1739000001");
			display.displayImage();

			try {
				Thread.sleep(2000);
				for (int i = 0; i < count; i++) {
					int row = i % 5 + 1;
					System.out.println("values: " + i + "; row =" + row);
					// display.clearImage();
					display.setString(row, "Hello" + i);
					Thread.sleep(250);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException | UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void testDS1820() {
		System.out.println("Test run for 'DS1820' one wire sensor.");
		DS1820 ds = new DS1820();
		// for testing only deviceDir = "X:/";
		List<String> slaves = ds.getConnectedSlaves();
		if (slaves.size() > 0) {
			ds.getTempValue(slaves.get(0));
		}
	}

	/**
	 * @param args
	 */
	private static void testLEDMatrix() {
		System.out.println("Test run for '8x8 LED Matrix'.");
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

	/**
	 * @param args
	 */
	private static void testLEDMatrix(int x, int y) {
		System.out.println("Test run for '8x8 LED Matrix'; set one pixel.");
		LEDMatrix8x8 matrix = new LEDMatrix8x8(0x70);

		matrix.setPixel(x, y, true);
		matrix.writeDisplay();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		matrix.clear();
	}
}
