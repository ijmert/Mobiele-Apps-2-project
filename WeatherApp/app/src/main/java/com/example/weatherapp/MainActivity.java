package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity {
    private static final String TAG = "mainactivity";



    private long LOCATION_REFRESH_TIME = 10000;
    private float LOCATION_REFRESH_DISTANCE = 5;
    private int NUM_PAGES = 5;
    private static final int FINE_LOC_CODE = 1;
    private static final int COARSE_LOC_CODE = 2;
    private static final int INTERNET_CODE = 3;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue queue;


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateFragment(location, pagerAdapter.fragments.get(0).getView(), false);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentLocationFragmentLastLocation();
    }

//    public void checkRelevantPermissionsssds (String permission, int requestcode)
//    {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestcode);
//        }
//    }

    public void checkPermission() {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOC_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOC_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_CODE);
        }
    }

    public void updateCurrentLocationFragmentLastLocation() {
//        checkRelevantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOC_CODE);
//        checkRelevantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOC_CODE);
//        checkRelevantPermissions(Manifest.permission.INTERNET, INTERNET_CODE);
        checkPermission();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        updateFragment(location, pagerAdapter.fragments.get(0).getView(), false);
                    }
                });
    }

    public void updateFragment(Location location,View fragmentView, boolean anyLocationFragment)
    {
        if (location != null) {
            Address address = getAddressFromLocation(location);
            if (address != null)
            {
                updateLocaleTextView(address, fragmentView.findViewById(R.id.localeTV));
                performAndHandleAPIcalls(location, fragmentView, anyLocationFragment);
            }
        }
    }

    public Address getAddressFromLocation(Location location)
    {
        Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0)
        {
            return addresses.get(0);
        }
        else
        {
            return null;
        }
    }

    public void updateLocaleTextView(Address address, TextView localeTV)
    {
        if (address.getLocality() != null)
        {
            localeTV.setText(address.getLocality());
        }
        else if (address.getAdminArea() != null)
        {
            localeTV.setText(address.getAdminArea());
        }
        else
        {
            localeTV.setText(address.getCountryName());
        }
    }

    public void performAndHandleAPIcalls(Location location, View fragmentView, boolean anyLocationFragment)
    {
        queue = Volley.newRequestQueue(MainActivity.this);
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.1f&lon=%.1f&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", (float)location.getLatitude(), (float)location.getLongitude());
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, response);
                        WeatherData wd = parseJsonToWeatherData(response);
                        updateViewWithWeatherData(fragmentView, wd, anyLocationFragment);
                        getWeatherForecastAndUpdateFragment(location, fragmentView);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                        return;
                }
        });
        queue.add(stringRequest);
    }


    public WeatherData parseJsonToWeatherData(String json)
    {
        try {

            JSONObject jsonObject = new JSONObject(json);
            WeatherData wd = new WeatherData();
            wd.temperature = (float)jsonObject.getJSONObject("main").getDouble("temp");
            wd.descriptor = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            wd.windDeg = jsonObject.getJSONObject("wind").getInt("deg");
//            wd.windGust = (float)jsonObject.getJSONObject("wind").getDouble("gust");
            wd.windSpeed = (float)jsonObject.getJSONObject("wind").getDouble("speed");
            String[] directions = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            Log.d("deg", wd.windDeg + "");
            double degrees = (double)wd.windDeg * 8/360;
            degrees = Math.round(degrees);
            degrees = (degrees + 8) % 8;
            wd.windDirection = directions[(int)degrees];
            return wd;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void updateViewWithWeatherData(View view, WeatherData wd, boolean anyLocationFragment)
    {
        TextView tv;
        if (!anyLocationFragment)
        {
            tv = (TextView)view.findViewById(R.id.WeatherDescriptor);
            tv.setText(wd.descriptor);
        }
        switch(wd.descriptor)
        {
            case "Clouds":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.cloudy);
                break;
            case "Thunderstorm":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.lightning);
                break;
            case "Rain":
            case "Drizzle":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.rainy);
                break;
            case "Snow":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.snow_icon);
                break;
            case "Clear":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.sunny);
                break;
            default:
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.rainy);
                break;
        }
        tv = (TextView)view.findViewById(R.id.TemperatureTV);
        tv.setText(String.format("%.1f °C", wd.temperature));
        tv = (TextView)view.findViewById(R.id.WindSpeedTV);
        tv.setText(String.format("Windspeed of %.1f km/h %s", wd.windSpeed, wd.windDirection));
