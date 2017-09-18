/*
* Author: Alona Rudchenko
* Description: The app shows temperature and humidity values collected
* by a sensor configured with a raspberry pi.
* Library: GraphView library is used for displaying time-series data
* in a graph - http://www.android-graphview.org/
*/

package com.example.alion_000.weatherstation;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Contains utility methods to for Http request to create an URL, perform HttpRequest,
 * extract JSON response from stream, extract data from JSON
 */
public class HttpRequestUtils {

    // Tag for logging
    public static final String TAG = HttpRequestUtils.class.getSimpleName();

    public HttpRequestUtils() {
    }

    /**
     * Returns new URL object from string URL
     */
    protected static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(TAG, "Error creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Makes an HTTP request to URL and returns a String JSON response
     */
    protected static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // if URL is null, return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        // send a GET http request
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(20000); // milliseconds
            urlConnection.setConnectTimeout(20000); // milliseconds
            urlConnection.connect();

            // success, read response
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting Http response", e);
            return "";
        } finally {
            // done reading response
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Converts InputStream into a String which contains the JSON response
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        // building the string for JSON response
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // get each line of InputStream
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        // string with JSON response
        return output.toString();
    }

    /**
     * Extracts date, temperature, humidity for each measurement, combines them
     * into Measurement objects and returns an ArrayList of Measurements
     */
    protected static ArrayList<Measurement> extractValuesFromJson(String jsonResponse) {
        // ArrayList to contain Measurement objects
        ArrayList<Measurement> measurements = new ArrayList<>();

        Log.e(TAG, "Response from url: " + jsonResponse);

        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        try {
            // set up JSON Arrays
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray datetimes = root.getJSONArray("dateandtime");
            JSONArray temperatures = root.getJSONArray("temperature");
            JSONArray humidities = root.getJSONArray("humidity");

            // for each measure, obtain values from JSON Arrays
            if (datetimes.length() > 0) {
                for (int i = 0; i < datetimes.length(); i++) {
                    // extract values for date, temp, humidity from each array
                    long time = datetimes.getLong(i) * 1000;
                    double temp = temperatures.getDouble(i);
                    double hum = humidities.getDouble(i);

                    // combine time, temp, hum into a Measurement object
                    Measurement measure = new Measurement(time, temp, hum);
                    // add the object to the ArrayList
                    measurements.add(measure);
                }

                // ArrayList of Measurement objects
                return measurements;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Problem parsing values from JSON");
        }
        return null;
    }

}
