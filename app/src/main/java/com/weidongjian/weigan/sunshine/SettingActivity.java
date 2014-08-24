package com.weidongjian.weigan.sunshine;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Weigan on 2014/8/23.
 */
public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        System.out.println("onCreate SettingActivity");
//        Toast.makeText(this, "onCreate mainActivity", Toast.LENGTH_LONG).show();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
//            Nav
        return super.onOptionsItemSelected(item);
    }*/
}
