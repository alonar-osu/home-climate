# Home Climate
Home Climate shows you current temperature and humidity in your home, and shows you a graph of temperature in the last 24 hrs. To set it up, you need a Raspberry Pi with a sensor to collect measures and an android phone to run the app for viewing. 
## What's Needed
* Raspberry Pi microcontroller
* DHT22 temperature-humidity sensor
* Android phone or tablet
* Wi-Fi connection

## Setup Raspberry Pi

##### 1. Connecting the DHT22 Sensor
Connect the DHT22 sensor to Raspberry Pi's GPIO header pins:
- connect sensor's **GND** to any Ground pin (e.g. GPIO #6) on Raspberry Pi
- conect sensor's **OUT** to GPIO #7 on Raspberry Pi
- connect sensor's **UCC** to GPIO #1 on Raspberry Pi

##### 2. Setting up MySQL Database
- install MySQL
- set up credentials
- create a table `temperaturedata` with columns `dateandtime`, `temperature`, `humidity`

##### 3. Python Script to Collect Sensor Data to the Database
- use **recordtemp.py** from this repository
- modify **recordtemp.py** to use your database credentials:
```
db = MySQLdb.connect("localhost", "[user]", "[password]", "[database]")
```
- set up **recordtemp.py** to run continuously to collect sensor values into database
This can be done with a short shell script such as:
```
python /home/pi/recordtemp.py &
```
##### 4. PHP Script to Get Data from Database in JSON format
- install Apache to run the php script
- use **indexjson.php** and **funct_valuesForJSON.php** from this repository
- place these files in `/var/www/html` directory

## Android App
##### 1. Android Studio Project Files
- use **MainActivity.java**, **Measurement.java**, **HttpRequestUtils.java** from this repository
- add these files to an Android Studio project
 

##### 2. Adding GraphView Library
- download [GraphView Library](http://www.android-graphview.org/download-getting-started/) for app's temperature graph
- include it in the Android Studio project

##### 3. Installing the app
- connect an android phone via ADB and install the Home Climate app

## How to Use
Keep the Raspberry Pi server running, it will be collecting temperature and humidity values. Launch the Home Climate app on an Android phone - it will show current time, date, temperature and humidity. You will also see a graph of temperature values over the last 24 hours. To view JSON values directly, go to `[Raspberry Pi IP : port]/indexjson.php` on your 

## Limitations 
The phone and the Raspberry Pi have to be connected to the same Wi-Fi to see current data in the app.

## License
MIT License
Copyright (c) 2017 Alona Rudchenko
