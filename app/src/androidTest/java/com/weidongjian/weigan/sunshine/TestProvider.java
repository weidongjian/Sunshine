package com.weidongjian.weigan.sunshine;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.weidongjian.weigan.sunshine.Data.WeatherContract;

import java.util.Map;
import java.util.Set;

/**
 * Created by Weigan on 2014/8/31.
 */
public class TestProvider extends ApplicationTestCase<Application> {
    public static final String LOG_TAG = "ApplicationTest";
    public static final String TEST_LOCATION = "99705";
    public static final String TEST_DATE = "20141205";

    public TestProvider() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                WeatherContract.LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

//    public void testDeleteDb() throws Throwable {
//        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
//    }

    public void testInsertReadProvider() {



        // Create a new map of values, where column names are the keys
        ContentValues testValues = createNorthPoleLocationValues();
        long locationRowId;
        Uri locationUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, testValues);
        locationRowId = ContentUris.parseId(locationUri);
        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);
        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null);

        validateCursor(cursor, testValues);

        // If possible, move to the first row of the query results.
        ContentValues weatherValue = createWeatherValues(locationRowId);
        Uri insertUri = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValue);
//        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValue);
        long weatherRowId = ContentUris.parseId(insertUri);
//        assertTrue(weatherRowId != -1);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, null);
        if (weatherCursor.moveToFirst())
            validateCursor(weatherCursor, weatherValue);
        else
            fail("No weather data returned.");
        weatherCursor.close();

        addAllContentValues(weatherValue, testValues);

        weatherCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.buildWeatherLocation(TEST_LOCATION), null, null, null, null);
        if (weatherCursor.moveToFirst())
            validateCursor(weatherCursor, weatherValue);
        else
            fail("No weather data returned.");
        weatherCursor.close();

        weatherCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE), null, null, null, null);
        if (weatherCursor.moveToFirst())
            validateCursor(weatherCursor, weatherValue);
        else
            fail("No weather data returned.");
        weatherCursor.close();

        weatherCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE), null, null, null, null);
        if (weatherCursor.moveToFirst())
            validateCursor(weatherCursor, weatherValue);
        else
            fail("No weather data returned.");
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(WeatherContract.LocationEntry._ID, locationRowId);
        updatedValues.put(WeatherContract.LocationEntry.COLUMN_CITY, "Santa's Village");

        int count = mContext.getContentResolver().update(
                WeatherContract.LocationEntry.CONTENT_URI, updatedValues, WeatherContract.LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        validateCursor(cursor, updatedValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.COLUMN_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.COLUMN_CITY, "North Pole");
        testValues.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, 64.7488);
        testValues.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);
        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherContract.WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);
        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(WeatherContract.LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(WeatherContract.LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }
}
