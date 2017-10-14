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
public class VCNL4000 {

	private I2CBus bus;
	private I2CDevice device;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public VCNL4000() {

	}

	/**
	 * sudo i2cdetect -y 1 shows 0x13 = 19
	 * 
	 * @param hwadd
	 */
	public VCNL4000(int hwadd) {
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

			// Write proximity adjustement register
			device.write(VCNL4000_PROXINITYADJUST, (byte) 0x81);
			device.write(VCNL4000_IRLED, (byte) 0x02); // default value by manufacturer is 0x02 = 20mA
			
		} catch (IOException e) {
			e.printStackTrace();
		} // rev 2 board, so it is bus 1

	}

	/**
	 * Read the proximity.
	 * 
	 * @return -1 in case of failure
	 */
	public int read_proximity() {
		int result = -1;
		int timeout = 500; // wait max 500 milisec
		int count = 0;
		try {
			// start measurement
			device.write(VCNL4000_COMMAND, (byte) VCNL4000_MEASUREPROXIMITY);
			//
			while (count < timeout) {
				count++;
				// measurement values already available (takes about 40 micro seconds)
				result = device.read(VCNL4000_COMMAND);
				// if bit VCNL4000_PROXIMITYREADY is set then ok
				if ((result & VCNL4000_PROXIMITYREADY) == VCNL4000_PROXIMITYREADY) {
					byte[] buf = new byte[2];
					// read the two bytes
					int res = device.read(VCNL4000_PROXIMITYDATA, buf, 0, 2);
					if (res != 2) {
						throw new RuntimeException("Read failure - got only "
								+ res + " bytes from VCNL4000");
					}
					// convert to an integer
					int prox = asInt(buf[0]) * 256 + asInt(buf[1]);
					return prox;
				}
				Thread.sleep(1);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
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

	// Address of the sensor
	public static int VCNL4000_ADDRESS = 0x13;

	// Commands
	public static int VCNL4000_COMMAND = 0x80;
	public static int VCNL4000_PRODUCTID = 0x81;
	public static int VCNL4000_IRLED = 0x83;
	public static int VCNL4000_AMBIENTPARAMETER = 0x84;
	public static int VCNL4000_AMBIENTDATA = 0x85;
	public static int VCNL4000_PROXIMITYDATA = 0x87;
	public static int VCNL4000_SIGNALFREQ = 0x89;
	public static int VCNL4000_PROXINITYADJUST = 0x8A;

	public static int VCNL4000_3M125 = 0;
	public static int VCNL4000_1M5625 = 1;
	public static int VCNL4000_781K25 = 2;
	public static int VCNL4000_390K625 = 3;

	public static int VCNL4000_MEASUREAMBIENT = 0x10;
	public static int VCNL4000_MEASUREPROXIMITY = 0x08;
	public static int VCNL4000_AMBIENTREADY = 0x40;
	public static int VCNL4000_PROXIMITYREADY = 0x20;
}
