/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.test;

import java.util.List;

import net.boelstlf.raspi.pi4jdevices.i2c.LEDMatrix8x8;
import net.boelstlf.raspi.pi4jdevices.onewire.DS1820;
import net.boelstlf.raspi.pi4jdevices.uart.RFID;
import net.boelstlf.raspi.pi4jdevices.i2c.ADS1x15;
import net.boelstlf.raspi.pi4jdevices.i2c.VCNL4000;
import net.boelstlf.raspi.pi4jdevices.i2c.ADXL345;
import net.boelstlf.raspi.pi4jdevices.i2c.ADXL345.ThreeAxis;
import net.boelstlf.raspi.pi4jdevices.i2c.LCDDisplay;
import net.boelstlf.raspi.pi4jdevices.i2c.MPU6050;
import net.boelstlf.raspi.pi4jdevices.i2c.MPU6050.ThreeAxisAndGyro;
import net.boelstlf.raspi.pi4jdevices.gpio.LED;
import net.boelstlf.raspi.pi4jdevices.gpio.Switch;
import net.boelstlf.raspi.pi4jdevices.i2c.MPL115A2;
import net.boelstlf.raspi.pi4jdevices.i2c.MPL115A2.TP;
import net.boelstlf.raspi.pi4jdevices.i2c.SSD1306_I2C_Display;

/**
 * @author boelstlf
 *
 */
public class SimpleTest {

