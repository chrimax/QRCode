package com.chrimax.qrcode;

import android.database.Cursor;

public class Coordinate {

    private double latitude;
    private double longitude;

    /**
     * Construit une coordonnée
     * @param cursor Tableau dans lequel sont toutes les latitudes et longitudes.
     */
    public Coordinate(Cursor cursor) {
        latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
    }

    /**
     * @return une latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return une longitude.
     */
    public double getLongitude() {
        return longitude;
    }

}
