

package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



//This class is intended to update any given fragment with weather data: either current weather data or a weather forecast.
//The fragment to be updated is given as a parameter in most functions.
public class FragmentUpdater {

    //This is a queue used for API calls (using Volley).
    private static RequestQueue queue;

    private static final String TAG = "FragmentUpdater";

    //PARAMS: a latitude, a longitude, a view belonging to a particular fragment, a boolean to represent if this fragment is an 'anyLocationFragment' or a 'currentLocationFragment'
    //, a Context (the activity where the action is called from)
    //
    //This function will call other functions intended to update the fragmentView with current weather data and weather forecasts.
    public static void updateFragment(float latitude, float longitude, View fragmentView, boolean anyLocationFragment, Context context)
    {
        Address address = getAddressFromLatLon(latitude, longitude, context); //here we get an Address object from a latitude and longitude.
        if (address != null)
        {
            //the only difference in how the functions treat the AnyLocationFragment and the CurrentLocationFragment, the LocationTextView (localeTV) should be updated for the CurrentLocationFragment
//            it should not be updated for the AnyLocationFragment
            if (!anyLocationFragment)
            {
                updateLocaleTextView(address, fragmentView.findViewById(R.id.localeTV));
            }
            performAndHandleAPIcalls(latitude, longitude, fragmentView, context); //Here we go and handle the API calls and update the fragment further.
        }
    }

    //This function gets an Address object from the latitude and longitude, using Android Geocoder.
    public static Address getAddressFromLatLon(float latitude, float longitude, Context context)
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

    //This function updates a textView with information from an Address.
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

    //This function sets up the listeners to handle the API calls.
    public static void performAndHandleAPIcalls(float latitude, float longitude, View fragmentView, Context context)
    {
        queue = Volley.newRequestQueue(context);

        //here the url is formatted according to the latitude and longitude
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.1f&lon=%.1f&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", latitude, longitude);

        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d(TAG, response);

                        //The response from the API call will be in JSON format, and here I use a function to parse the JSON file into a custom class called "WeatherData"
                        WeatherData wd = parseJsonToWeatherData(response);

                        //Here the preferences are loaded so that we can have the appropriate units for our temperature and distance.
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
                        String TU = sPref.getString(context.getString(R.string.Temperature_Units_Key), "C");
                        String DU = sPref.getString(context.getString(R.string.Distance_Units_Key), "Km");

                        //Here we call a function that fills the view with the WeatherData wd.
                        updateViewWithWeatherData(fragmentView, wd, TU, DU);

                        //This function will handle another API call to get the weatherForecast and fill the fragment with the forecast
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
        queue.add(stringRequest); //We add the API request to the queue, this is how Volley works.
    }

    // A function that parses a JSON string into a WeatherData object, the JSON string is received by the api call to openweathermap.
    public static WeatherData parseJsonToWeatherData(String json)
    {
        try {

            JSONObject jsonObject = new JSONObject(json);
            WeatherData wd = new WeatherData();
            wd.temperature = (float)jsonObject.getJSONObject("main").getDouble("temp");
            wd.descriptor = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            wd.windDeg = jsonObject.getJSONObject("wind").getInt("deg");
            wd.windSpeed = (float)jsonObject.getJSONObject("wind").getDouble("speed");
            String[] directions = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            double degrees = (((double)wd.windDeg/22.5 + 1)%16)/2 - 0.5;
            degrees = Math.round(degrees);
            wd.windDirection = directions[(int)degrees];
            return wd;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    //Here we update the view with weather data.
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
        switch (TU) //handle the possible choices of units that the user might have selected.
        {
            case "C":
                tv.setText(String.format("%.1f °C", wd.temperature));
                break;
            case "F":
                float fahrenheit = Utils.ConvertFromCelsiusToFahrenheit(wd.temperature);
                tv.setText(String.format("%.1f °F", fahrenheit));
                break;
        }
        tv = (TextView)view.findViewById(R.id.WindSpeedTV); //handle the possible choices of units that the user might have selected.
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

    //This function sets up a listener for the WeatherForecast api call
    public static WeatherForecast getWeatherForecastAndUpdateFragment(float latitude, float longitude, View fragmentView, Context context)
    {
        String url = String.format("https://api.openweathermap.org/data/2.5/onecall?lat=%.1f&lon=%.1f&exclude=hourly,minutely,current,alerts&appid=cee2abc3b21e4cedaf1b3f0c464cc93f&units=metric", latitude, longitude);
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        WeatherForecast wf = parseJsonToWeatherForecast(response); //We parse the received JSON string to a custom class called WeatherForecast.
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context); //We get the preferences for the Temperate Unit.
                        String TU = sPref.getString(context.getString(R.string.Temperature_Units_Key), "C");
                        UpdateFragmentWithForecast(wf, fragmentView, TU); //We update the fragmentView with the forecast.

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

    //Parses the JSON string to a WeatherForecast object
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


    //updates fragmentview with a WeatherForecast object.
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
                    String bla = String.format("Max: %.1f °C", wf.maxTemps.get(i+1));
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMax)).setText(String.format("Max: %.1f °C", wf.maxTemps.get(i+1)));
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMin)).setText(String.format("Min: %.1f °C", wf.minTemps.get(i+1)));
                    break;
                case "F":
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMax)).setText(String.format("Max: %.1f °F", Utils.ConvertFromCelsiusToFahrenheit(wf.maxTemps.get(i+1))));
                    ((TextView)dayWidgetView.findViewById(R.id.ForecastMin)).setText(String.format("Min: %.1f °F", Utils.ConvertFromCelsiusToFahrenheit(wf.minTemps.get(i+1))));
                    break;
            }
        }
    }

}
