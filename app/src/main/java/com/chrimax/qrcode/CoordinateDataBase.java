package com.chrimax.qrcode;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class CoordinateDataBase extends SQLiteOpenHelper  {

    static String DB_NAME = "QRCode.db";
    static int DB_VERSION = 1;

    /**
     * Construit une base de données
     * @param context
     */
    public CoordinateDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Création de la DB des coordonnées
     * @param db Base de données
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlCreateDataTableCoordinate = "CREATE TABLE coordinate(idCoordinate INTEGER PRIMARY KEY, latitude DOUBLE, longitude DOUBLE);";
        db.execSQL(sqlCreateDataTableCoordinate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Ajoute dans la base de données les coordonnées
     * @param latitude  Latitude a ajouté dans la DB
     * @param longitude Longitude a ajouté dans la DB
     */
    public void addCoordinate(double latitude, double longitude) {
        String strSql ="INSERT INTO coordinate (latitude, longitude) values (" + latitude + ", " + longitude + ")";
        this.getWritableDatabase().execSQL(strSql);
    }
}
