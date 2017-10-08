package net.boelstlf.raspi.pi4jdevices.i2c;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class ADXL345 {

	private I2CBus bus;
	private I2CDevice device;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public ADXL345() {

	}

	/**
	 * sudo i2cdetect -y 1 shows 0x53 = 83
	 * 
	 * @param hwadd
	 */
	public ADXL345(int hwadd) {
		try {
			// get the I2C Bus 1 as we run Rev 2 board
			try {
				bus = I2CFactory.getInstance(I2CBus.BUS_1);
			} catch (UnsupportedBusNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connected to bus ok! " + I2CBus.BUS_1);

			// get the device ADS1015 on 0x48
			device = bus.getDevice(hwadd);
			System.out.println("Connected to device ok!");

			device.write(ADXL345_PWR_CTL, (byte) ADXL345_STANDBY);
			device.write(ADXL345_BW_RATE, (byte) ADXL345_DR_100);
			device.write(ADXL345_DATA_FORMAT,
					(byte) (ADXL345_2G + ADXL345_FULL_RES));
			device.write(ADXL345_FIFO_CTL, (byte) ADXL345_STREAM);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read acceleration
	 * 
	 * @param scaleFactor
	 * @return
	 */
	public ThreeAxis readAccl(float scaleFactor) {
		ThreeAxis ret = new ThreeAxis();
		byte[] buf = new byte[2];
		int res;

		try {

			device.write(ADXL345_PWR_CTL, (byte) ADXL345_MEASURE);
			res = device.read(ADXL345_DATA_X0, buf, 0, 2);
			if (res != 2) {
				throw new RuntimeException("Read failure on 'X' - got only " + res
						+ " bytes from ADXL345");
			}
			ret.x = asInt(buf[0]) * 256 + asInt(buf[1]);

			res = device.read(ADXL345_DATA_Y0, buf, 0, 2);
			if (res != 2) {
				throw new RuntimeException("Read failure on 'Y' - got only " + res
						+ " bytes from ADXL345");
			}
			ret.y = asInt(buf[0]) * 256 + asInt(buf[1]);

			res = device.read(ADXL345_DATA_Y0, buf, 0, 2);
			if (res != 2) {
				throw new RuntimeException("Read failure on 'Z' - got only " + res
						+ " bytes from ADXL345");
			}
			ret.z = asInt(buf[0]) * 256 + asInt(buf[1]);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Utility method to convert byte to int, take care on negative values.
	 * 
	 * @param b
	 *            byte value
	 * @return integer value
	 */
	private int asInt(byte b) {
		int i = b;
		if (i < 0) {
			i = i + 256;
		}
		return i;
	}

	/**
	 * Inner class for 3D return value, x, y, z
	 * 
	 * @author frb
	 * 
	 */
	public static class ThreeAxis {

		public int x = -1;
		public int y = -1;
		public int z = -1;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "x: " + x + "\t\ty: " + y + "\t\tz: " + z;
		}
	}

	// Registers
	public static int ADXL345_DEVID = 0x00;
	public static int ADXL345_THRESH_TAP = 0x1D; // 8 bits
	public static int ADXL345_X_OFFSET = 0x1E; // 8 bits
	public static int ADXL345_Y_OFFSET = 0x1F; // 8 bits
	public static int ADXL345_Z_OFFSET = 0x20; // 8 bits
	public static int ADXL345_TAP_DUR = 0x21; // 8 bits
	public static int ADXL345_TAP_LATENCY = 0x22; // 8 bits
	public static int ADXL345_TAP_WINDOW = 0x23; // 8 bits
	public static int ADXL345_THRESH_ACT = 0x24; // 8 bits
	public static int ADXL345_THRESH_INACT = 0x25; // 8 bits
	public static int ADXL345_TIME_INACT = 0x26; // 8 bits
	public static int ADXL345_ACT_INACT_CTL = 0x27;
	public static int ADXL345_THRESH_FF = 0x28; // 8 bits
	public static int ADXL345_TIME_FF = 0x29; // 8 bits
	public static int ADXL345_TAP_AXES = 0x2A;
	public static int ADXL345_ACT_TAP_STATUS = 0x2B;
	public static int ADXL345_BW_RATE = 0x2C;
	public static int ADXL345_PWR_CTL = 0x2D;
	public static int ADXL345_INT_ENABLE = 0x2E;
	public static int ADXL345_INT_MAP = 0x2F;
	public static int ADXL345_INT_SOURCE = 0x30;
	public static int ADXL345_DATA_FORMAT = 0x31;
	public static int ADXL345_DATA_X0 = 0x32; // 8 bits
	public static int ADXL345_DATA_X1 = 0x33; // 8 bits
	public static int ADXL345_DATA_Y0 = 0x34; // 8 bits
	public static int ADXL345_DATA_Y1 = 0x35; // 8 bits
	public static int ADXL345_DATA_Z0 = 0x36; // 8 bits
	public static int ADXL345_DATA_Z1 = 0x37; // 8 bits
	public static int ADXL345_FIFO_CTL = 0x38;
	public static int ADXL345_FIFO_STATUS = 0x39;

	// Datarate controls using BW_RATE register 0x2c
	public static int ADXL345_DR_3200 = 0x0F;
	public static int ADXL345_DR_1600 = 0x0E;
	public static int ADXL345_DR_800 = 0x0D;
	public static int ADXL345_DR_400 = 0x0C;
	public static int ADXL345_DR_200 = 0x0B;
	public static int ADXL345_DR_100 = 0x0A;
	public static int ADXL345_DR_50 = 0x09;
	public static int ADXL345_DR_25 = 0x08;
	public static int ADXL345_DR_12_5 = 0x07;
	public static int ADXL345_DR_6_25 = 0x06;

	// Datarate controls using BW_RATE at low power
	public static int ADXL345_DR_400_LOWPOWER = 0x1C;
	public static int ADXL345_DR_200_LOWPOWER = 0x1B;
	public static int ADXL345_DR_100_LOWPOWER = 0X1A;
	public static int ADXL345_DR_50_LOWPOWER = 0x19;
	public static int ADXL345_DR_25_LOWPOWER = 0x18;
	public static int ADXL345_DR_12_5_LOWPOWER = 0x17;

	// Data Format using DATA_FORMAT 0x31
	public static int ADXL345_SELFTEST = 0x80;
	public static int ADXL345_SPI_BIT = 0x40;
	public static int ADXL345_INT_INVERT = 0x20;
	public static int ADXL345_FULL_RES = 0x08;
	public static int ADXL345_JUSTIFY = 0x04;
	public static int ADXL345_2G = 0x00;
	public static int ADXL345_4G = 0x01;
	public static int ADXL345_8G = 0x02;
	public static int ADXL345_16G = 0x03;

	// FIFO Control using FIFO_CTL 0x38
	public static int ADXL345_BYPASS = 0x00;
	public static int ADXL345_FIFO = 0x40;
	public static int ADXL345_STREAM = 0x80;
	public static int ADXL345_TRIG_MODE = 0xC0;
	public static int ADXL345_TRIG_INT1 = 0x00;
	public static int ADXL345_TRIG_INT2 = 0x20;
	public static int ADXL345_SAMPLES31 = 0x1F;
	public static int ADXL345_SAMPLES16 = 0x10;
	public static int ADXL345_SAMPLES10 = 0x0A;

	// Power control using PWR_CTL 0x2D
	public static int ADXL345_LINK_BIT = 0x20;
	public static int ADXL345_AUTO_SLEEP = 0x10;
	public static int ADXL345_MEASURE = 0x08;
	public static int ADXL345_STANDBY = 0x00;
	public static int ADXL345_SLEEP = 0x04;
	public static int ADXL345_WAKEUP8HZ = 0x00;
	public static int ADXL345_WAKEUP4HZ = 0x01;
	public static int ADXL345_WAKEUP2HZ = 0x2;
	public static int ADXL345_WAKEUP1HZ = 0x03;
}
