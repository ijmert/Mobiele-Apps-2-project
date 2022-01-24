package com.example.weatherapp;

import java.util.ArrayList;
import java.util.List;

//class containing all weather forecast data of a certain place or region
public class WeatherForecast {
    List<String> days = new ArrayList<>();
    List<Float> minTemps = new ArrayList<>();
    List<Float> maxTemps = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> mainDescriptor = new ArrayList<>();

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public List<Float> getMinTemps() {
        return minTemps;
    }

    public void setMinTemps(List<Float> minTemps) {
        this.minTemps = minTemps;
    }

    public List<Float> getMaxTemps() {
        return maxTemps;
    }

    public void setMaxTemps(List<Float> maxTemps) {
        this.maxTemps = maxTemps;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }
}
