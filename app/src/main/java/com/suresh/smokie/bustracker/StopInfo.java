package com.suresh.smokie.bustracker;


import com.google.android.gms.maps.model.LatLng;

public class StopInfo {
    private String name;
    String lon,lat;

    public StopInfo() {

    }

    public StopInfo(String name, String lon, String lat) {
        this.name = name;
        this.lon = lon;
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getlon() {
        return lon;
    }

    public void setlon(String lon) {
        this.lon = lon;
    }

    public String getlat() {
        return lat;
    }

    public void setlat(String lat) {
        this.lat = lat;
    }

    public LatLng getLatLngObject() {
        LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
        return latLng;
    }
}
