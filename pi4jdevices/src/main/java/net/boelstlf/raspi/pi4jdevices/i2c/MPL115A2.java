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
public class MPL115A2 {

	private I2CBus bus;
	private I2CDevice device;
	private float mpl115a2_a0 = 0.0F;
	private float mpl115a2_b1 = 0.0F;
	private float mpl115a2_b2 = 0.0F;
	private float mpl115a2_c12 = 0.0F;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public MPL115A2() {

	}

	/**
	 * sudo i2cdetect -y 1 shows 0x60 = 96
	 * 
	 * @param hwaddr
	 */
	public MPL115A2(int hwaddr) {
		try {
			// get the I2C Bus 1 as we run Rev 2 board
			try {
				bus = I2CFactory.getInstance(I2CBus.BUS_1);
			} catch (UnsupportedBusNumberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connected to bus ok! " + I2CBus.BUS_1);

			// get the device at I2C address
			device = bus.getDevice(hwaddr);
			System.out.println("Connected to device ok!");

			readCoefficients();
			System.out.println("Device configuration done.");

		} catch (IOException e) {
			e.printStackTrace();
		} // rev 2 board, so it is bus 1
	}

	/**
	 * Gets the factory-set coefficients for this particular sensor
	 */
	private void readCoefficients() {
		byte[] buf = new byte[8];

		try {
			device.read(MPL115A2_REGISTER_A0_COEFF_MSB, buf, 0, 8);
			int a0coeff = (buf[0] << 8) + buf[1];
			int b1coeff = (buf[2] << 8) + buf[0];
			int b2coeff = (buf[4] << 8) + buf[5];
			int c12coeff = (buf[6] << 8) + (buf[7] >> 2);
			
			//System.out.println("a0-0: " + buf[0] + ", a0-1: " + buf[1]);
			//System.out.println("a0: " + a0coeff + "\tb1: " + b1coeff + "\tb2: " + b2coeff + "\tc12: " + c12coeff);

			mpl115a2_a0 = (float) a0coeff / 8; // 3 - factional digits
			mpl115a2_b1 = (float) b1coeff / 8192;
			mpl115a2_b2 = (float) b2coeff / 16384;
			mpl115a2_c12 = (float) c12coeff;
			mpl115a2_c12 /= 4194304.0;

			//System.out.println("a0: " + mpl115a2_a0 + "\tb1: " + mpl115a2_b1 + "\tb2: " + mpl115a2_b2 + "\tc12: " + mpl115a2_c12);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the floating-point pressure level in kPa.
	 * 
	 * @return pressure reading
	 */
	public float getPressure()
	{
		return readTP().Pressure;
	}
	
	/**
	 * Gets the floating-point temperature in Centigrade.
	 * 
	 * @return temperature reading
	 */
	public float getTemperature()
	{
		return readTP().Temperature;
	}

	/**
	 * Read Temperature and Pressure
	 * 
	 * @return
	 */
	public TP readTP() {
		byte[] tempData = new byte[2];
		byte[] pressData = new byte[2];
		TP tp = new TP();

		try {
			// start a new measurement
			device.write(MPL115A2_REGISTER_STARTCONVERSION, (byte) 0x00);
			Thread.sleep(5); // refer to data sheet, Conversion Time 1.6 ms,
								// double it to be on the save side

			// read raw pressure values
			device.read(MPL115A2_REGISTER_TEMP_MSB, tempData, 0, 2);
			int LSBval = (tempData[1] & 0xFF) >>> 6; // 10-bit only, i.e. only bit 7, 6 of LSB are used
			int MSBval = (tempData[0] & 0xFF) << 2; // shift 2-bit to left, i.e. multiplied with 4
			int rawTemp = MSBval + LSBval;
			//System.out.print("rawTemp: " + rawTemp);
			tp.Temperature = ((float) rawTemp - 498.0F) / -5.35F + 25.0F;

			// read raw temp values
			device.read(MPL115A2_REGISTER_PRESSURE_MSB, pressData, 0, 2);
			LSBval = (pressData[1] & 0xFF) >>> 6;
			MSBval = (pressData[0] & 0xFF) << 2;
			int rawPressure = MSBval + LSBval;
			// See data sheet p.6 for evaluation sequence
			float pressureComp = mpl115a2_a0 + (mpl115a2_b1 + mpl115a2_c12 * rawTemp ) * rawPressure + mpl115a2_b2 * rawTemp;
			//System.out.println("\trawPressure: " + rawPressure + "\tpressComp: " + pressureComp);
			tp.Pressure = ((65.0F / 1023.0F) * pressureComp) + 50.0F; 

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tp;
	}

	/**
	 * Inner class to define data type for Temperature and Pressure
	 * 
	 * @author frb
	 * 
	 */
	public static class TP {

		public float Temperature = -1;
		public float Pressure = -1;
		
		public String toString()
		{
			String msg = "";
			msg += "Temp: " + Temperature;
			msg += "\tPressure: " + Pressure;  //raw pressure for test purposes
			return msg;
		}
	}

	public static int MPL115A2_REGISTER_PRESSURE_MSB = (0x00);
	public static int MPL115A2_REGISTER_PRESSURE_LSB = (0x01);
	public static int MPL115A2_REGISTER_TEMP_MSB = (0x02);
	public static int MPL115A2_REGISTER_TEMP_LSB = (0x03);
	public static int MPL115A2_REGISTER_A0_COEFF_MSB = (0x04);
	public static int MPL115A2_REGISTER_A0_COEFF_LSB = (0x05);
	public static int MPL115A2_REGISTER_B1_COEFF_MSB = (0x06);
	public static int MPL115A2_REGISTER_B1_COEFF_LSB = (0x07);
	public static int MPL115A2_REGISTER_B2_COEFF_MSB = (0x08);
	public static int MPL115A2_REGISTER_B2_COEFF_LSB = (0x09);
	public static int MPL115A2_REGISTER_C12_COEFF_MSB = (0x0A);
	public static int MPL115A2_REGISTER_C12_COEFF_LSB = (0x0B);
	public static int MPL115A2_REGISTER_STARTCONVERSION = (0x12);
}
