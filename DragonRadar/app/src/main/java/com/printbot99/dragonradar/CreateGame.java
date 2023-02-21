package com.printbot99.dragonradar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateGame extends AppCompatActivity implements View.OnClickListener, LocationListener{

    LocationManager locationManager;
    LocationListener locationListener;
    Context context;
    EditText enterRadius;
    EditText[] enterCoordinatesN = new EditText[7];
    EditText[] enterCoordinatesW = new EditText[7];;
    static double[] coordinatesN = new double[7];
    static double[] coordinatesW = new double[7];
    int numberOfDragonBalls = MainActivity.getNumberOfBalls();
    static double latitude;
    static double longitude;
    TextView wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        enterRadius = findViewById(R.id.radius);
        for (int i = 0; i<numberOfDragonBalls; i++){
            switch (i){
                case 0:
                    enterCoordinatesN[i] = findViewById(R.id.N1);
                    enterCoordinatesW[i] = findViewById(R.id.W1);
                    break;
                case 1:
                    enterCoordinatesN[i] = findViewById(R.id.N2);
                    enterCoordinatesW[i] = findViewById(R.id.W2);
                    break;
                case 2:
                    enterCoordinatesN[i] = findViewById(R.id.N3);
                    enterCoordinatesW[i] = findViewById(R.id.W3);
                    break;
                case 3:
                    enterCoordinatesN[i] = findViewById(R.id.N4);
                    enterCoordinatesW[i] = findViewById(R.id.W4);
                    break;
                case 4:
                    enterCoordinatesN[i] = findViewById(R.id.N5);
                    enterCoordinatesW[i] = findViewById(R.id.W5);
                    break;
                case 5:
                    enterCoordinatesN[i] = findViewById(R.id.N6);
                    enterCoordinatesW[i] = findViewById(R.id.W6);
                    break;
                case 6:
                    enterCoordinatesN[i] = findViewById(R.id.N7);
                    enterCoordinatesW[i] = findViewById(R.id.W7);
                    break;

            }
            enterCoordinatesN[i].setVisibility(View.VISIBLE);
            enterCoordinatesW[i].setVisibility(View.VISIBLE);
        }

        wait = findViewById(R.id.textView4);

        Button randomize = findViewById(R.id.button3);
        randomize.setOnClickListener(this);
        Button launch = findViewById(R.id.button2);
        launch.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.button3:
                for (int i = 0; i<numberOfDragonBalls; i++){
                    try {
                        int radius = Integer.parseInt(enterRadius.getText().toString());
                        double minLat = latitude - (double)radius*0.000009;
                        double maxLat = latitude + (double)radius*0.000009;
                        double minLong = longitude - (double)radius*0.0000135;
                        double maxLong = longitude + (double)radius*0.0000135;
                        enterCoordinatesN[i].setText(String.format("%.8f",(double)(minLat + Math.random() * (maxLat-minLat))));
                        enterCoordinatesW[i].setText(String.format("%.8f",(double)(minLong + Math.random() * (maxLong-minLong))));
                    }
                    catch(Exception e) {
                    }
                }
                break;
            case R.id.button2:
                for (int i = 0; i<numberOfDragonBalls; i++) {
                    coordinatesN[i] = Double.parseDouble(enterCoordinatesN[i].getText().toString());
                    coordinatesW[i] = Double.parseDouble(enterCoordinatesW[i].getText().toString());
                }
                Intent i = new Intent(this, Game.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    public static double getCoordinateN(int i){
        return coordinatesN[i];
    }

    public static double getCoordinateW(int i){
        return coordinatesW[i];
    }

    public static double getLatitude(){return latitude;}

    public static double getLongitude(){return longitude;}

    @Override
    public void onLocationChanged(Location location) {
        latitude = (double)location.getLatitude();
        longitude = (double)location.getLongitude();
        wait.setVisibility(View.GONE);
//        latitude = 48.4009650;
//        longitude = -4.503913;
    }
}