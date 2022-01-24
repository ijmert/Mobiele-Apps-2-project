package com.example.weatherapp;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

//settingsfragment to change some prefernces
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}