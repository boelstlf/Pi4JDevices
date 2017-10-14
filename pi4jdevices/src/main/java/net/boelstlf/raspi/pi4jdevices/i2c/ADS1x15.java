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
public class ADS1x15 {

	private I2CBus bus;
	private I2CDevice device;

	/**
	 * Default constructor without initializing bus and device in order to be
	 * able to run TestNG on non-Raspi host.
	 */
	public ADS1x15() {
	}

	/**
	 * sudo i2cdetect -y 1 shows 0x48 = 72
	 */
	public ADS1x15(int hwadd) {
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

		} catch (IOException e) {
			e.printStackTrace();
		} // rev 2 board, so it is bus 1
	}

	/**
	 * Gets a single-ended ADC reading from the specified channel in mV. The
	 * sample rate for this mode (single-shot) can be used to lower the noise
	 * (low sps) or to lower the power consumption (high sps) by duty cycling,
	 * see datasheet page 14 for more info. The pga must be given in mV, see
	 * page 13 for the supported values."
	 * 
	 * @param channel 0..3
	 * @param pga
	 * @param sps
	 */
	public double readADCSingleEnded(int channel, int pga, int sps) {
		System.out.println("read from single channel: '" + channel
				+ "'; with PGA: '" + pga + "' and SPS: '" + sps + "'");

		// With invalid channel return -1
		if (channel > 3) {
			System.out
					.println("ADS1x15: Invalid channel specified: " + channel);
			return -1;
		}

		// calculate 16-bit config settings
		byte[] writebuf = this.getADCSingleEndedConfig(channel, pga, sps);
		// write 16-bit setting to Config-Register (0x01)
		try {
			device.write(ADS1x15.ADS1015_REG_POINTER_CONFIG, writebuf,
					0, 2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Wait for the ADC conversion to complete
		// The minimum delay depends on the sps: delay >= 1/sps
		// We add 2ms to be sure
		try {
			Thread.sleep(1000 / sps + 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Read the conversion results (register 0x00)
		byte[] readbuf = new byte[2];
		try {
			int res = device.read(ADS1x15.ADS1015_REG_POINTER_CONVERT,
					readbuf, 0, 2);
			if (res != 2) {
				System.out.println("Error reading data < 2 bytes");
			}
			// Shift right 4 bits for the 12-bit ADS1015 and convert to mV
			double volt = ((readbuf[0] << 8 | (readbuf[1] & 0xFF)) >> 4) * pga
					/ 2048.0;
			return volt;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param channel
	 * @param pga
	 * @param sps
	 * @return
	 */
	public byte[] getADCSingleEndedConfig(int channel, int pga, int sps) {
		// Disable comparator, Non-latching, Alert/Rdy active low
		// traditional comparator, single-shot mode

		int config = ADS1x15.ADS1015_REG_CONFIG_CQUE_NONE
				| ADS1x15.ADS1015_REG_CONFIG_CLAT_NONLAT
				| ADS1x15.ADS1015_REG_CONFIG_CPOL_ACTVLOW
				| ADS1x15.ADS1015_REG_CONFIG_CMODE_TRAD
				| ADS1x15.ADS1015_REG_CONFIG_MODE_SINGLE;

		// Set sample per seconds, defaults to 250sps
		// If sps is in the dictionary (defined in init) it returns the value of
		// the constant
		// otherwise it returns the value for 250sps. This saves a lot of
		// if/else if/else code!
		config |= getSPSConfigADS1015(sps);

		// Set PGA/voltage range, defaults to +-6.144V
		config |= getPGAConfig(pga);

		// Set the channel to be converted
		if (channel == 3)
			config |= ADS1x15.ADS1015_REG_CONFIG_MUX_SINGLE_3;
		else if (channel == 2)
			config |= ADS1x15.ADS1015_REG_CONFIG_MUX_SINGLE_2;
		else if (channel == 1)
			config |= ADS1x15.ADS1015_REG_CONFIG_MUX_SINGLE_1;
		else
			config |= ADS1x15.ADS1015_REG_CONFIG_MUX_SINGLE_0;

		// Set 'start single-conversion' bit
		config |= ADS1x15.ADS1015_REG_CONFIG_OS_SINGLE;

		// Write config register to the ADC
		byte[] writebuf = new byte[2];
		writebuf[0] = (byte) (config >>> 8);
		// System.out.printf("%d %s%n", writebuf[0],
		// Integer.toBinaryString(writebuf[0]));
		writebuf[1] = (byte) (config & 0xFF);
		// System.out.printf("%d %s%n", writebuf[1],
		// Integer.toBinaryString(writebuf[1]));

		// System.out.println("Byte 0: " + (writebuf[0] & 0xFF) + "; byte 1: "+
		// (writebuf[1] & 0xFF));

		return writebuf;
	}

	/**
	 * @param sps
	 * @return
	 */
	public int getSPSConfigADS1015(int sps) {
		int spsConfig;
		switch (sps) {
		case 128:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_128SPS;
			break;
		case 250:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_250SPS;
			break;
		case 490:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_490SPS;
			break;
		case 920:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_920SPS;
			break;
		case 1600:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_1600SPS;
			break;
		case 2400:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_2400SPS;
			break;
		case 3300:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_3300SPS;
			break;
		default:
			spsConfig = ADS1x15.ADS1015_REG_CONFIG_DR_250SPS; // default
																		// set
																		// to
																		// 250sps
		}
		// System.out.println("Config for sps: '" + sps + "' is :" + spsConfig);
		return spsConfig;
	}

	/**
	 * @param pga
	 * @return
	 */
	public int getPGAConfig(int pga) {
		int pgaConfig;
		switch (pga) {
		case 6144:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_6_144V;
			break;
		case 4096:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_4_096V;
			break;
		case 2048:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_2_048V;
			break;
		case 1024:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_1_024V;
			break;
		case 512:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_0_512V;
			break;
		case 256:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_0_256V;
			break;
		default:
			pgaConfig = ADS1x15.ADS1015_REG_CONFIG_PGA_6_144V;

		}
		// System.out.println("Config for pga: '" + pga + "' is :" + pgaConfig);
		return pgaConfig;
	}

	// IC Identifiers
	public static int IC_ADS1015 = 0x00;
	public static int IC_ADS1115 = 0x01;

	// Pointer Register
	public static int ADS1015_REG_POINTER_MASK = 0x03;
	public static int ADS1015_REG_POINTER_CONVERT = 0x00;
	public static int ADS1015_REG_POINTER_CONFIG = 0x01;
	public static int ADS1015_REG_POINTER_LOWTHRESH = 0x02;
	public static int ADS1015_REG_POINTER_HITHRESH = 0x03;

	// Config Register
	public static int ADS1015_REG_CONFIG_OS_MASK = 0x8000;
	public static int ADS1015_REG_CONFIG_OS_SINGLE = 0x8000; // Write: Set to
																// start a
																// single-conversion
	public static int ADS1015_REG_CONFIG_OS_BUSY = 0x0000; // Read: Bit = 0 when
															// conversion is in
															// progress
	public static int ADS1015_REG_CONFIG_OS_NOTBUSY = 0x8000; // Read: Bit = 1
																// when device
																// is not
																// performing a
																// conversion

	public static int ADS1015_REG_CONFIG_MUX_MASK = 0x7000;
	public static int ADS1015_REG_CONFIG_MUX_DIFF_0_1 = 0x0000; // Differential
																// P = AIN0, N =
																// AIN1
																// (default)
	public static int ADS1015_REG_CONFIG_MUX_DIFF_0_3 = 0x1000; // Differential
																// P = AIN0, N =
																// AIN3
	public static int ADS1015_REG_CONFIG_MUX_DIFF_1_3 = 0x2000; // Differential
																// P = AIN1, N =
																// AIN3
	public static int ADS1015_REG_CONFIG_MUX_DIFF_2_3 = 0x3000; // Differential
																// P = AIN2, N =
																// AIN3
	public static int ADS1015_REG_CONFIG_MUX_SINGLE_0 = 0x4000; // Single-ended
																// AIN0
	public static int ADS1015_REG_CONFIG_MUX_SINGLE_1 = 0x5000; // Single-ended
																// AIN1
	public static int ADS1015_REG_CONFIG_MUX_SINGLE_2 = 0x6000; // Single-ended
																// AIN2
	public static int ADS1015_REG_CONFIG_MUX_SINGLE_3 = 0x7000; // Single-ended
																// AIN3

	public static int ADS1015_REG_CONFIG_PGA_MASK = 0x0E00;
	public static int ADS1015_REG_CONFIG_PGA_6_144V = 0x0000; // +/-6.144V range
	public static int ADS1015_REG_CONFIG_PGA_4_096V = 0x0200; // +/-4.096V range
	public static int ADS1015_REG_CONFIG_PGA_2_048V = 0x0400; // +/-2.048V range
																// (default)
	public static int ADS1015_REG_CONFIG_PGA_1_024V = 0x0600; // +/-1.024V range
	public static int ADS1015_REG_CONFIG_PGA_0_512V = 0x0800; // +/-0.512V range
	public static int ADS1015_REG_CONFIG_PGA_0_256V = 0x0A00; // +/-0.256V range

	public static int ADS1015_REG_CONFIG_MODE_MASK = 0x0100;
	public static int ADS1015_REG_CONFIG_MODE_CONTIN = 0x0000; // Continuous
																// conversion
																// mode
	public static int ADS1015_REG_CONFIG_MODE_SINGLE = 0x0100; // Power-down
																// single-shot
																// mode
																// (default)

	public static int ADS1015_REG_CONFIG_DR_MASK = 0x00E0;
	public static int ADS1015_REG_CONFIG_DR_128SPS = 0x0000; // 128 samples per
																// second
	public static int ADS1015_REG_CONFIG_DR_250SPS = 0x0020; // 250 samples per
																// second
	public static int ADS1015_REG_CONFIG_DR_490SPS = 0x0040; // 490 samples per
																// second
	public static int ADS1015_REG_CONFIG_DR_920SPS = 0x0060; // 920 samples per
																// second
	public static int ADS1015_REG_CONFIG_DR_1600SPS = 0x0080; // 1600 samples
																// per second
																// (default)
	public static int ADS1015_REG_CONFIG_DR_2400SPS = 0x00A0; // 2400 samples
																// per second
	public static int ADS1015_REG_CONFIG_DR_3300SPS = 0x00C0; // 3300 samples
																// per second
																// (also 0x00E0)

	public static int ADS1115_REG_CONFIG_DR_8SPS = 0x0000; // 8 samples per
															// second
	public static int ADS1115_REG_CONFIG_DR_16SPS = 0x0020; // 16 samples per
															// second
	public static int ADS1115_REG_CONFIG_DR_32SPS = 0x0040; // 32 samples per
															// second
	public static int ADS1115_REG_CONFIG_DR_64SPS = 0x0060; // 64 samples per
															// second
	public static int ADS1115_REG_CONFIG_DR_128SPS = 0x0080; // 128 samples per
																// second
	public static int ADS1115_REG_CONFIG_DR_250SPS = 0x00A0; // 250 samples per
																// second
																// (default)
	public static int ADS1115_REG_CONFIG_DR_475SPS = 0x00C0; // 475 samples per
																// second
	public static int ADS1115_REG_CONFIG_DR_860SPS = 0x00E0; // 860 samples per
																// second

	public static int ADS1015_REG_CONFIG_CMODE_MASK = 0x0010;
	public static int ADS1015_REG_CONFIG_CMODE_TRAD = 0x0000; // Traditional
																// comparator
																// with
																// hysteresis
																// (default)
	public static int ADS1015_REG_CONFIG_CMODE_WINDOW = 0x0010; // Window
																// comparator

	public static int ADS1015_REG_CONFIG_CPOL_MASK = 0x0008;
	public static int ADS1015_REG_CONFIG_CPOL_ACTVLOW = 0x0000; // ALERT/RDY pin
																// is low when
																// active
																// (default)
	public static int ADS1015_REG_CONFIG_CPOL_ACTVHI = 0x0008; // ALERT/RDY pin
																// is high when
																// active

	public static int ADS1015_REG_CONFIG_CLAT_MASK = 0x0004; // Determines if
																// ALERT/RDY pin
																// latches once
																// asserted
	public static int ADS1015_REG_CONFIG_CLAT_NONLAT = 0x0000; // Non-latching
																// comparator
																// (default)
	public static int ADS1015_REG_CONFIG_CLAT_LATCH = 0x0004; // Latching
																// comparator

	public static int ADS1015_REG_CONFIG_CQUE_MASK = 0x0003;
	public static int ADS1015_REG_CONFIG_CQUE_1CONV = 0x0000; // Assert
																// ALERT/RDY
																// after one
																// conversions
	public static int ADS1015_REG_CONFIG_CQUE_2CONV = 0x0001; // Assert
																// ALERT/RDY
																// after two
																// conversions
	public static int ADS1015_REG_CONFIG_CQUE_4CONV = 0x0002; // Assert
																// ALERT/RDY
																// after four
																// conversions
	public static int ADS1015_REG_CONFIG_CQUE_NONE = 0x0003; // Disable the
																// comparator
																// and put
																// ALERT/RDY in
																// high state
																// (default)
}
