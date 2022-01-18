package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;




public class FragmentUpdater {

    private static RequestQueue queue;

    private static final String TAG = "FragmentUpdater";
    public static void updateFragment(float latitude, float longitude, View fragmentView, boolean anyLocationFragment, Context context)
    {
        Address address = getAddressFromLocation(latitude, longitude, context);
        if (address != null)
        {
            if (!anyLocationFragment)
            {
                updateLocaleTextView(address, fragmentView.findViewById(R.id.localeTV));
            }
            performAndHandleAPIcalls(latitude, longitude, fragmentView, context);
        }
    }

    public static Address getAddressFromLocation(float latitude, float longitude, Context context)
    {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
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

    public static void updateLocaleTextView(Address address, TextView localeTV)
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

    public static void performAndHandleAPIcalls(float latitude, float longitude, View fragmentView, Context context)
    {
        queue = Volley.newRequestQueue(context);
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.1f&lon=%.1f&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", latitude, longitude);
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, response);
                        WeatherData wd = parseJsonToWeatherData(response);
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
                        String TU = sPref.getString(context.getString(R.string.Temperature_Units_Key), "C");
                        String DU = sPref.getString(context.getString(R.string.Distance_Units_Key), "Km");
                        updateViewWithWeatherData(fragmentView, wd, TU, DU);
                        getWeatherForecastAndUpdateFragment(latitude, longitude, fragmentView, context);
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

    public static WeatherData parseJsonToWeatherData(String json)
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

    public static void updateViewWithWeatherData(View view, WeatherData wd, String TU, String DU)
    {
        TextView tv;
        tv = (TextView)view.findViewById(R.id.WeatherDescriptor);
        tv.setText(wd.descriptor);
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
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.snow);
                break;
            case "Clear":
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.sunny);
                break;
            default:
                ((ImageView)view.findViewById(R.id.bgImage)).setImageResource(R.drawable.rainy);
                break;
        }
        tv = (TextView)view.findViewById(R.id.TemperatureTV);
        switch (TU)
        {
            case "C":
                tv.setText(String.format("%.1f °C", wd.temperature));
                break;
            case "F":
                float fahrenheit = Utils.ConvertFromCelsiusToFahrenheit(wd.temperature);
                tv.setText(String.format("%.1f °F", fahrenheit));
                break;
        }
        tv = (TextView)view.findViewById(R.id.WindSpeedTV);
        switch (DU)
        {
            case "Km":
                tv.setText(String.format("Windspeed of %.1f km/h %s", wd.windSpeed, wd.windDirection));
                break;
            case "Mi":
                float miles = Utils.ConvertFromKmToMiles(wd.windSpeed);
                tv.setText(String.format("Windspeed of %.1f mi./h %s", miles, wd.windDirection));
                break;
        }


    }

    public static WeatherForecast getWeatherForecastAndUpdateFragment(float latitude, float longitude, View fragmentView, Context context)
    {
        queue = Volley.newRequestQueue(context);
        String url = String.format("https://api.openweathermap.org/data/2.5/onecall?lat=%.1f&lon=%.1f&exclude=hourly,minutely,current,alerts&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", latitude, longitude);
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        WeatherForecast wf = parseJsonToWeatherForecast(response);
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
                        String TU = sPref.getString(context.getString(R.string.Temperature_Units_Key), "C");
                        UpdateFragmentWithForecast(wf, fragmentView, TU);

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

    public static WeatherForecast parseJsonToWeatherForecast(String json)
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



    public static void UpdateFragmentWithForecast(WeatherForecast wf, View fragmentView, String TU)
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
            switch (TU)
            {
                case "C":
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMax)).setText(String.format("Max: %.1f °C", wf.maxTemps.get(i+1)));
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMin)).setText(String.format("Max: %.1f °C", wf.minTemps.get(i+1)));
                    break;
                case "F":
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMax)).setText(String.format("Max: %.1f °F", Utils.ConvertFromCelsiusToFahrenheit(wf.maxTemps.get(i+1))));
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMin)).setText(String.format("Max: %.1f °F", Utils.ConvertFromCelsiusToFahrenheit(wf.minTemps.get(i+1))));
                    break;
            }
        }
    }

}
