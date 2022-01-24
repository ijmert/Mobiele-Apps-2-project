
package com.example.weatherapp;


// This class is intended to hold information about any given city. Currently in my app I only use the 'name' field of this class, but the other fields can be used when expanding the app.
public class City {
    String country;
    String name;
    String lat;
    String lng;


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
