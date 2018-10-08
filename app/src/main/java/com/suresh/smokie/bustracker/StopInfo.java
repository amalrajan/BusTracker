package com.suresh.smokie.bustracker;


import com.google.android.gms.maps.model.LatLng;

public class StopInfo {
    private String name,longitude,latitude;

    public StopInfo() {

    }

    public StopInfo(String name, String longitude, String latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public LatLng getLatLngObject() {
        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        return latLng;
    }
}
