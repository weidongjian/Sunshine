package com.weidongjian.weigan.sunshine;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Weigan on 2014/8/23.
 */
public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perf_general);
//        System.out.println("onCreate PrefFragment");
    }
}
