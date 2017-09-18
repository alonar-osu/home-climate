/*
* Author: Alona Rudchenko
* Description: The app shows temperature and humidity values collected
* by a sensor configured with a raspberry pi.
* Library: GraphView library is used for displaying time-series data
* in a graph - http://www.android-graphview.org/
*/

package com.example.alion_000.weatherstation;

/**
 * This class serves for creating objects that represent a single
 * measurement from a sensor. Each measurement object combines: the time of
 * measurement (mTimeInMillisec), the temperature value (mTemperature), and
 * the humidity value (mHumidity).
 */
public class Measurement {

    // time in milliseconds
    private long mTimeInMillisec;

    // temperature value
    private double mTemperature;

    // humidity value
    private double mHumidity;

    // constructor
    public Measurement(long cTimeInMillisec, double cTemperature, double cHumidity) {
        mTimeInMillisec = cTimeInMillisec;
        mTemperature = cTemperature;
        mHumidity = cHumidity;
    }

    // getter methods
    public long getTimeInMillisec() {
        return mTimeInMillisec;
    }

    public double getTemperature() {
        return mTemperature;
    }

    public double getHumidity() {
        return mHumidity;
    }

}
