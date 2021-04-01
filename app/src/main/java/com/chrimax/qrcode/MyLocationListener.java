package com.chrimax.qrcode;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.v("GPS", "localiastion : " + location.toString());
        Log.v("GPS", "Latitude , longitude : " + location.getLatitude() + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
