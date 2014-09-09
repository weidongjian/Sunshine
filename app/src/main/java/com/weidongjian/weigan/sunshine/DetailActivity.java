package com.weidongjian.weigan.sunshine;

import android.app.Activity;
import android.os.Bundle;

public class DetailActivity extends Activity{
    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            String date = getIntent().getStringExtra(DetailActivity.DATE_KEY);
            Bundle argement = new Bundle();
            argement.putString(DetailActivity.DATE_KEY, date);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(argement);
            getFragmentManager().beginTransaction().add(R.id.weather_detail_container, detailFragment).commit();
        }
    }
}
