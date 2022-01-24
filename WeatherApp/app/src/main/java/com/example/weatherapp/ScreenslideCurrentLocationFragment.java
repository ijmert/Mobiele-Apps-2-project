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
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

//This fragment is intended to show weatherdata of the current location of the device.
public class ScreenslideCurrentLocationFragment extends Fragment {

    String TAG = "currentlocfrag";
    //both the LOCATION_REFRESH_TIME must have passed AND the device must have moved LOCATION_REFRESH_DISTANCE for the app to update its location
    private long LOCATION_REFRESH_TIME = 10000;
    private float LOCATION_REFRESH_DISTANCE = 0;

    //these are just codes that have to be used for permissions
    private static final int FINE_LOC_CODE = 1;
    private static final int COARSE_LOC_CODE = 2;
    private static final int INTERNET_CODE = 3;

    //a location provider client
    private FusedLocationProviderClient fusedLocationClient;


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            //the method that will be called when the location has changed.
            updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
        }
    };


    //inflate the view with the correct layout file
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.current_location_fragment, container, false);
        Log.d(TAG, "onCreateView: currentlogfrac created");
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocationManager(); //setup the locatio manager
        Log.d(TAG, "onCreate");
    }

    @Override //When the app is paused we stop receiving location updates and triggering the listener.
    public void onPause() {
        super.onPause();
        LocationManager mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override //when the app is resumed we resume receiving location updates
    public void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOC_CODE); //check if we have permission to access location and internet, and if not, ask permission
        LocationManager mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE); // initialize location manager.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener); //request location updates upon which mLocationListener will be called.
        updateCurrentLocationFragmentLastLocation();//when the app resumes we get the last known location and call on the api to get weather information on it
        Log.d(TAG, "onResume: at end, did updateCurrentLocationFragmentLastLocation");
    }

    //calls fragment updater to update this fragment with weather data.
    public void updateThisFragment(float lat, float lon)
    {
        Log.d(TAG, "updateThisFragment");
        FragmentUpdater.updateFragment(lat, lon, this.getView(), false, getActivity());
    }


    public void setupLocationManager()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity()); //setup the fused location client so that we can get the last location in updateCurrentLocationFragmentLastLocation
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOC_CODE); //check if we have permission to access location and internet, and if not, ask permission
        LocationManager mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE); // initialize location manager.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener); //request location updates upon which mLocationListener will be called.
        Log.d(TAG, "setupLocationManager: ");
    }



    public void checkPermission(String permission, int code) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, code);
        }
        while(ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED){}
    }


    //gets last location and updates fragment with weather information about last location.
    public void updateCurrentLocationFragmentLastLocation() {
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOC_CODE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(Location location) {
                updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
            }
        });
    }

}
