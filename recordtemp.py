# Author: Alona Rudchenko
# Description: This script runs on raspberry pi and collects 
# temperature and humidity values from its DHT sensor. 
# The values are logged to MySQL database table "temperaturedata".

import Adafruit_DHT as dht
import datetime
import time
import MySQLdb
import sys

# 30 sec wait
time.sleep(30)

# connect to MySQL database
db = MySQLdb
db = MySQLdb.connect("localhost", "logger", "raspas", "temperatures")
curs = db.cursor()


if len(sys.argv) > 1:
	log = sys.argv[1] == 'log'
else:
	log = False

# keep reading humidity, temp values from sensor
while True: 

	hum,tempC = dht.read_retry(dht.DHT22, 4)
		
	# retry in 10 sec if no reading
	if (hum is None or tempC is None):
		time.sleep(10)
		continue

	tempF=9.0/5.0 * tempC +32 # temp in Fahrenheit
	
	# output values logged
	if log: 
		print 'Temp={0:0.1f}F Humidity={1:0.1f}%'.format(tempF,hum)
		print 'Temp={0:0.1f}C'.format(tempC)
		print datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
		print

	# set up query for database
	query = "INSERT INTO temperaturedata values(NOW(), null, %s, %s)"
	args = (tempC, hum)

	# insert values into database
	with db:
		curs.execute(query, args)
		db.commit()
		
		if log:		
			print "values logged to temperatures database"	
	
	# 10 sec wait
	time.sleep(10)	
	

