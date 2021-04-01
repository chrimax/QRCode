package com.chrimax.qrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.Result;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    private CodeScannerView scannerView;

    private TextView TV_textQRCode;
    private Button BT_send_coordinate;

    private boolean isCameraActived = false;
    private boolean isGPSActived = false;
    private boolean isCoordinatesNull = true;

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;

    private double latitude;
    private double longitude;

    private CoordinateDataBase helper;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannerView = findViewById(R.id.scanner_view);
        TV_textQRCode = findViewById(R.id.main_textQrCode);
        BT_send_coordinate = findViewById(R.id.main_sendCoordinate_bt);

        helper = new CoordinateDataBase(this);
        mCodeScanner = new CodeScanner(this, scannerView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestCamera();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        if (isCameraActived) {
            mCodeScanner.startPreview();
            checkLocationPermission();

            startCamera();

            // Appuie sur le bouton pour envoyer les coordonnées
            BT_send_coordinate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isCoordinatesNull) {
                        // Ajoute dans la base de données les coordonnées de l'emplacement actuel.
                        SQLiteDatabase db = helper.getWritableDatabase();

                        // Ajoute à la DB la latitude et longitude de l'emplacement actuel
                        helper.addCoordinate(latitude, longitude);

                        ArrayList<Coordinate> listCoordinates = new ArrayList<>();

                        Cursor cursor = db.query(true,"coordinate",new String[]{"idCoordinate","latitude","longitude"},null,null,null,null,"idCoordinate",null);

                        while (cursor.moveToNext()) {
                            listCoordinates.add(new Coordinate(cursor));
                        }
                        cursor.close();
                        db.close();

                        String coordinates = "";

                        // Ajoute dans une variable la latitude et longitude des coordonnées prises
                        for (Coordinate currentCoordinate : listCoordinates) {
                            coordinates += "Latitude " + currentCoordinate.getLatitude();
                            coordinates += " Longitude " + currentCoordinate.getLongitude() + "\n";
                        }

                        // Ouvre la pop up qui affiche les coordonnées prises
                        Intent popWindow = new Intent(MainActivity.this, PopWindow.class);
                        popWindow.putExtra("coordinate", coordinates);
                        startActivity(popWindow);

                        // Affecte true pour qu'on ne puisse pas renvoyer les mêmes coordonnées sans les avoirs scannées
                        isCoordinatesNull = true;

                        TV_textQRCode.setText(R.string.scan_QRCode);

                    } else {
                        // Affiche un message lorsque les coordonnées sont envoyées alors qu'elles sont vides
                        Toast.makeText(MainActivity.this, R.string.coordinate_null_toast, Toast.LENGTH_SHORT).show();
                }
                }
            });
        }

    }


    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


    /**
     * Demande à l'utilisateur s'il veut activer la caméra
     */
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isCameraActived = true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }


    /**
     * Vérifie si l'utilisateur a donné son consentement pour utiliser la caméra et la localisation
     *
     * @param requestCode  Le code de requête transmis
     * @param permissions  Les autorisations demandées
     * @param grantResults Pour les autorisations correspondnates
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isCameraActived = true;
                } else {
                    Toast.makeText(this, R.string.camera_refused, Toast.LENGTH_SHORT).show();
                }
            }

            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "La localisation a été autorisée", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, R.string.location_refused, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * Lance le scan du QRCode
     */
    private void startCamera() {
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Vérifie que le GPS a bien été activé pour afficher les coordonnées sinon
                        // demande de d'activer le GPS à l'utilisateur.
                        isGPSActived = isGPSActived();

                        if (isGPSActived) {
                            getLocationSmartphone();
                        } else {
                            enableGPS();
                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }


    /**
     * Affiche la position du smartphone en latitude et longitude sur un textView
     */
    @SuppressLint("MissingPermission")
    public void getLocationSmartphone() {

        myLocationListener = new MyLocationListener();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                loadLocation();

                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    isCoordinatesNull = false;

                    TV_textQRCode.setText("Latitude : " + latitude + " Longitude : " + longitude);
                    Toast.makeText(MainActivity.this, R.string.location_scanQrCode_success, Toast.LENGTH_SHORT).show();

                    stopLocation();
                } else {
                    isCoordinatesNull = true;

                    Toast.makeText(MainActivity.this, R.string.toast_load_location, Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    /**
     * Demande l'autorisation d'utiliser le GPS
     */
    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!isGPSActived()) {
                enableGPS();
            }

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            }
        }
    }


    /**
     * @return un booléen qui indique si le GPS est activé ou non.
     */
    public boolean isGPSActived() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * Affiche un message d'alerte pour activer la localisation dans les paramètres du smartphone
     */
    public void enableGPS() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(R.string.location_dialog_text);
        dialog.setNegativeButton(R.string.location_dialog_bt_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.setPositiveButton(R.string.location_dialog_bt_positive, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cela dirigera l'utilisateur vers l'écran des paramètres de localisation de l'appareil
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }


    /**
     * Recherche une localisation
     */
    @SuppressLint("MissingPermission")
    private void loadLocation() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 150, myLocationListener);
    }


    /**
     * Stop les mises à jour de localisation
     */
    private void stopLocation() {
        if (locationManager != null)
        locationManager.removeUpdates(myLocationListener);
        myLocationListener = null;
    }
}