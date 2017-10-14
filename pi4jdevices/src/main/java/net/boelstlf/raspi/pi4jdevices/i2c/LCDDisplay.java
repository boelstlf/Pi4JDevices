/**
 * Based on Adafruit's python code.
 * Refer to https://github.com/adafruit/
 */
package net.boelstlf.raspi.pi4jdevices.i2c;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * @author boelstlf
 *
 */
public class LCDDisplay {
	private I2CBus bus;
	private I2CDevice device;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public LCDDisplay() {

	}

	/**
	 * sudo i2cdetect -y 1 shows 0x27 = 39
	 * 
	 * @param hwadd
	 */
	public LCDDisplay(int hwadd) {
		try {
			// get the I2C Bus 1 as we run Rev 2 board
			try {
				bus = I2CFactory.getInstance(I2CBus.BUS_1);
			} catch (UnsupportedBusNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connected to bus ok! " + I2CBus.BUS_1);

			// get the device LCD Display on 0x27
			device = bus.getDevice(hwadd);
			System.out.println("Connected to device ok!");

			device.write(LCD_FUNCTIONSET, (byte) LCD_FUNCTIONSET);

			lcd_write(0x03);
			lcd_write(0x03);
			lcd_write(0x03);
			lcd_write(0x02);

			lcd_write(LCD_FUNCTIONSET | LCD_2LINE | LCD_5x8DOTS | LCD_4BITMODE);
			lcd_write(LCD_DISPLAYCONTROL | LCD_DISPLAYON);
			lcd_write(LCD_CLEARDISPLAY);
			lcd_write(LCD_ENTRYMODESET | LCD_ENTRYLEFT);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param target
	 */
	public void primzahlen(int target, int sleep) {
		this.lcd_display_string("suchen bis: " + target, 1);
		boolean[] primzahl = new boolean[target+1];
		// init all to TRUE
		for (int i = 0; i < target; i++) {
			primzahl[i] = true;
		}
		// set to false all multiplications
		for (int i = 2; i <= target / 2; i++) {
			System.out.println("check on '"+ i+ "'");
			// if still a TRUE
			if(primzahl[i])
			{
				System.out.println("is primenumber");
				// set all multis to FALSE
				int t = 2*i;
				while (t <= target) {
					System.out.println("set '" + t + "' to false");
					primzahl[t] = false;
					t += i;
				}
			}
		}
		// print out all true starting from 2
		for (int i = 2; i < target; i++) {
			if (primzahl[i]) {
				this.lcd_display_string("gefunden: " + i, 2);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void lcd_strobe(int data) throws IOException, InterruptedException {
		byte b = (byte) (data | En | LCD_BACKLIGHT);
		device.write(b);
		Thread.sleep(5);

		b = (byte) ((data & ~En) | LCD_BACKLIGHT);
		device.write(b);
		Thread.sleep(1);
	}

	private void lcd_write_four_bits(int data) throws IOException, InterruptedException {
		byte b = (byte) (data | LCD_BACKLIGHT);
		device.write(b);
		lcd_strobe(data);
	}

	private void lcd_write(int cmd) {
		lcd_write(cmd, (byte) 0);
	}

	private void lcd_write(int cmd, byte mode) {
		try {
			lcd_write_four_bits(mode | (cmd & 0xF0));
			lcd_write_four_bits(mode | ((cmd << 4) & 0xF0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Turn ON/OFF the backlight.
	 * 
	 * @param state
	 */
	public void lcd_backlight(String state) {
		try {
			if (state.equalsIgnoreCase("ON"))
				device.write((byte) LCD_BACKLIGHT);
			else if (state.equalsIgnoreCase("OFF"))
				device.write((byte) LCD_NOBACKLIGHT);
			else
				System.out.println("Unknown State!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the given text message into the defined line.
	 * 
	 * @param message
	 * @param line
	 */
	public void lcd_display_string(String message, int line) {
		if (line == 1)
			lcd_write(0x80);
		else if (line == 2)
			lcd_write(0xC0);

		else if (line == 3)
			lcd_write(0x94);
		else if (line == 4)
			lcd_write(0xD4);

		char[] charArray = message.toCharArray();
		for (char c : charArray) {
			lcd_write((int) c, Rs);
		}

	}

	/**
	 * Clear display and set cursor back to home position
	 */
	public void lcd_clear() {
		lcd_write(LCD_CLEARDISPLAY);
		lcd_write(LCD_RETURNHOME);

	}

	public static byte En = 0b00000100; // Enable bit
	public static byte Rw = 0b00000010; // Read/Write bit
	public static byte Rs = 0b00000001; // Register select bit

	// commands
	public static int LCD_CLEARDISPLAY = 0x01;
	public static int LCD_RETURNHOME = 0x02;
	public static int LCD_ENTRYMODESET = 0x04;
	public static int LCD_DISPLAYCONTROL = 0x08;
	public static int LCD_CURSORSHIFT = 0x10;
	public static int LCD_FUNCTIONSET = 0x20;
	public static int LCD_SETCGRAMADDR = 0x40;
	public static int LCD_SETDDRAMADDR = 0x80;

	// flags for display entry mode
	public static int LCD_ENTRYRIGHT = 0x00;
	public static int LCD_ENTRYLEFT = 0x02;
	public static int LCD_ENTRYSHIFTINCREMENT = 0x01;
	public static int LCD_ENTRYSHIFTDECREMENT = 0x00;

	// flags for display on/off control
	public static int LCD_DISPLAYON = 0x04;
	public static int LCD_DISPLAYOFF = 0x00;
	public static int LCD_CURSORON = 0x02;
	public static int LCD_CURSOROFF = 0x00;
	public static int LCD_BLINKON = 0x01;
	public static int LCD_BLINKOFF = 0x00;

	// flags for display/cursor shift
	public static int LCD_DISPLAYMOVE = 0x08;
	public static int LCD_CURSORMOVE = 0x00;
	public static int LCD_MOVERIGHT = 0x04;
	public static int LCD_MOVELEFT = 0x00;

	// flags for function set
	public static int LCD_8BITMODE = 0x10;
	public static int LCD_4BITMODE = 0x00;
	public static int LCD_2LINE = 0x08;
	public static int LCD_1LINE = 0x00;
	public static int LCD_5x10DOTS = 0x04;
	public static int LCD_5x8DOTS = 0x00;

	// flags for backlight control
	public static int LCD_BACKLIGHT = 0x08;
	public static int LCD_NOBACKLIGHT = 0x00;
}
