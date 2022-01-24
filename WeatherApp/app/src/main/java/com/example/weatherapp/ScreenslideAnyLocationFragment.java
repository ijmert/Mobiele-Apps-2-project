package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//The goal of this fragment is to allow the user to fill in any city, and get the weather information from that city. I use an autocomplete textview for this.

public class ScreenslideAnyLocationFragment extends Fragment {
    
    String TAG = "AnyLocFrag";
    String CityName = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //We set the view to the appropriate layout file.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.any_location_fragment, container, false);
        return rootView;
    }

    //When the view is created we load the sharedPreferences and check if there was a cityname previously entered into the fragment's autocomplete textview and saved in the preferences.
    //There will always be a stored value in the preferences except for if the app is started the first time on a new device.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAutoCompleteView();
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CityName = sPref.getString(getString(R.string.Any_Location_CityName_Key), "");
        if (CityName != "")
        {
            FillFragmentFromCityName(CityName); //this will fill the fragment with
            Log.d(TAG, "onViewCreated: view created with cityname from preferences");
        }
        Log.d(TAG, "onViewCreated: view created with actv");
    }

    //We fill the fragment from any given cityname.
    public void FillFragmentFromCityName(String CityName)
    {
        View v = getView();
        AutoCompleteTextView actv =  (AutoCompleteTextView)v.findViewById(R.id.localeTV);

        actv.setText(CityName); //Here we set the text of the autocomplete textview to the given city, this is needed because the user can fill in only a part of the cityname and press enter
        //to let the app autocomplete the rest, which would leave the autocomplete textview only partly filled in.

        float[] latlon = Utils.getLatLonFromCity(CityName, getActivity());//function to get the latitude and longitude from a given city name
        FragmentUpdater.updateFragment(latlon[0], latlon[1], getView(), true, getActivity());//update the fragment using the fragmentupdater class with the previously acquired latitude and longitude
        v = (View)(getView().findViewById(R.id.weatherContainer));
        v.setVisibility(View.VISIBLE);//we set the weathercontainer to visible, it was invisible before because it was not yet filled with weather information.

        //these two lines are intended to get rid of the keyboard after the autocomplete has occured.
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        storeValues(PreferenceManager.getDefaultSharedPreferences(getActivity())); //we store the current city in our preferences
    }



    private void setupAutoCompleteView()
    {
        View v = this.getView();

        AutoCompleteTextView actv =  (AutoCompleteTextView)v.findViewById(R.id.localeTV);

        //get a list of citynames to populate the autcomplete textview suggestions. These citynames were gotten from the internet as a JSON file.
        List<String> cityNameList = Utils.GetCities(getActivity()); //I use a custom function for this, see the Utils class for more information on how it works.

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.select_dialog_item, cityNameList);//Make the arrayadapter and populate it with the citynames array.
        actv.setAdapter(adapter);
        actv.setThreshold(1); //after 1 character is written we will show suggestions

        //Define the method to be executed when an item is clicked in the autocomplete textview.
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityName = (String) parent.getItemAtPosition(position);//get the cityname that was clicked.
                FillFragmentFromCityName(CityName); //fill the fragment with weatherdata from the city that was clicked.
                actv.clearFocus();
            }
        });

        //Define the method to be executed when enter is pressed in the autocompletetextview.
        actv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                CityName = (String)adapter.getItem(0);//get the city on position one in the autocomplete textview suggestions, this is the most likely to be the correct cityname.
                FillFragmentFromCityName(CityName);//fill the fragment with data for this city.
                actv.clearFocus();
                return true;
            }
        });
    }


    //We store the current cityname in the preferences
    public void storeValues(SharedPreferences sPref)
    {
        if (CityName != "")
        {
            SharedPreferences.Editor editor = sPref.edit();
            editor.putString(getString(R.string.Any_Location_CityName_Key), CityName);
            editor.apply();
        }
    }
}
