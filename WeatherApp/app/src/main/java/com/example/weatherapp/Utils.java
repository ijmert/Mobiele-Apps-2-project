package com.example.weatherapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//class containing static methods for some functionality that was needed in my app
public class Utils {


    static float ConvertFromCelsiusToFahrenheit(float celsius)
    {
        return (celsius * 9 / 5 + 32);
    }

    static float ConvertFromKmToMiles(float km)
    {
        return (float)(km / 1.6);
    }


    //a function that gets a json string from a json file.
    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    //a function that gets all cities from a json file and parses them to the City class.
    static List<String> GetCities(Context context)
    {
        String jsonString = Utils.getJsonFromAssets(context, "cities.json");
        Gson gson = new Gson();
        Type cityListType = new TypeToken<List<City>>(){}.getType();
        List<City> cities = gson.fromJson(jsonString, cityListType);
        List<String> cityNameList = new ArrayList<>();
        for (City city : cities){
            cityNameList.add(city.name);
        }
        return cityNameList;
    }


    //a function that gets the latitude and longitude from a city name.
    static float[] getLatLonFromCity(String cityname, Context context)
    {
        Geocoder gc = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gc.getFromLocationName(cityname, 1);
            return new float[] {(float)addresses.get(0).getLatitude(),(float) addresses.get(0).getLongitude()};
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}


