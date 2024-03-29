package com.weidongjian.weigan.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.weidongjian.weigan.sunshine.Data.WeatherContract;

/**
 * Created by Weigan on 2014/9/5.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_LAYOUT_TODAY = 0;
    private static final int VIEW_LAYOUT_FUTURE_DAY = 1;

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_LAYOUT_TODAY : VIEW_LAYOUT_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public ForecastAdapter(Context context, Cursor cursor, int flag) {
        super(context, cursor, flag);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutID = -1;
        if (viewType == VIEW_LAYOUT_TODAY)
            layoutID = R.layout.list_item_forecast_today;
        else if (viewType == VIEW_LAYOUT_FUTURE_DAY)
            layoutID = R.layout.list_item_forecast;

        View view = LayoutInflater.from(context).inflate(layoutID, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();
        int viewType = getItemViewType(cursor.getPosition());
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        if (viewType == VIEW_LAYOUT_TODAY) {
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            Uri uri = WeatherContract.LocationEntry.CONTENT_URI;
            Cursor cursor1 = context.getContentResolver().query(uri, null, WeatherContract.LocationEntry.COLUMN_SETTING  + "= ?",
                    new String[]{Utility.getPreferredLocation(context)}, null);
            cursor1.moveToFirst();
            String location = cursor1.getString(cursor1.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY));
            ((TextView)view.findViewById(R.id.list_item_city_name)).setText(location);
        }
        else
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high, isMetric));
        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}
