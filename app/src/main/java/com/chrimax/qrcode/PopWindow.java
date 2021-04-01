package com.chrimax.qrcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PopWindow extends AppCompatActivity {

    private TextView TV_coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popwindow);
        TV_coordinate = findViewById(R.id.popwindow_coordinate);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.4));

        //getWindow().getDecorView().setBackgroundColor(Color.parseColor("#BFBFBF"));

        // Récupère les coordonnées passés en paramètre lorsque l'Intent a été créé
        String coordinates = "";
        Intent popWindow = getIntent();
        coordinates = popWindow.getStringExtra("coordinate");


        // Affiche les coordonnées prises
        TV_coordinate.setText(coordinates);
    }
}
