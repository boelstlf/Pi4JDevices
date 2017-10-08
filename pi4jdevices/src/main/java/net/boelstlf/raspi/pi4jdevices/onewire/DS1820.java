/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.onewire;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author boelstlf
 *
 */
public class DS1820 {

	private static final String DEVICEDIR = "/sys/devices/w1_bus_master1/";
	private static final String SLAVELIST = "w1_master_slaves";
	private static final String SLAVEVALUEFILE = "w1_slave";

	/**
	 * @param deviceDir
	 * @param slaveListFileName
	 * @return
	 */
	public List<String> getConnectedSlaves() {
		System.out.println("Retrieve list of w1-slaves connected to system");
		List<String> slaves = new ArrayList<String>();
		FileReader slaveListFile;
		BufferedReader br = null;
		String row = "";

		try {
			// read all available w1 slaves from system file
			slaveListFile = new FileReader(DS1820.DEVICEDIR + DS1820.SLAVELIST);
			br = new BufferedReader(slaveListFile);
			while ((row = br.readLine()) != null) {
				slaves.add(row);
				System.out.println("Identified SlaveID: " + row);
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return slaves;
	}

	/**
	 * @param deviceDir
	 * @param slave
	 * @return
	 */
	public float getTempValue(String slave) {
		System.out.print("Retrieve temp value from slaveID: " + slave);
		BufferedReader br = null;
		String row = "";
		float temp = 0.0f;

		String slaveFile = DS1820.DEVICEDIR + slave + "/" + DS1820.SLAVEVALUEFILE;

		try {
			FileReader slaveValue = new FileReader(slaveFile);
			br = new BufferedReader(slaveValue);
			row = br.readLine();
			row = br.readLine();
			temp = Float.parseFloat(row.substring(row.indexOf("t=") + 2)) / 1000;
			System.out.println(" current temp: " + temp + " C");
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;

	}

}
