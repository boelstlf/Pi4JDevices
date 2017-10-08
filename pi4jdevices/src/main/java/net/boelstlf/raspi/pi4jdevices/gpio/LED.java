/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author boelstlf
 *
 */
public class LED {
	private Pin offsetLEDPin = RaspiPin.GPIO_04; // pin 16
	private Pin processingLEDPin = RaspiPin.GPIO_05; // pin 18
	private Pin idleLEDPin = RaspiPin.GPIO_06; // pin 22
	private GpioPinDigitalOutput processingLED;
	private GpioPinDigitalOutput idleLED;
	private GpioPinDigitalOutput offsetLED;
	private GpioController gpio = null;

	/**
	 * @param pin
	 */
	public LED (int pin)
	{
		init();
	}
	/**
	 * Initialize the Raspi and its connected sensors and actors.
	 */
	public void init() {

		System.out.println("initialize RaspberryPi CSV-EQ Emulator...");

		// create gpio instance
		gpio = GpioFactory.getInstance();

		System.out.println("set offset LED to HIGH");
		// instantiate offset LED and set to HIGH
		offsetLED = gpio.provisionDigitalOutputPin(offsetLEDPin, "offsetLED",
				PinState.HIGH);
		offsetLED.high();
		
		System.out.println("set processing LED to HIGH");
		// instantiate processing LED and set to HIGH
		processingLED = gpio.provisionDigitalOutputPin(processingLEDPin,
				"processingLED", PinState.HIGH);
		processingLED.high();

		System.out.println("set idle LED to HIGH");
		// instantiate idle LED and set to HIGH
		idleLED = gpio.provisionDigitalOutputPin(idleLEDPin, "idleLED",
				PinState.HIGH);
		idleLED.high();
	}
}
