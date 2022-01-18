package com.example.weatherapp;

public class WeatherData {
    public float temperature;
    public String descriptor;
    public float windSpeed;
    public int windDeg;
//    public float windGust;
    public String windDirection;

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

//    public float getWindGust() {
//        return windGust;
//    }
//
//    public void setWindGust(float windGust) {
//        this.windGust = windGust;
//    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDeg(int windDeg) {
        this.windDeg = windDeg;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindDeg() {
        return windDeg;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String country) {
        this.descriptor = country;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float name) {
        this.temperature = temperature;
    }
}
