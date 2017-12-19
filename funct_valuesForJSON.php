<?php
/*
	Author: Alona Rudchenko
	Description: These functions serve to get temperature, humidity, and datetime data 
	from SQL database and output them in JSON format
*/


/*
	Description: Fetches temperature, humidity values with datetime from database
	for specified number of hours at maxpoints sampling frequency, returns an array
	of values.
	param $hours is number of hours for which to get data
	param $maxpoints is number of points of data to get
*/
function loadSensorData($hours=0, $maxpoints) {

	// query selects all entries if $hours not specified, or starting from X hours ago
	if($hours <= 0) {
		$sql = "SELECT UNIX_TIMESTAMP(dateandtime) AS timestamp, temperature, humidity FROM temperaturedata";
	} else {
		$sql = "SELECT UNIX_TIMESTAMP(dateandtime) AS timestamp, temperature, humidity FROM temperaturedata WHERE dateandtime >= (NOW() - INTERVAL $hours HOUR)";
	}

$temperatures = mysql_query($sql); // holds sql query
$points = mysql_num_rows($temperatures); // holds number of values 
$sampling = ceil($points / $maxpoints); // holds sampling frequency for data points

// create arrays to place values
$dateandtimearray = array();
$temparray = array();
$humidarray = array();


$pointindex = 0; // counter for data in database	
	
	// loop the data from database and sample points 
	while($temperature = mysql_fetch_assoc($temperatures)) {
		// data index corresponds to sampling frequency, fetch it
		if ($pointindex % $sampling == 0) {	
			$dateandtimearray[] = $temperature['timestamp'];
			$temparray[] = $temperature['temperature'];
			$humidarray[] = $temperature['humidity'];
		}
		// move on to next data point
		$pointindex++;
	}

$pointsloaded = count($temparray); // count of data sampled


$out = array();  // array containing fetched values
$out['dateandtime'] = $dateandtimearray;
$out['temperature'] = $temparray;
$out['humidity'] = $humidarray;

// return collected values
return $out;

}


/* 	
	Description: Helper function for giveValuesJSON. Constructs JSON format
	for a single value type.
	param $arraykey is the key for data in the array
   	param $isstring is true if data type is String
   	param $sensordata is data array returned from loadSensorData method
*/
function collectValuesForJSON($arraykey, $isstring, $sensordata) {

// construct key for JSON
echo " \"$arraykey\":[";

// 	loop through values for the key and place each in JSON format, separate with comas,
$rowindex = 0;

	foreach($sensordata[$arraykey] as $value) {
		if ($rowindex == 0) {
			if ($isstring) {
			// add "" for strings
			echo " \"".$value."\"";
			}
			else {
			echo " ".$value."";
			}
		}
		// add coma before value if it's not first one
		else {
			if ($isstring) {
			// add "" for strings
			echo ", \"".$value."\"";
			}
			else {
			echo ", ".$value."";
			}		
		}
		$rowindex++;
	}
echo "]"; // closing bracket for JSON
}


/* 
	Description: Combines JSON values for dateandtime, temperature, humidity into single JSON
	param $sensordata - data values returned from loadSensorData method
*/
function giveValuesJSON($sensordata) {

// JSON format: separate each series of keys with values for each data types and add brackets around
echo "{";

collectValuesForJSON('dateandtime',FALSE,$sensordata);
echo ",";

collectValuesForJSON('temperature',FALSE,$sensordata);
echo ",";

collectValuesForJSON('humidity',FALSE,$sensordata);

echo "}";

}


?>
