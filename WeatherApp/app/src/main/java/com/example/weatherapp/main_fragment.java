package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


public class main_fragment extends Fragment {


    String TAG = "main_fragment";

    public main_fragment() {
        // Required empty public constructor
    }

//    private long LOCATION_REFRESH_TIME = 10000;
//    private float LOCATION_REFRESH_DISTANCE = 5;
//    private int NUM_PAGES = 5;
//    private static final int FINE_LOC_CODE = 1;
//    private static final int COARSE_LOC_CODE = 2;
//    private static final int INTERNET_CODE = 3;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private LocationManager mLocationManager;
//    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue queue;


//    private final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(@NonNull Location location) {
//            ((ScreenslideCurrentLocationFragment)pagerAdapter.fragments.get(0)).updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
//        }
//    };


//    public void checkPermission() {
//        // Checking if permission is not granted
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
//        {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOC_CODE);
//        }
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
//        {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOC_CODE);
//        }
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED)
//        {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET}, INTERNET_CODE);
//        }
//    }
//
//    public void updateCurrentLocationFragmentLastLocation() {
//        checkPermission();
//        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
//                    @RequiresApi(api = Build.VERSION_CODES.N)
//                    @Override
//                    public void onSuccess(Location location) {
//                        ((ScreenslideCurrentLocationFragment)pagerAdapter.fragments.get(0)).updateThisFragment((float)location.getLatitude(), (float)location.getLongitude());
//                    }
//                });
//    }

//    public void setupLocationManager()
//    {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity()); //idk what this does, but is probably needed
//        checkPermission();
//        LocationManager mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
//    }

    public void setupScreenSlidePager(View view)
    {
        mPager = view.findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        pagerAdapter.add(new ScreenslideCurrentLocationFragment());
        pagerAdapter.add(new ScreenslideAnyLocationFragment());
        Log.d(TAG, "created frags again");
        mPager.setAdapter(pagerAdapter);
        Log.d(TAG, "setupScreenSlidePager: either created frags or didn't");
    }



    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public final List<Fragment> fragments = new ArrayList<>();
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

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


//    @Override
//    public void onResume() {
//
//        super.onResume();
//        setupScreenSlidePager(getView());
//        Log.d(TAG, "onresume");
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.root_preferences, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_options_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: created options menu");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                NavHostFragment.findNavController(this).navigate(R.id.action_main_fragment_to_settingsFragment);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_fragment, container, false);
        setupScreenSlidePager(view);
        Log.d(TAG, "onCreateView: view created inflated now to return it");
        return view;
    }
}