	public static String version = "0.2";

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
		} else if (args[0].equalsIgnoreCase("MPU6050")) {
			SimpleTest.testMPU6050(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("ADXL345")) {
			SimpleTest.testADXL345(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("LCD")) {
			SimpleTest.testLCD(args[1], Integer.parseInt(args[2]));
		} else if (args[0].equalsIgnoreCase("CalcLCD")) {
			SimpleTest.testCalcLCD(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else if (args[0].equalsIgnoreCase("ADS1015")) {
			SimpleTest.testADS1015(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else if (args[0].equalsIgnoreCase("VCNL4000")) {
			SimpleTest.testVCNL4000(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("RFID")) {
			SimpleTest.testRFID(Integer.parseInt(args[1]));
		} else if (args[0].equalsIgnoreCase("Switch")) {
			SimpleTest.testSwitch(Integer.parseInt(args[1]));
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
		System.out.println("\t[0x3C] SSD1306 <number of 'Hello'>");
		System.out.println("\t[0x3C] LCD <message> <line number 1|2>");
		System.out.println("\t[0x3C] CalcLCD <target prime number> <ms pause btw display prime number>");
		System.out.println("\t[0x60] MPL115A2 <number measures>");
		System.out.println("\t[0x68] MPU6050 <number measures>");
		System.out.println("\t[0x60] ADXL345 <number measures>");
		System.out.println("\t[0x48] ADS1015 <number measures>");
		System.out.println("\t[0x13] VCNL4000 <number measures>");
		System.out.println(" One Wire");
		System.out.println("\tDS1820");
		System.out.println(" GPIO");
		System.out.println("\tLED <pin>");
		System.out.println("\tSwitch <wait seconds>");
		System.out.println(" UART");
		System.out.println("\tRFID <wait seconds>");
	}

	/**
	 * Test on listing on GPIO input.
	 */
	private static void testSwitch(int wait) {
		Switch s = new Switch();
		s.init();
		
		try {
			Thread.sleep(wait * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test on RFID sensor.
	 */
	private static void testRFID(int wait) {
		System.out.println("Test on RFID sensor.");

		RFID rfid = new RFID();
		rfid.start();
		
		try {
			Thread.sleep(wait * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Simple test to get values from the ADC converter.
	 * 
	 * @param count
	 *            number of readings
	 */
	private static void testADS1015(int count, int channel) {
		System.out.println("Test ADS1015 ADC converter");
		// create device instance for the ads1015
		ADS1x15 ads1015 = new ADS1x15(0x48);

		for (int i = 0; i < count; i++) {

			double result = ads1015.readADCSingleEnded(channel, 4096, 250);
			System.out.println("volt: " + result / 1000);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Test run on 2x16 character LCD display.
	 * 
	 * @param msg
	 *            Message to display
	 * @param line
	 *            line 1 or 2
	 */
	private static void testLCD(String msg, int line) {
		System.out.println("Test run on 2x16 character LCD display.");
		LCDDisplay lcd = new LCDDisplay(39);
		lcd.lcd_display_string(msg, line);
	}

	/**
	 * Simple test to get values from the proximity sensor.
	 * 
	 * @param count
	 *            number of readings
	 */
	private static void testVCNL4000(int count) {
		System.out.println("Test run on VCNL4000 proximity sensor");
		VCNL4000 vcnl = new VCNL4000(0x13);

		for (int i = 0; i < count; i++) {
			System.out.println("proximity: " + vcnl.read_proximity());
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Determine prime numbers until target number.
	 * 
	 * @param target
	 *            target number
	 * @param sleep
	 *            ms pause until showing next prime number
	 */
	private static void testCalcLCD(int target, int sleep) {
		System.out.println("Determine prime numbers until target number.");
		LCDDisplay lcd = new LCDDisplay(39);
		lcd.primzahlen(target, sleep);
	}

	/**
	 * Test run on 3-axis acceleration sensor 'ADXL345'.
	 * 
	 * @param count
	 *            number of readings
	 */
	private static void testADXL345(int count) {
		System.out.println("Test run on 3-axis acceleration sensor 'ADXL345'.");
		ADXL345 adxl = new ADXL345(0x53);
		SSD1306_I2C_Display display = new SSD1306_I2C_Display(0x3c);
		display.clearImage();

		for (int i = 0; i < count; i++) {
			ThreeAxis accl = adxl.readAccl(0.0043f);
			System.out.println("accel: " + adxl.readAccl(0.0043f));
			display.setString(2, "x_accl: '" + accl.x + "'");
			display.setString(3, "y_accl: '" + accl.y + "'");
			display.setString(4, "z_accl: '" + accl.z + "'");
			display.displayImage();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param count
	 *            number of readings
	 */
	private static void testMPU6050(int count) {
		System.out.println("Test run on 3-axis-gyro sensor 'MPU-6050'");
		MPU6050 test = new MPU6050(0x68);
		SSD1306_I2C_Display display = new SSD1306_I2C_Display(0x3c);
		LEDMatrix8x8 matrix = new LEDMatrix8x8(0x70);
		ThreeAxisAndGyro accl;

		int x = 4;
		int y = 4;
		double rotXOffset;
		double rotYOffset;
		double rotX;
		double rotY;

		display.clearImage();
		matrix.setPixel(x, y, true);
		matrix.writeDisplay();

		accl = test.readAccl();
		rotXOffset = accl.getXRotation();
		rotYOffset = accl.getYRotation();

		for (int i = 0; i < count; i++) {
			accl = test.readAccl();
			System.out.println("" + accl);
			rotX = accl.getXRotation() - rotXOffset;
			rotY = accl.getYRotation() - rotYOffset;

			display.clearImage();
			display.setString(1, 1, "x_rot: '" + rotX + "'");
			display.setString(2, 1, "y_rot: '" + rotY + "'");

			if (rotX < -10 && x >= 1) {
				matrix.setPixel(x, y, false);
				x += 1;
				matrix.setPixel(x, y, true);
				display.setString(3, 1, "+ X: '" + x + "'");
			}
			if (rotX > 10 && x <= 6) {
				matrix.setPixel(x, y, false);
				x -= 1;
				matrix.setPixel(x, y, true);
				display.setString(3, 1, "- X: '" + x + "'");
			}
			if (rotY < -10 && y >= 1) {
				matrix.setPixel(x, y, false);
				y -= 1;
				matrix.setPixel(x, y, true);
				display.setString(4, 1, "- Y: '" + y + "'");
			}
			if (rotY > 10 && y <= 6) {
				matrix.setPixel(x, y, false);
				y += 1;
				matrix.setPixel(x, y, true);
				display.setString(4, 1, "+ Y: '" + y + "'");
			}
			matrix.writeDisplay();

			display.displayImage();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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
		SSD1306_I2C_Display display = new SSD1306_I2C_Display(0x3c);
		display.clearImage();

		for (int i = 0; i < count; i++) {
			TP tp = test.readTP();
			System.out.println("values: " + tp);
			display.setString(2, "temp: '" + tp.Temperature + "' C");
			display.setString(3, "press: '" + tp.Pressure + "' bar");
			display.displayImage();
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
