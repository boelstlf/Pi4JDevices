/**
 * Based on Adafruit's python code for HT16KT33 I2C controller
 * Refer to https://github.com/adafruit/micropython-adafruit-ht16k33.git
 */
package net.boelstlf.raspi.pi4jdevices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * @author boelstlf
 * @created 2017/10/08
 *
 */
public class LEDMatrix8x8 {
	private I2CBus bus;
	private I2CDevice device;
	// 8x16-bits RAM of the used HT16K33 i2c controller chip,
	// even we have an 8x8 led matrix only
	private int[] buffer = { 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000 };

	/**
	 * Clear entire display.
	 */
	public void clear() {
		for (int x = 0; x < 8; x++)
			for (int y = 0; y < 8; y++)
				setPixel(x, y, false);
	}

	/**
	 * Set a pixel as on/off (true / false).
	 * 
	 * @param x
	 * @param y
	 * @param on
	 *            true=on, false=off
	 */
	public void setPixel(int x, int y, boolean on) {
		if (x <= 8 && y <= 8) {
			x += 7; // ATTN: This might be a bug? On the color matrix, this
					// causes x=0 to draw on the last line instead of the first.
			x %= 8;
			if (on)
				this.setBufferRow(y, buffer[y] | 1 << x);
			else
				this.setBufferRow(y, buffer[y] & ~(1 << x));
		}
	}

	/**
	 * Clear a pixel, i.e. turn off.
	 * 
	 * @param x
	 * @param y
	 */
	public void clearPixel(int x, int y) {
		setPixel(x, y, false);
	}

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public LEDMatrix8x8() {
	}

	/**
	 * sudo i2cdetect -y 1 shows 0x70 = 112
	 * 
	 * @param hwadd
	 */
	public LEDMatrix8x8(int hwadd) {
		try {
			// get the I2C Bus 1 as we run Rev 2 board
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus ok! " + I2CBus.BUS_1);

			// get the device ADS1015 on 0x48
			device = bus.getDevice(hwadd);
			System.out.println("Connected to device ok!");

			// init
			device.write(HT16K33_REGISTER_SYSTEM_SETUP | 0x01, (byte) 0x00);
			this.setBrightness(15);
			this.clear();
			this.writeDisplay();
			
		} catch (UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} // rev 2 board, so it is bus 1

	}
	
	public void begin()
	{
        //Initialize driver with LEDs enabled and all turned off."""
        // Turn on the oscillator.
        //device.write(HT16K33_SYSTEM_SETUP | HT16K33_OSCILLATOR, []);
        
        // Turn display on with no blinking.
        //this.setBlink(HT16K33_BLINKRATE_OFF);
        
        // Set display to full brightness.
        this.setBrightness(15);

	}
	
	/**
	 * Set the brightness level from 0..15.
	 * 
	 * @param brightness
	 */
	public void setBrightness(int brightness) {
		if (brightness > 15)
			brightness = 15;
		try {
			device.write(HT16K33_REGISTER_DIMMING | brightness, (byte) 0x00);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Update the buffer memory.
	 */
	public void writeDisplay() {
		int counter = 0;
		byte[] buf = new byte[16];
		for (int value : buffer) {
			byte[] dummy = convertIntToByteArray(value);
			buf[counter] = dummy[0];
			buf[counter + 1] = dummy[1];
			counter += 2;
		}
		try {
			device.write(0x00, buf, 0, 16);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param val
	 * @return
	 */
	private static byte[] convertIntToByteArray(int val) {
		byte[] buffer = new byte[2];

		buffer[1] = (byte) (val >>> 8);
		buffer[0] = (byte) val;

		return buffer;
	}

	/**
	 * Updates a single 16-bit entry in the 8*16-bit buffer
	 */
	private void setBufferRow(int row, int value) {
		if (row <= 8) {
			buffer[row] = value;
		}
	}

	/**
	 * @return
	 */
	public int[] getBuffer() {
		return buffer;
	}

	// Registers
	public static int HT16K33_REGISTER_DISPLAY_SETUP = 0x80;
	public static int HT16K33_REGISTER_SYSTEM_SETUP = 0x20;
	public static int HT16K33_REGISTER_DIMMING = 0xE0;

	// Blink rate
	public static int HT16K33_BLINKRATE_OFF = 0x00;
	public static int HT16K33_BLINKRATE_2HZ = 0x01;
	public static int HT16K33_BLINKRATE_1HZ = 0x02;
	public static int HT16K33_BLINKRATE_HALFHZ = 0x03;
}