//        tv = (TextView)view.findViewById(R.id.windGustTV);
//        tv.setText(String.format("Windgusts of %.1f km/h", wd.windGust));

    }

    public WeatherForecast getWeatherForecastAndUpdateFragment(Location location, View fragmentView)
    {
        queue = Volley.newRequestQueue(MainActivity.this);
        String url = String.format("https://api.openweathermap.org/data/2.5/onecall?lat=%.1f&lon=%.1f&exclude=hourly,minutely,current,alerts&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", (float)location.getLatitude(), (float)location.getLongitude());
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        WeatherForecast wf = parseJsonToWeatherForecast(response);
                        UpdateFragmentWithForecast(wf, fragmentView);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
                return;
            }
        });
        queue.add(stringRequest);
        return null;
    }


    public WeatherForecast parseJsonToWeatherForecast(String json)
    {
        try {
            JSONObject jsonObject = new JSONObject(json);
            WeatherForecast wf = new WeatherForecast();
            JSONArray daysForecast = jsonObject.getJSONArray("daily");
            String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            for (int i = 0; i<daysForecast.length(); i++)
            {
                JSONObject dayForecast = daysForecast.getJSONObject(i);
                int dt = dayForecast.getInt("dt");
                java.util.Date time = new java.util.Date((long)dt*1000);
                wf.days.add(days[time.getDay()]);
                wf.maxTemps.add((float)dayForecast.getJSONObject("temp").getDouble("max"));
                wf.minTemps.add((float)dayForecast.getJSONObject("temp").getDouble("min"));
                wf.descriptions.add(dayForecast.getJSONArray("weather").getJSONObject(0).getString("description"));
                wf.mainDescriptor.add(dayForecast.getJSONArray("weather").getJSONObject(0).getString("main"));
            }
            return wf;


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    public void UpdateFragmentWithForecast(WeatherForecast wf, View fragmentView)
    {
        int[] daysWidgetResources = new int[]{R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6, R.id.day7};
        for (int i = 0; i<7; i++)
        {
            View dayWidgetView = fragmentView.findViewById(daysWidgetResources[i]);
            ((TextView)dayWidgetView.findViewById(R.id.ForecastTitle)).setText(wf.days.get(i+1));
            ((TextView)dayWidgetView.findViewById(R.id.ForecastDescription)).setText(wf.mainDescriptor.get(i+1));
            switch (wf.mainDescriptor.get(i+1))
            {
                case "Thunderstorm":
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.thunderstorm_icon);
                    break;
                case "Drizzle":
                case "Rain":
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.rain_icon);
                    break;
                case "Snow":
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.snow_icon);
                    break;
                case "Clear":
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.clear_sky_icon);
                    break;
                case "Clouds":
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.scattered_clouds_icon);
                    break;
                default:
                    ((ImageView)dayWidgetView.findViewById(R.id.ForecastImage)).setImageResource(R.drawable.mist_icon);
                    break;
            }
            ((TextView)dayWidgetView.findViewById(R.id.ForecastMax)).setText(String.format("Max: %.1f °C", wf.maxTemps.get(i+1)));
        ((TextView)dayWidgetView.findViewById(R.id.ForecastMin)).setText(String.format("Max: %.1f °C", wf.minTemps.get(i+1)));
        }
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupLocationManager(); //this is for updates when location changed
        setupScreenSlidePager();
//        setupAutoCompleteView();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//        setupAutoCompleteView();
        return super.onCreateView(name, context, attrs);
    }

    public void setupLocationManager()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //idk what this does, but is probably needed
        checkPermission();
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    public void setupScreenSlidePager()
    {
        mPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pagerAdapter.add(new ScreenslideCurrentLocationFragment());
        pagerAdapter.add(new ScreenslideAnyLocationFragment());
        mPager.setAdapter(pagerAdapter);
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
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public final List<Fragment> fragments = new ArrayList<>();
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }



//        @Override
//        public Fragment createFragment(int position)
//        {
//            if (position == 0)
//            {
//                return new ScreenslideCurrentLocationFragment();
//            }
//            else
//            {
//                return new ScreenslideAnyLocationFragment();
//            }
//        }

        public void add(Fragment fragment)
        {
            fragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}