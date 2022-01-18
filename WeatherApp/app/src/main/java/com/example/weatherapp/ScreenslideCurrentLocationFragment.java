package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class ScreenslideCurrentLocationFragment extends Fragment {

    String TAG = "currentlocfrag";
    private long LOCATION_REFRESH_TIME = 10000;
    private float LOCATION_REFRESH_DISTANCE = 5;
    private int NUM_PAGES = 5;
    private static final int FINE_LOC_CODE = 1;
    private static final int COARSE_LOC_CODE = 2;
    private static final int INTERNET_CODE = 3;
    private FusedLocationProviderClient fusedLocationClient;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.current_location_fragment, container, false);
        Log.d(TAG, "onCreateView: currentlogfrac created");
        return rootView;
    }

    public void updateThisFragment(float lat, float lon)
    {
        Log.d(TAG, "updateThisFragment");
        FragmentUpdater.updateFragment(lat, lon, this.getView(), false, getActivity());
    }



    public void setupLocationManager()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity()); //idk what this does, but is probably needed
        checkPermission();
        LocationManager mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        Log.d(TAG, "setupLocationManager: ");
    }

    public void checkPermission() {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOC_CODE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOC_CODE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET}, INTERNET_CODE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocationManager(); //this is for updates when location changed
        Log.d(TAG, "onCreate");
    }

    public void updateCurrentLocationFragmentLastLocation() {
        checkPermission();
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(Location location) {
                updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentLocationFragmentLastLocation();
        Log.d(TAG, "onResume: at end, did updateCurrentLocationFragmentLastLocation");
    }
}
