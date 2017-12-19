<?php
/*
	Author: Alona Rudchenko
	Description: This php script is executed on raspberry pi server with MySQL database.
	It fetches temperature and humidity values from database using parameters specified
	in URL - hours and max number of points. It then outputs the values in JSON format.
*/

// file with functions
include "./funct_valuesForJSON.php";


// URL request parameter hours - number of hours ago to start loading data from
$hours = $_REQUEST['hours'];
if (!isset($hours)){
	$hours = 0;
}

// URL request parameter maxpoints - maximum numer of points to load
$maxpoints = $_REQUEST['maxpts'];
if (!isset($maxpoints) || $maxpoints <= 0) {
	$maxpoints = 2000;
}

// connect to database using included file with settings
$db = connection_db();

// fetch data from database  for hours with maxpoints 
$sensordata = loadSensorData($hours, $maxpoints); 

// output database values in JSON format
giveValuesJSON($sensordata);


?>

