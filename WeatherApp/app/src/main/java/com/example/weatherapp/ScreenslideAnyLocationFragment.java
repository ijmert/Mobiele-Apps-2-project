package com.example.weatherapp;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScreenslideAnyLocationFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.any_location_fragment, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAutoCompleteView();
    }

    private void setupAutoCompleteView()
    {
        View v = this.getView();

        AutoCompleteTextView actv =  (AutoCompleteTextView)v.findViewById(R.id.localeTV);
        List<String> cityNameList = Utils.GetCities(getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.select_dialog_item, cityNameList);
        actv.setAdapter(adapter);
        actv.setThreshold(1);

        actv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String city = (String) parent.getItemAtPosition(position);
                float[] latlon = Utils.getLatLonFromCity(city, getActivity());
                FragmentUpdater.updateFragment(latlon[0], latlon[1], getView(), true, getActivity());
                View v = (View)(getView().findViewById(R.id.weatherContainer));
                v.setVisibility(View.VISIBLE);
            }
        });
    }
}
