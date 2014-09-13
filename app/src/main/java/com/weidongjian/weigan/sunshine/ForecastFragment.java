package com.weidongjian.weigan.sunshine;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.weidongjian.weigan.sunshine.Data.WeatherContract;
import com.weidongjian.weigan.sunshine.service.SunshineService;

import java.util.Date;

/**
 * Created by Weigan on 2014/8/18.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
//    public SimpleCursorAdapter mForecastAdapter;
    private ForecastAdapter mForecastAdapter;
    private static final int FORECAST_LOADER = 0;
    private static final String POSITION_KEY = "position";
    private String mLocation;
    private int mPosition;
    ListView listView;

    private static final String[] TODAY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    private static final String[] FORECAST_COLUMNS = {WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_SETTING};

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_ID = 5;

    String[] result = new String[7];

    public ForecastFragment() {
    }

    public interface Callback {
        public void onItemSelected(String date);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY))
            mPosition = savedInstanceState.getInt(POSITION_KEY);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
//                    startActivity(intent);
                    ((Callback)getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
                mPosition = position;
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }else {
            checkWeatherUpdate();
        }
    }

    private void checkWeatherUpdate() {
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, WeatherContract.getDbDateString(new Date()));
        Cursor cursor = getActivity().getContentResolver().query(uri, TODAY_WEATHER_PROJECTION, null, null, null);
        if (!cursor.moveToFirst()) {
            updateWeather();
        }
    }

    private void updateWeather() {
//        String location = Utility.getPreferredLocation(getActivity());
//        new FetchWeatherTask(getActivity()).execute(location);
//        Intent intent = new Intent(getActivity(), SunshineService.class);
//        getActivity().startService(intent);

        AlarmManager alarmManager;
        PendingIntent pendingIntent;
//        System.out.println("alarmManager about to start");

        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
        1000, pendingIntent);
//        System.out.println("alarmManager start");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
//            notifyWeather(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDbDateString(new Date());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
//        if (mPosition != ListView.INVALID_POSITION)
//            listView.setSelection(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

}