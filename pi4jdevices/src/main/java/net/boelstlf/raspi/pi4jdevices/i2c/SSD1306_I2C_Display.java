/**
 * Based on Adafruit's python code for HT16KT33 I2C controller
 * Refer to https://github.com/adafruit/
 */
package net.boelstlf.raspi.pi4jdevices.i2c;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
//import com.pi4j.wiringpi.I2C;

/**
 * @author boelstlf
 *
 */
public class SSD1306_I2C_Display {

	protected int vccState;
	protected BufferedImage img;
	protected Graphics2D graphics;
	private int width, height, pages;
	private I2CDevice device;

	private byte[] buffer;
	public final static int NUM_ROWS = 5;
	private String[] rows = new String[NUM_ROWS];

	/**
	 * Display object using I2C communication with a reset pin <br/>
	 * As I haven't got an I2C display and I don't understand I2C much, I just
	 * tried to copy the Adafruit's library and I am using a hack to use
	 * WiringPi function similar to one in the original lib directly.
	 *
	 * @param width
	 *            Display width
	 * @param height
	 *            Display height
	 * @param gpio
	 *            GPIO object
	 * @param i2c
	 *            I2C object
	 * @param address
	 *            Display address
	 * @param rstPin
	 *            Reset pin
	 * @see GpioFactory#getInstance() GpioController instance factory
	 * @see com.pi4j.io.i2c.I2CFactory#getInstance(int) I2C bus factory
	 * @throws ReflectiveOperationException
	 *             Thrown if I2C handle is not accessible
	 * @throws IOException
	 *             Thrown if the bus can't return device for specified address
	 */
	public SSD1306_I2C_Display(int width, int height, int address) {
		this.width = width;
		this.height = height;
		this.pages = (height / 8);
		this.buffer = new byte[width * this.pages];

		this.img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		this.graphics = this.img.createGraphics();

		// get the device at I2C address
		try {
			I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

			device = bus.getDevice(address);
			System.out.println("Connected to device ok!");

			this.begin();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// init the rows buffer
		for (int i = 0; i < rows.length; i++) {
			rows[i] = "";
		}

		System.out.println("Instance of SSD1306 created");
	}

	public SSD1306_I2C_Display(int address) {
		this(SSD1306_I2C_Display.LCD_WIDTH_128, SSD1306_I2C_Display.LCD_HEIGHT_64, address);
	}

	private void initDisplay() throws IOException {
		if (this.width == LCD_WIDTH_128 && this.height == LCD_HEIGHT_64) {
			this.init(0x3F, 0x12, 0x80);
		} else if (this.width == LCD_WIDTH_128 && this.height == LCD_HEIGHT_32) {
			this.init(0x1F, 0x02, 0x80);
		} else if (this.width == LCD_WIDTH_96 && this.height == LCD_HEIGHT_16) {
			this.init(0x0F, 0x02, 0x60);
		} else {
			throw new IOException("Invalid width: " + this.width + " or height: " + this.height);
		}

	}

	private void init(int multiplex, int compins, int ratio) {
		this.command(SSD1306_DISPLAYOFF);
		this.command(SSD1306_SETDISPLAYCLOCKDIV);
		this.command((short) ratio);
		this.command(SSD1306_SETMULTIPLEX);
		this.command((short) multiplex);
		this.command(SSD1306_SETDISPLAYOFFSET);
		this.command((short) 0x0);
		this.command(SSD1306_SETSTARTLINE);
		this.command(SSD1306_CHARGEPUMP);

		if (this.vccState == SSD1306_EXTERNALVCC)
			this.command((short) 0x10);
		else
			this.command((short) 0x14);

		this.command(SSD1306_MEMORYMODE);
		this.command((short) 0x00);
		this.command((short) (SSD1306_SEGREMAP | 0x1));
		this.command(SSD1306_COMSCANDEC);
		this.command(SSD1306_SETCOMPINS);
		this.command((short) compins);
		this.command(SSD1306_SETCONTRAST);

		if (this.vccState == SSD1306_EXTERNALVCC)
			this.command((short) 0x9F);
		else
			this.command((short) 0xCF);

		this.command(SSD1306_SETPRECHARGE);

		if (this.vccState == SSD1306_EXTERNALVCC)
			this.command((short) 0x22);
		else
			this.command((short) 0xF1);

		this.command(SSD1306_SETVCOMDETECT);
		this.command((short) 0x40);
		this.command(SSD1306_DISPLAYALLON_RESUME);
		this.command(SSD1306_NORMALDISPLAY);
	}

	/**
	 * Turns on command mode and sends command
	 * 
	 * @param command
	 *            Command to send. Should be in short range.
	 */
	private void command(int command) {
		this.i2cWrite(0, command);
	}

	/**
	 * Turns on data mode and sends data
	 * 
	 * @param data
	 *            Data to send. Should be in short range.
	 */
	public void data(int data) {
		this.i2cWrite(0x40, data);
	}

	/**
	 * Turns on data mode and sends data array
	 * 
	 * @param data
	 *            Data array
	 */
	private void data(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < 16; j++) {
				this.i2cWrite(0x40, data[i]);
				i++;
			}
			i--;
		}

	}

	/**
	 * Begin with SWITCHCAPVCC VCC mode
	 * 
	 * @see SSD1306_Constants#SSD1306_SWITCHCAPVCC
	 */
	public void begin() throws IOException {
		this.begin(SSD1306_SWITCHCAPVCC);
	}

	/**
	 * Begin with specified VCC mode (can be SWITCHCAPVCC or EXTERNALVCC)
	 * 
	 * @param vccState
	 *            VCC mode
	 * @see SSD1306_Constants#SSD1306_SWITCHCAPVCC
	 * @see SSD1306_Constants#SSD1306_EXTERNALVCC
	 */
	public void begin(int vccState) throws IOException {
		this.vccState = vccState;

		this.initDisplay();
		this.command(SSD1306_DISPLAYON);
		this.clear();
		this.display();
	}

	/**
	 * Sends the buffer to the display
	 */
	public synchronized void display() {
		this.command(SSD1306_COLUMNADDR);
		this.command(0);
		this.command(this.width - 1);
		this.command(SSD1306_PAGEADDR);
		this.command(0);
		this.command(this.pages - 1);

		this.data(this.buffer);
	}

	/**
	 * Clears the buffer by creating a new byte array
	 */
	public void clear() {
		this.buffer = new byte[this.width * this.pages];
	}

	/**
	 * Sets the display contract. Apparently not really working.
	 * 
	 * @param contrast
	 *            Contrast
	 */
	public void setContrast(byte contrast) {
		this.command(SSD1306_SETCONTRAST);
		this.command(contrast);
	}

	/**
	 * Sets if the backlight should be dimmed
	 * 
	 * @param dim
	 *            Dim state
	 */
	public void dim(boolean dim) {
		if (dim) {
			this.setContrast((byte) 0);
		} else {
			if (this.vccState == SSD1306_EXTERNALVCC) {
				this.setContrast((byte) 0x9F);
			} else {
				this.setContrast((byte) 0xCF);
			}
		}
	}

	/**
	 * Sets if the display should be inverted
	 * 
	 * @param invert
	 *            Invert state
	 */
	public void invertDisplay(boolean invert) {
		if (invert) {
			this.command(SSD1306_INVERTDISPLAY);
		} else {
			this.command(SSD1306_NORMALDISPLAY);
		}
	}

	/**
	 * Probably broken
	 */
	public void scrollHorizontally(boolean left, int start, int end) {
		this.command(left ? SSD1306_LEFT_HORIZONTAL_SCROLL : SSD1306_RIGHT_HORIZONTAL_SCROLL);
		this.command(0);
		this.command(start);
		this.command(0);
		this.command(end);
		this.command(1);
		this.command(0xFF);
		this.command(SSD1306_ACTIVATE_SCROLL);
	}

	/**
	 * Probably broken
	 */
	public void scrollDiagonally(boolean left, int start, int end) {
		this.command(SSD1306_SET_VERTICAL_SCROLL_AREA);
		this.command(0);
		this.command(this.height);
		this.command(left ? SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL : SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL);
		this.command(0);
		this.command(start);
		this.command(0);
		this.command(end);
		this.command(1);
		this.command(SSD1306_ACTIVATE_SCROLL);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScroll() {
		this.command(SSD1306_DEACTIVATE_SCROLL);
	}

	/**
	 * @return Display width
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * @return Display height
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Sets one pixel in the current buffer
	 * 
	 * @param x
	 *            X position
	 * @param y
	 *            Y position
	 * @param white
	 *            White or black pixel
	 * @return True if the pixel was successfully set
	 */
	public boolean setPixel(int x, int y, boolean white) {
		if (x < 0 || x > this.width || y < 0 || y > this.height) {
			return false;
		}

		if (white) {
			this.buffer[x + (y / 8) * this.width] |= (1 << (y & 7));
		} else {
			this.buffer[x + (y / 8) * this.width] &= ~(1 << (y & 7));
		}

		return true;
	}

	/**
	 * Copies AWT image contents to buffer. Calls display()
	 * 
	 * @see SSD1306_I2C_Display#display()
	 */
	public synchronized void displayImage() {
		Raster r = this.img.getRaster();

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.setPixel(x, y, (r.getSample(x, y, 0) > 0));
			}
		}

		this.display();
	}

	/**
	 * Sets internal buffer
	 * 
	 * @param buffer
	 *            New used buffer
	 */
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	/**
	 * Sets one byte in the buffer
	 * 
	 * @param position
	 *            Position to set
	 * @param value
	 *            Value to set
	 */
	public void setBufferByte(int position, byte value) {
		this.buffer[position] = value;
	}

	/**
	 * Sets internal AWT image to specified one.
	 * 
	 * @param img
	 *            BufferedImage to set
	 * @param createGraphics
	 *            If true, createGraphics() will be called on the image and the
	 *            result will be saved to the internal Graphics field accessible
	 *            by getGraphics() method
	 */
	public void setImage(BufferedImage img, boolean createGraphics) {
		this.img = img;

		if (createGraphics) {
			this.graphics = img.createGraphics();
		}
	}

	public void clearImage() {
		this.graphics.setBackground(new Color(0, 0, 0, 0));
		this.graphics.clearRect(0, 0, img.getWidth(), img.getHeight());
	}

	/**
	 * Returns internal AWT image
	 * 
	 * @return BufferedImage
	 */
	public BufferedImage getImage() {
		return this.img;
	}

	/**
	 * Returns Graphics object which is associated to current AWT image, if it
	 * wasn't set using setImage() with false createGraphics parameter
	 * 
	 * @return Graphics2D object
	 */
	public Graphics2D getGraphics() {
		return this.graphics;
	}

	/**
	 * Clears the screen and displays the string sent in, adding new lines as
	 * needed
	 * 
	 * @param data
	 * @param line
	 */
	public void displayString(String... data) {
		clearImage();
		for (int i = 0; i < data.length; i++) {
			graphics.drawString(data[i], 0, STRING_HEIGHT * (i + 1));
		}
		displayImage();
	}

	/**
	 * Clears the screen and displays the string sent in, adding new lines as
	 * needed
	 * 
	 * @param data
	 * @param line
	 */
	public void displayString(int x, int y, String... data) {
		clearImage();
		for (int i = 0; i < data.length; i++) {
			graphics.drawString(data[i], x, y + STRING_HEIGHT * (i + 1));
		}
		displayImage();
	}

	/**
	 * Set the string on position column and row.
	 * 
	 * @param data
	 * @param row
	 *            between 1..NUM_ROWS
	 * @param col
	 *            between 1..20
	 */
	public void setString(int row, int col, String... data) {
		if (row > 0 && row <= NUM_ROWS && col > 0 && col < 21) {
			for (int i = 0; i < data.length; i++) {
				graphics.drawString(data[i], (col - 1) * STRING_HEIGHT,
						(row - 1) * STRING_HEIGHT + STRING_HEIGHT * (i + 1));
			}
		}

	}

	/**
	 * Set the string on position column and row.
	 * 
	 * @param data
	 * @param row
	 *            between 1..NUM_ROWS
	 */
	public void setString(int row, String message) {
		if (row > 0 && row <= NUM_ROWS) {
			System.out.println("set string '" + message + "' on row '" + row + "'");
			clearImage();
			rows[row - 1] = message;
			for (int i = 0; i < rows.length; i++)
				setString(i + 1, 1, rows[i]);
			displayImage();
		} else {
			System.out.println("row not between 1 and " + NUM_ROWS + "; row is given as '" + row + "'");
		}
	}

	public void horizontalLine(int position) {
		for (int i = 0; i < width; i++) {
			setPixel(i, position, true);
		}
		display();
	}

	private void i2cWrite(int register, int value) {
		value &= 0xFF;

		try {
			device.write(register, (byte) value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final short SSD1306_I2C_ADDRESS = 0x3C;
	public static final short SSD1306_SETCONTRAST = 0x81;
	public static final short SSD1306_DISPLAYALLON_RESUME = 0xA4;
	public static final short SSD1306_DISPLAYALLON = 0xA5;
	public static final short SSD1306_NORMALDISPLAY = 0xA6;
	public static final short SSD1306_INVERTDISPLAY = 0xA7;
	public static final short SSD1306_DISPLAYOFF = 0xAE;
	public static final short SSD1306_DISPLAYON = 0xAF;
	public static final short SSD1306_SETDISPLAYOFFSET = 0xD3;
	public static final short SSD1306_SETCOMPINS = 0xDA;
	public static final short SSD1306_SETVCOMDETECT = 0xDB;
	public static final short SSD1306_SETDISPLAYCLOCKDIV = 0xD5;
	public static final short SSD1306_SETPRECHARGE = 0xD9;
	public static final short SSD1306_SETMULTIPLEX = 0xA8;
	public static final short SSD1306_SETLOWCOLUMN = 0x00;
	public static final short SSD1306_SETHIGHCOLUMN = 0x10;
	public static final short SSD1306_SETSTARTLINE = 0x40;
	public static final short SSD1306_MEMORYMODE = 0x20;
	public static final short SSD1306_COLUMNADDR = 0x21;
	public static final short SSD1306_PAGEADDR = 0x22;
	public static final short SSD1306_COMSCANINC = 0xC0;
	public static final short SSD1306_COMSCANDEC = 0xC8;
	public static final short SSD1306_SEGREMAP = 0xA0;
	public static final short SSD1306_CHARGEPUMP = 0x8D;
	public static final short SSD1306_EXTERNALVCC = 0x1;
	public static final short SSD1306_SWITCHCAPVCC = 0x2;

	public static final short SSD1306_ACTIVATE_SCROLL = 0x2F;
	public static final short SSD1306_DEACTIVATE_SCROLL = 0x2E;
	public static final short SSD1306_SET_VERTICAL_SCROLL_AREA = 0xA3;
	public static final short SSD1306_RIGHT_HORIZONTAL_SCROLL = 0x26;
	public static final short SSD1306_LEFT_HORIZONTAL_SCROLL = 0x27;
	public static final short SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
	public static final short SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;

	public static final int LCD_WIDTH_128 = 128;
	public static final int LCD_WIDTH_96 = 96;
	public static final int LCD_HEIGHT_64 = 64;
	public static final int LCD_HEIGHT_32 = 32;
	public static final int LCD_HEIGHT_16 = 16;

	public final static int STRING_HEIGHT = 12;
}
