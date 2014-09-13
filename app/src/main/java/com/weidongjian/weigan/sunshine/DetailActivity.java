package com.weidongjian.weigan.sunshine;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class DetailActivity extends Activity{
    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            String date = getIntent().getStringExtra(DetailActivity.DATE_KEY);
            Bundle argement = new Bundle();
            argement.putString(DetailActivity.DATE_KEY, date);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(argement);
            getFragmentManager().beginTransaction().add(R.id.weather_detail_container, detailFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
