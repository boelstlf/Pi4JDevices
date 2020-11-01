# Pi4JDevices Library
Java Library to use several sensors/actors for RaspberryPi based on [Pi4J framework](https://pi4j.com/1.2/index.html).

Following devices (sensors & actors) are included currently
* I2C - Devices
  * VCNL4000    (proximity sensor)
  * ADS10x15    (4-channel analog -> digital converter)
  * ADXL345     (3-axis accelerometer with 13-bit resolution) 
  * MPU-6050    (3-axis accelerometer and 3-axis gyro 16-bit sensor)
  * MPL115A2    (Pressure/Temperature Sensor)
  * SSD1306     (OLED display)
  * LCD         (2x20 character LCD display)
  * LED Matrix  (8x8 LED matrix)
* UART (serial connection)
  * RFID        (RFID reading based on serial communication)
* GPIO
  * Turn on/off LED (pin-number)
  * Switch      (input pin-number)
  
### Pre-Requiste
#### Determine your RaspberryPi model and Pin layout
In order to use the correct pin layout and mapping determine the hardware model first.
Open a terminal on the raspi and enter
> cat /sys/firmware/devicetree/base/model

The output will look something like this
> Raspberry Pi 3 Model B Rev 1.2

Refer to (https://pinout.xyz/) to get the corresponding pin layout.

### Pin Layout
An direct way way is to install the support library [GPIO-Zero](https://gpiozero.readthedocs.io/en/stable/installing.html)
and run the command
> pinout
which will return the hardware model and the current pin states and layout in one step
<img src="./resources/pinlayout_sample.png" alt="gpiozero pin layout result" width="250"/>

## Setup steps after raw image


