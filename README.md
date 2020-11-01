# Pi4JDevices
Library of several sensors/actors for RaspberryPi using Pi4J framework.

Following devices (sensors & actors) are included currently

I2C - Devices
  * VCNL4000    (proximity sensor)
  * ADS10x15    (4-channel analog -> digital converter)
  * ADXL345     (3-axis accelerometer with 13-bit resolution) 
  * MPU-6050    (3-axis accelerometer and 3-axis gyro 16-bit sensor)
  * MPL115A2    (Pressure/Temperature Sensor)
  * SSD1306     (OLED display)
  * LCD         (2x20 character LCD display)
  * LED Matrix  (8x8 LED matrix)
  
UART (serial connection)
  * RFID        (RFID reading based on serial communication)
  
  
#### Pre-Requiste
##### Determine your RaspberryPi model and Pin layout
In order to use the correct pin layout and mapping determine the hardware model first.
Open a terminal on the raspi and enter
> cat /sys/firmware/devicetree/base/model

The output will look something like this
> Raspberry Pi 3 Model B Rev 1.2

### Setup steps after raw image

### Pin Layout
As identified above I am running this code on a Raspi model 3, thus the following pin layout applies
https://projects.drogon.net/raspberry-pi/wiringpi/pins/
