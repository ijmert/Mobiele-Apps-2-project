package com.example.weatherapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity {

    private int NUM_PAGES = 5;
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String jsonString = Utils.getJsonFromAssets(getApplicationContext(), "cities.json");
        Gson gson = new Gson();
        Type cityListType = new TypeToken<List<City>>(){}.getType();
        List<City> cities = gson.fromJson(jsonString, cityListType);
        List<String> cityNameList = new ArrayList<String>();
        for (City city : cities){
            cityNameList.add(city.name);
        }

        mPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        mPager.setAdapter(pagerAdapter);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item,cityNameList);
        AutoCompleteTextView actv =  (AutoCompleteTextView)findViewById(R.id.actvCreatePage);
        actv.setThreshold(1);
        actv.setAdapter(adapter);
        actv.setTextColor(Color.RED);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return new create_new_page_fragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}