package com.weidongjian.weigan.sunshine.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.weidongjian.weigan.sunshine.Data.WeatherContract;
import com.weidongjian.weigan.sunshine.R;
import com.weidongjian.weigan.sunshine.Utility;
import com.weidongjian.weigan.sunshine.main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Weigan on 2014/9/10.
 */
public class SunshineService extends IntentService {
    private static final String LOG_TAG = SunshineService.class.getSimpleName();
    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
// If there's no zip code, there's nothing to look up.  Verify size of params.
//        if (params.length == 0) {
//            return;
//        }

        String locationQuery = Utility.getPreferredLocation(this);
//        String locationQuery = Utility.getSelectedLocation(this);
        System.out.println("locationQuery" + locationQuery);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "id";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getWeatherDataFromJson(forecastJsonStr, numDays, locationQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // Data is fetched in Celsius by default.
        // If user prefers to see in Fahrenheit, convert the values here.
        // We do this rather than fetching in Fahrenheit so that the user can
        // change this option without us having to re-fetch the data once
        // we start storing the values in a database.
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String unitType = sharedPrefs.getString(
                this.getString(R.string.pref_units_key),
                this.getString(R.string.pref_units_metric));

        if (unitType.equals(this.getString(R.string.pref_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(this.getString(R.string.pref_units_metric))) {
            Log.d(LOG_TAG, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private void getWeatherDataFromJson(String forecastJsonStr, int numDays,
                                            String locationSetting)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
        double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

        Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude + " " + cityLongitude);

        long locationID = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);
//        System.out.println("Location setting is " + locationSetting + ". LocationID is " + locationID);
        // Insert the location into the database.
        // The function referenced here is not yet implemented, so we've commented it out for now.
//        long locationID = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);
            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
//            System.out.println("weatherId:" + weatherId);
//            System.out.println("description:" + description);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);

            highAndLow = formatHighLows(high, low);
//            String day = getReadableDateString(dateTime);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int rowsInserted =  this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " rows of weather data");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
            getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + "<= ?",
                    new String[]{yesterdayDate});
            }
        }

    private long addLocation(String locationSetting, String cityName, double lat, double lon) {
        Log.v(LOG_TAG, "inserting " + cityName + ", with coord: " + lat + ", " + lon);

        // First, check if the location with this city name exists in the db
        Cursor cursor = this.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_SETTING + " = ?",
                new String[]{locationSetting},
                null);
        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int locationIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return cursor.getLong(locationIndex);
        } else {
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, lon);

            Uri locationInsertUri = this.getContentResolver()
                    .insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        public AlarmReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive start");
            Intent intent1 = new Intent(context, SunshineService.class);
            context.startService(intent1);
            notifyWeather(context);
        }
    }

    private static void notifyWeather(Context mContext) {
        Context context = mContext;
//        System.out.println("notifyWeather start");
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        long lastSync = prefs.getLong(lastNotificationKey, 0);

//        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS)
        if (displayNotifications)
        {
//            System.out.println("call the noti meathed");
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);

                // Define the text of the forecast.
                boolean isMetric = Utility.isMetric(context);
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Utility.formatTemperature(high, isMetric),
                        Utility.formatTemperature(low, isMetric));

                //build your notification here.
                NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(context);
                mNotificationBuilder.setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText);

                Intent intent = new Intent(context, main.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(intent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotificationBuilder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
                notificationManager.notify(WEATHER_NOTIFICATION_ID, mNotificationBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }

    }

}
