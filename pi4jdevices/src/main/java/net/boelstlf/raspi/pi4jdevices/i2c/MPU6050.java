/**
 * 
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
public class MPU6050 {

	private I2CBus bus;
	private I2CDevice device;
	int result;
	int error;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public MPU6050() {

	}

	/**
	 * sudo i2cdetect -y 1 shows 0x68 = 104
	 * 
	 * @param hwadd
	 */
	public MPU6050(int hwadd) {
		// default at power-up:
		// Gyro at 250 degrees second
		// Acceleration at 2g
		// Clock source at internal 8MHz
		// The device is in sleep mode.
		//
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
			System.out.println("Connected to device on hwadd '" + hwadd + "' ok!");

			result = device.read(MPU6050_WHO_AM_I);
			System.out.println("WHO_AM_I: " + result);

			// According to the datasheet, the 'sleep' bit
			// should read a '1'. But I read a '0'.
			// That bit has to be cleared, since the sensor
			// is in sleep mode at power-up. Even if the
			// bit reads '0'.
			result = device.read(MPU6050_PWR_MGMT_2);
			System.out.println("PWR_MGMT_2: " + result);

			// Clear the 'sleep' bit to start the sensor.
			device.write(MPU6050_PWR_MGMT_1, (byte) 0x00);
			device.write(MPU6050_PWR_MGMT_2, (byte) 0x00);
			System.out.println("Configuring Device OK!");

			// config gyro
			device.write(MPU6050_GYRO_CONFIG, (byte) 0xE0); // 0b11100000
			// config accel
			device.write(MPU6050_ACCEL_CONFIG, (byte) 0x19); // 0b00011001
			System.out.println("Configuring sensors OK!");

		} catch (IOException e) {
			e.printStackTrace();
		} // rev 2 board, so it is bus 1

	}

	/**
	 * @return
	 */
	public ThreeAxisAndGyro readAccl() {
		ThreeAxisAndGyro ret = new ThreeAxisAndGyro();
		byte[] accelData = new byte[6];
		byte[] gyroData = new byte[6];

		try {

			// read 3D accel values
			error = device.read(MPU6050_ACCEL_XOUT_H, accelData, 0, 6);
			if (error < 6)
				System.out.println("Could not read all data");
			else {
				ret.x_accel = get2C(asInt(accelData[0]) * 256 + asInt(accelData[1]));
				ret.y_accel = get2C(asInt(accelData[2]) * 256 + asInt(accelData[3]));
				ret.z_accel = get2C(asInt(accelData[4]) * 256 + asInt(accelData[5]));
			}

			// read 3D gyro data
			error = device.read(MPU6050_GYRO_XOUT_H, gyroData, 0, 6);
			if (error < 6)

				System.out.println("Could not read all data");
			else {
				ret.x_gyro = get2C(asInt(gyroData[0]) * 256 + asInt(gyroData[1]));
				ret.y_gyro = get2C(asInt(gyroData[2]) * 256 + asInt(gyroData[3]));
				ret.z_gyro = get2C(asInt(gyroData[4]) * 256 + asInt(gyroData[5]));
			}
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
	
	private int get2C(int val)
	{
		if(val>0x8000)
			val=-((65535 - val) + 1);
		return val;

	}

	/**
	 * Inner class for 3D return value, x, y, z on acceleration and gyro.
	 * 
	 * @author frb
	 * 
	 */
	public static class ThreeAxisAndGyro {

		private static final double ACCELSCALE = 16384.0;
		private static final double GYROSCALE = 131.0;
		public int x_accel = -1;
		public int y_accel = -1;
		public int z_accel = -1;
		public int x_gyro = -1;
		public int y_gyro = -1;
		public int z_gyro = -1;

		private double dist(double a, double b) {
			return Math.sqrt((a * a) + (b * b));
		}

		public double getYRotation() {
			double radians = Math.atan2(x_accel_scaled(), dist(y_accel_scaled(), z_accel_scaled()));
			return -Math.toDegrees(radians);
		}

		public double getXRotation() {
			double radians = Math.atan2(y_accel_scaled(), dist(x_accel_scaled(), z_accel_scaled()));
			return Math.toDegrees(radians);
		}

		public double x_accel_scaled() {
			return x_accel / ACCELSCALE;
		}

		public double y_accel_scaled() {
			return y_accel / ACCELSCALE;
		}

		public double z_accel_scaled() {
			return z_accel / ACCELSCALE;
		}

		public double x_gyro_scaled() {
			return (double) (x_gyro / GYROSCALE);
		}

		public double y_gyro_scaled() {
			return (double) (y_gyro / GYROSCALE);
		}

		public double z_gyro_scaled() {
			return (double) (z_gyro / GYROSCALE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {

			String msg;
			msg = String.format("xRot: %10.3f \tyRot: %10.3f ", getXRotation(), getYRotation());
			msg += String.format("\tgyroX: %10.3f \tgyroY: %10.3f \tgyroZ: %10.3f", x_gyro_scaled(), y_gyro_scaled(), z_gyro_scaled());
			msg += String.format("\tacclX: %10.3f \tacclY: %10.3f \tacclZ: %10.3f", x_accel_scaled(), y_accel_scaled(), z_accel_scaled());
			return msg;
		}
	}

	// Register names according to the datasheet.
	// According to the InvenSense document
	// "MPU-6000 and MPU-6050 Register Map
	// and Descriptions Revision 3.2", there are no registers
	// at 0x02 ... 0x18, but according other information
	// the registers in that unknown area are for gain
	// and offsets.
	public static int MPU6050_AUX_VDDIO = 0x01; // R/W
	public static int MPU6050_SMPLRT_DIV = 0x19; // R/W
	public static int MPU6050_CONFIG = 0x1A; // R/W
	public static int MPU6050_GYRO_CONFIG = 0x1B; // R/W
	public static int MPU6050_ACCEL_CONFIG = 0x1C; // R/W
	public static int MPU6050_FF_THR = 0x1D; // R/W
	public static int MPU6050_FF_DUR = 0x1E; // R/W
	public static int MPU6050_MOT_THR = 0x1F; // R/W
	public static int MPU6050_MOT_DUR = 0x20; // R/W
	public static int MPU6050_ZRMOT_THR = 0x21; // R/W
	public static int MPU6050_ZRMOT_DUR = 0x22; // R/W
	public static int MPU6050_FIFO_EN = 0x23; // R/W
	public static int MPU6050_I2C_MST_CTRL = 0x24; // R/W
	public static int MPU6050_I2C_SLV0_ADDR = 0x25; // R/W
	public static int MPU6050_I2C_SLV0_REG = 0x26; // R/W
	public static int MPU6050_I2C_SLV0_CTRL = 0x27; // R/W
	public static int MPU6050_I2C_SLV1_ADDR = 0x28; // R/W
	public static int MPU6050_I2C_SLV1_REG = 0x29; // R/W
	public static int MPU6050_I2C_SLV1_CTRL = 0x2A; // R/W
	public static int MPU6050_I2C_SLV2_ADDR = 0x2B; // R/W
	public static int MPU6050_I2C_SLV2_REG = 0x2C; // R/W
	public static int MPU6050_I2C_SLV2_CTRL = 0x2D; // R/W
	public static int MPU6050_I2C_SLV3_ADDR = 0x2E; // R/W
	public static int MPU6050_I2C_SLV3_REG = 0x2F; // R/W
	public static int MPU6050_I2C_SLV3_CTRL = 0x30; // R/W
	public static int MPU6050_I2C_SLV4_ADDR = 0x31; // R/W
	public static int MPU6050_I2C_SLV4_REG = 0x32; // R/W
	public static int MPU6050_I2C_SLV4_DO = 0x33; // R/W
	public static int MPU6050_I2C_SLV4_CTRL = 0x34; // R/W
	public static int MPU6050_I2C_SLV4_DI = 0x35; // R
	public static int MPU6050_I2C_MST_STATUS = 0x36; // R
	public static int MPU6050_INT_PIN_CFG = 0x37; // R/W
	public static int MPU6050_INT_ENABLE = 0x38; // R/W
	public static int MPU6050_INT_STATUS = 0x3A; // R
	public static int MPU6050_ACCEL_XOUT_H = 0x3B; // R
	public static int MPU6050_ACCEL_XOUT_L = 0x3C; // R
	public static int MPU6050_ACCEL_YOUT_H = 0x3D; // R
	public static int MPU6050_ACCEL_YOUT_L = 0x3E; // R
	public static int MPU6050_ACCEL_ZOUT_H = 0x3F; // R
	public static int MPU6050_ACCEL_ZOUT_L = 0x40; // R
	public static int MPU6050_TEMP_OUT_H = 0x41; // R
	public static int MPU6050_TEMP_OUT_L = 0x42; // R
	public static int MPU6050_GYRO_XOUT_H = 0x43; // R
	public static int MPU6050_GYRO_XOUT_L = 0x44; // R
	public static int MPU6050_GYRO_YOUT_H = 0x45; // R
	public static int MPU6050_GYRO_YOUT_L = 0x46; // R
	public static int MPU6050_GYRO_ZOUT_H = 0x47; // R
	public static int MPU6050_GYRO_ZOUT_L = 0x48; // R
	public static int MPU6050_EXT_SENS_DATA_00 = 0x49; // R
	public static int MPU6050_EXT_SENS_DATA_01 = 0x4A; // R
	public static int MPU6050_EXT_SENS_DATA_02 = 0x4B; // R
	public static int MPU6050_EXT_SENS_DATA_03 = 0x4C; // R
	public static int MPU6050_EXT_SENS_DATA_04 = 0x4D; // R
	public static int MPU6050_EXT_SENS_DATA_05 = 0x4E; // R
	public static int MPU6050_EXT_SENS_DATA_06 = 0x4F; // R
	public static int MPU6050_EXT_SENS_DATA_07 = 0x50; // R
	public static int MPU6050_EXT_SENS_DATA_08 = 0x51; // R
	public static int MPU6050_EXT_SENS_DATA_09 = 0x52; // R
	public static int MPU6050_EXT_SENS_DATA_10 = 0x53; // R
	public static int MPU6050_EXT_SENS_DATA_11 = 0x54; // R
	public static int MPU6050_EXT_SENS_DATA_12 = 0x55; // R
	public static int MPU6050_EXT_SENS_DATA_13 = 0x56; // R
	public static int MPU6050_EXT_SENS_DATA_14 = 0x57; // R
	public static int MPU6050_EXT_SENS_DATA_15 = 0x58; // R
	public static int MPU6050_EXT_SENS_DATA_16 = 0x59; // R
	public static int MPU6050_EXT_SENS_DATA_17 = 0x5A; // R
	public static int MPU6050_EXT_SENS_DATA_18 = 0x5B; // R
	public static int MPU6050_EXT_SENS_DATA_19 = 0x5C; // R
	public static int MPU6050_EXT_SENS_DATA_20 = 0x5D; // R
	public static int MPU6050_EXT_SENS_DATA_21 = 0x5E; // R
	public static int MPU6050_EXT_SENS_DATA_22 = 0x5F; // R
	public static int MPU6050_EXT_SENS_DATA_23 = 0x60; // R
	public static int MPU6050_MOT_DETECT_STATUS = 0x61; // R
	public static int MPU6050_I2C_SLV0_DO = 0x63; // R/W
	public static int MPU6050_I2C_SLV1_DO = 0x64; // R/W
	public static int MPU6050_I2C_SLV2_DO = 0x65; // R/W
	public static int MPU6050_I2C_SLV3_DO = 0x66; // R/W
	public static int MPU6050_I2C_MST_DELAY_CTRL = 0x67; // R/W
	public static int MPU6050_SIGNAL_PATH_RESET = 0x68; // R/W
	public static int MPU6050_MOT_DETECT_CTRL = 0x69; // R/W
	public static int MPU6050_USER_CTRL = 0x6A; // R/W
	public static int MPU6050_PWR_MGMT_1 = 0x6B; // R/W
	public static int MPU6050_PWR_MGMT_2 = 0x6C; // R/W
	public static int MPU6050_FIFO_COUNTH = 0x72; // R/W
	public static int MPU6050_FIFO_COUNTL = 0x73; // R/W
	public static int MPU6050_FIFO_R_W = 0x74; // R/W
	public static int MPU6050_WHO_AM_I = 0x75; // R
}
