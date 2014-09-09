package com.weidongjian.weigan.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.weidongjian.weigan.sunshine.Data.WeatherContract;

/**
 * Created by Weigan on 2014/9/7.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECASE_SHARE_HASHTAG = " #SunshineApp";
    public static final String LOCATION_KEY = "location";
    private static final int DETAIL_LOADER = 0;
    public static final String DATE_KEY = "forecast_date";
    public static final String extra = "extra";

    private String mForecastStr;
    private String mLocation;
    private ImageView mIconView;
    private TextView mDateView;
    private TextView mFriendlyDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
//        Intent intent = getActivity().getIntent();
        Bundle argement = getArguments();
        if (argement != null && argement.containsKey(DetailActivity.DATE_KEY))
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mFriendlyDateView = (TextView)rootView.findViewById(R.id.detail_day_textview);
        mDateView = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mHighTempView = (TextView)rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView)rootView.findViewById(R.id.detail_low_textview);
        mIconView = (ImageView)rootView.findViewById(R.id.detail_icon);
        mDescriptionView = (TextView)rootView.findViewById(R.id.detail_forecast_textview);
        mHumidityView = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView)rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView)rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_KEY, mLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Intent intent = getActivity().getIntent();
        Bundle argement = getArguments();
        if (argement != null && argement.containsKey(DetailActivity.DATE_KEY) && mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
        shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        shareActionProvider.setShareIntent(getShareIntent());
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, extra + FORECASE_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Intent intent = getActivity().getIntent();
//        if (intent == null || !intent.hasExtra(DATE_KEY)) {
//            return null;
//        }
        System.out.println("onCreateLoader: ");
        String dateString = getArguments().getString(DetailActivity.DATE_KEY);
        System.out.println("dateString: " + dateString );
        if (dateString == null)
            return null;

        String[] columns = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.LocationEntry.COLUMN_SETTING,
        };
        mLocation = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, dateString);

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                columns,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
        System.out.println("weatherId" + weatherId);
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
        String friendlyDateText = Utility.getDayName(getActivity(),date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        mFriendlyDateView.setText(friendlyDateText);
        mDateView.setText(dateText);

        String weatherDescription =
                data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
        mDescriptionView.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
        String low = Utility.formatTemperature(
                data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
        mHighTempView.setText(high);
        mLowTempView.setText(low);

        float windSpeedStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
        float windDirStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
        mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

        float pressureStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressureStr));

        float humilityStr = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
        mHumidityView.setText(getActivity().getString(R.string.format_humidity, humilityStr));

        mForecastStr = String.format("%s - %s - %s/%s",
                dateText, weatherDescription, high, low);

        Log.v(LOG_TAG, "Forecast String: " + mForecastStr);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null,this);
    }
}
