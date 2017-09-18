/*
* Author: Alona Rudchenko
* Description: The app shows temperature and humidity values collected
* by a sensor configured with a raspberry pi.
* Library: GraphView library is used for displaying time-series data
* in a graph - http://www.android-graphview.org/
*/

package com.example.alion_000.weatherstation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.text.DecimalFormat;

/**
 * Displays current temperature, humidity, date and time. Shows temperature values
 * over the last 24 hours. The values are shown on Cards within a scrollable screen
 */
public class MainActivity extends AppCompatActivity {

    // Tag for logging
    public static final String TAG = MainActivity.class.getSimpleName();

    // URL to query for measurements from raspberry pi
    private static final String REQUEST_URL = "http://192.168.0.17:8000/indexjson.php?hours=24&maxpts=2000";

    // measurements JSON response
    ArrayList<Measurement> measurementsArrayList;

    // graph of temperatures
    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();

    // layout for pull to refresh
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the View swipe to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        // accent color for swipe to refresh
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                // show the spinner
                mSwipeRefreshLayout.setRefreshing(true);

                // Start AsyncTask to do the Http request
                MeasurementAsyncTask task = new MeasurementAsyncTask();
                task.execute();
            }
        });

        // listener for swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                MeasurementAsyncTask task = new MeasurementAsyncTask();
                task.execute();
            }
        });
    }

    /**
     * AsyncTask performs an Http request in the background, then updates UI
     */
    private class MeasurementAsyncTask extends AsyncTask<URL, Void, ArrayList<Measurement>> {

        @Override
        protected ArrayList<Measurement> doInBackground(URL... urls) {
            // Create URL object
            URL url = HttpRequestUtils.createUrl(REQUEST_URL);

            // Do Http request to the URL, get JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = HttpRequestUtils.makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(TAG, "Problem getting JSON response");
            }

            // Extract information from JSON response, fill ArrayList with Measurement objects
            measurementsArrayList = HttpRequestUtils.extractValuesFromJson(jsonResponse);

            return measurementsArrayList;
        }

        /**
         * Updates screen with current measurements
         */
        @Override
        protected void onPostExecute(ArrayList<Measurement> measurementsArrayList) {
            if (measurementsArrayList == null) {
                return;
            }

            // Get graph values ready
            populateGraph();

            // show data in UI
            updateUI(measurementsArrayList);

            // stop the refresh spinner
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Adds data points to graph series
     */
    private void populateGraph() {

        for (int i = 0; i < measurementsArrayList.size() - 1; i++) {
            // measurement's time
            long x = measurementsArrayList.get(i).getTimeInMillisec();
            // measurement's temperature
            double t = measurementsArrayList.get(i).getTemperature();

            // add data point
            series.appendData(new DataPoint(x, t), true, measurementsArrayList.size() - 1);
        }
    }

    /**
     * Updates UI to display current measurement
     */
    private void updateUI(ArrayList<Measurement> measurementsArrayList) {
        // last measurement
        Measurement lastMeas = measurementsArrayList.get(measurementsArrayList.size() - 1);

        // Create a new Date object for the time of last measurement
        Date dateObject = new Date(lastMeas.getTimeInMillisec());

        // Find the TextView with view ID date
        TextView dateTextView = (TextView) findViewById(R.id.date);
        // Format the date string (e.g. June 21)
        String formattedDate = formatDate(dateObject);
        // Display the date in TextView
        dateTextView.setText(formattedDate);

        // Find the TextView with view ID time
        TextView timeTextView = (TextView) findViewById(R.id.time);
        // Format the time string (e.g. "2:05")
        String formattedTime = formatTime(dateObject);
        // Display the time in TextView
        timeTextView.setText(formattedTime);

        // Find the TextView with view ID temperature
        TextView temperatureTextView = (TextView) findViewById(R.id.temperature);
        // Format temperature to show 1 decimal place and degree sign ° C
        String formattedTemp = formatMeasure(lastMeas.getTemperature()) + "°C";
        // Display the temperature in TextView
        temperatureTextView.setText(formattedTemp);

        // Find the TextView with view ID humidity
        TextView humidityTextView = (TextView) findViewById(R.id.humidity);
        // Format humidity to show 1 decimal place and % sign
        String formattedHum = formatMeasure(lastMeas.getHumidity()) + " %";
        // Display the humidity in TextView
        humidityTextView.setText(formattedHum);

        // update graph with current temperatures
        updateGraph();
    }

    /**
     * Returns the formatted date String (e.g. "June 19") from a Date object
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd");
        return dateFormat.format(dateObject);
    }

    /**
     * Returns the formatted time String (e.g. "6:22") from a Date object
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
        return timeFormat.format(dateObject);
    }

    /**
     * Returns the formatted temperature or humidity String showing 1 decimal place
     */
    private String formatMeasure(double temphum) {
        DecimalFormat temphumFormat = new DecimalFormat("0.0");
        return temphumFormat.format(temphum);
    }

    /**
     * Updates the graph to display temperature values over the last 24 hours,
     * sets axis labels, the min and max of X axis
     */
    private void updateGraph() {
        // set current values to the Graph
        GraphView graph = (GraphView) findViewById(R.id.plot);
        graph.addSeries(series);

        // hour format for graph's X axis
        SimpleDateFormat hourDateFormat = new SimpleDateFormat("HH");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, hourDateFormat));

        // set X and Y axis names and text size
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Temperature");
        graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(65);
        graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(65);

        // padding around graph
        graph.getGridLabelRenderer().setPadding(55);

        // max and min values for graph's X axis
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(series.getLowestValueX());
        graph.getViewport().setMaxX(series.getHighestValueX() + 5);
    }

}
