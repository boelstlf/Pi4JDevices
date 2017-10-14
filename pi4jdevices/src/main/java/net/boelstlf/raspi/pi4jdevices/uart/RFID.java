/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.uart;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author boelstlf
 *
 */
public class RFID extends Thread {
	
	private boolean reading = true;
	// reading interval default = 100ms
	private int interval = 100;

	// - Get an instance of Serial for COM interaction
	private final Serial serial = SerialFactory.createInstance();

	/**
	 * 
	 */
	public RFID() {
		System.out.println("Serial Communication Example ... started.");
		System.out.println(" ... connect using settings: 9600, N, 8, 1.");

		// - Create and add a SerialDataListener
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				System.out.println(" ... data received");
				// - Get byte array from SerialDataEvent
				byte[] buffer = null;
				try {
					buffer = event.getBytes();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(" ... length" + buffer.length);
				byte[] data = Arrays.copyOfRange(buffer, 1, buffer.length - 3);
				String ID = "";

				// get rid of start byte (0x02) and stop byte (0x03) and the
				// checksum 2xbyte before the stop byte

				// - Iterate byte array print a readable representation of each
				// byte
				for (int i = 0; i < data.length; i++) {
					ID += (char) data[i];
				}

				// - Line break to represent end of data for this event
				System.out.println();
				System.out.println("----------------------------------------------");
				System.out.println("Tag: " + ID.substring(0, 4));
				System.out.println("ID: " + ID.substring(4, ID.length()));
				System.out.println("ID: " + Long.parseLong(ID.substring(4, ID.length()), 16));
				System.out.println("----------------------------------------------");
				System.out.println();
			}
		});
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static String bytesToStringUTFCustom(byte[] bytes) {
		char[] buffer = new char[bytes.length >> 1];
		for (int i = 0; i < buffer.length; i++) {
			int bpos = i << 1;
			char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
			buffer[i] = c;
		}
		return new String(buffer);
	}
	
	/**
	 * Stop continues reading
	 */
	public void stopReading()
	{
		reading = false;
	}

	/**
	 * 
	 */
	public void run() {
		System.out.println("start reading...");

		try {
			// open the default serial port provided on the GPIO header
			// - Attempt to open the COM port
			serial.open("/dev/ttyAMA0", 9600);

			while(reading)
			{		// - When you are done, ensure you close the port
			// To demonstrate, I am waiting X seconds and then closing the
			// port.
			Thread.sleep(interval);
			}
			
			// - Close port
			serial.close();
			System.out.println("COM port closed.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {

		}
	}
}
