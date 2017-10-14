/**
 * 
 */
package net.boelstlf.raspi.pi4jdevices.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * @author boelstlf
 *
 */
public class Switch {
	//
	// refer to <b>https://projects.drogon.net/raspberry-pi/wiringpi/pins/</b> for pin <-> GPIO mapping
	//
	private Pin materialTriggerPin = RaspiPin.GPIO_00; // pin 11
	private Pin processingLEDPin = RaspiPin.GPIO_05; // pin 18
	private Pin idleLEDPin = RaspiPin.GPIO_06; // pin 22
	private GpioPinDigitalOutput processingLED;
	private GpioPinDigitalOutput IdleLED;
	private GpioController gpio = null;

	/**
	 * Initialize the Raspi and its connected sensors and actors.
	 */
	public void init() {

		System.out.println("initialize RaspberryPi Switch Test...");
		
		// create gpio instance
		gpio = GpioFactory.getInstance();

		// provision GPIO pin as an input pin with its internal pull down
		// resistor enabled
		final GpioPinDigitalInput materialTrigger = gpio
				.provisionDigitalInputPin(materialTriggerPin,
						PinPullResistance.PULL_UP);
		
		System.out.println("Listening for trigger on GPIO: "
				+ materialTriggerPin + " (state = "
				+ materialTrigger.getState() + ")");

		// create and register gpio pin listener
		materialTrigger.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				// display pin state on console
				System.out.println(" --> GPIO PIN STATE CHANGE: "
						+ event.getPin() + " = " + event.getState());
				if (event.getState() == PinState.LOW) {
					setIdle();
				} else {
					setProcessing();
				}
			}

		});

		// instantiate processing LED and set to LOW
		processingLED = gpio.provisionDigitalOutputPin(processingLEDPin,
				"processingLED", PinState.LOW);

		// instantiate idle LED and set to HIGH
		IdleLED = gpio.provisionDigitalOutputPin(idleLEDPin, "idleLED",
				PinState.HIGH);

	}
	
	private void setIdle()
	{
		processingLED.low();
		IdleLED.high();
	
	}

	private void setProcessing()
	{
		processingLED.high();
		IdleLED.low();

	}
}
