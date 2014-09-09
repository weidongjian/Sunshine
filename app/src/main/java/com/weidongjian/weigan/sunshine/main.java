package com.weidongjian.weigan.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class main extends Activity implements ForecastFragment.Callback {
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailFragment()).commit();
            }
        }else
            mTwoPane = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.weather_detail_container, detailFragment).commit();
        }else {
            Intent intent = new Intent(this, DetailActivity.class)
                            .putExtra(DetailActivity.DATE_KEY, date);
                    startActivity(intent);
        }
    }
}
