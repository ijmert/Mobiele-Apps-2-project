package com.example.weatherapp;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

//This is the main fragment, this houses a pager which enables us to slide between fragments. There is also the ability to navigate from this fragment to the settingsfragment.
public class main_fragment extends Fragment {


    String TAG = "main_fragment";

    public main_fragment() {
        // Required empty public constructor
    }

    private ViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;

    //custom class ScreenSlidePagerAdapter is the adapter we will use for our pager.
    //The overriden methods are needed for the viewpager to work.
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        //A list of all fragments that are housed in our viewpager.
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



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //enable the optionsmenu
        PreferenceManager.setDefaultValues(getActivity(), R.xml.root_preferences, false);//set default values for this activity gotten from our preferences file root_preferences.xml
    }

    //initialize menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_options_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: created options menu");
    }

    //executes when a menu item is selected, then will navigate using appropriate navigate action. Currently the app only has one menu item, but this could be easily expanded.
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

    //here we inflate the view and setup the viewpager when the view has been inflated.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_fragment, container, false);
        setupScreenSlidePager(view);
        Log.d(TAG, "onCreateView: view created inflated now to return it");
        return view;
    }


    public void setupScreenSlidePager(View view) //the view passed here is the fragment view, see above method.
    {
        mPager = view.findViewById(R.id.pager); //we get the pager view from our fragmentview.
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager()); //when we make a ScreenSlidePagerAdapter we must pass a fragmentmanager. We use the getChildFragmentManager for this.
        pagerAdapter.add(new ScreenslideCurrentLocationFragment()); //we add our fragments to the pagerAdapter
        pagerAdapter.add(new ScreenslideAnyLocationFragment());
        mPager.setAdapter(pagerAdapter);//we set the adapter of the pagerview to the newly created pagerAdapter
    }
}