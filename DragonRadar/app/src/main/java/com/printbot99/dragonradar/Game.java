package com.printbot99.dragonradar;

import static com.printbot99.dragonradar.CreateGame.getLatitude;
import static com.printbot99.dragonradar.CreateGame.getLongitude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class Game extends AppCompatActivity implements LocationListener, View.OnClickListener, SensorEventListener{

    LocationManager locationManager;
    //    LocationListener locationListener;
//    Context context;
    int successes = 0;
    float[] mGravity;
    float[] mGeomagnetic;
    double rotation;
    double latitude = getLatitude();
    double longitude = getLongitude();
    static int numberOfDragonBalls = MainActivity.getNumberOfBalls();
    double[] coordinatesN = new double[numberOfDragonBalls];
    double[] coordinatesW = new double[numberOfDragonBalls];
    double[] relativeLatitude = new double[numberOfDragonBalls];
    double[] relativeLongitude = new double[numberOfDragonBalls];
    boolean[] found = new boolean[numberOfDragonBalls];
    ImageView[] dragonBalls = new ImageView[numberOfDragonBalls];
    MediaPlayer mp;

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 78) {
                        Intent data = result.getData();
                        boolean success = data.getBooleanExtra("result",false);
                        Balls(success);
                    }
                }
            });

    ImageView[] progress = new ImageView[numberOfDragonBalls];
    double[] zooms = {100.0, 25.0, 10.0, 5.0, 3.0, 1.0};
    int zoom = 0;
    ConstraintLayout clayout;
    Handler handler = new Handler(Looper.getMainLooper());

    // 0.0009 latitude = 100 meters
    // 0.00135 longitude = 100 meters
    // 1 square = 65dp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        clayout = (ConstraintLayout) findViewById(R.id.layout);

        numberOfDragonBalls = MainActivity.getNumberOfBalls();
        for (int i = 0; i < numberOfDragonBalls; i++) {
            found[i] = false;
            switch (i){
                case 0:
                    dragonBalls[i] = findViewById(R.id.ball1);
                    progress[i] = findViewById(R.id.progress1);
                    break;
                case 1:
                    dragonBalls[i] = findViewById(R.id.ball2);
                    progress[i] = findViewById(R.id.progress2);
                    break;
                case 2:
                    dragonBalls[i] = findViewById(R.id.ball3);
                    progress[i] = findViewById(R.id.progress3);
                    break;
                case 3:
                    dragonBalls[i] = findViewById(R.id.ball4);
                    progress[i] = findViewById(R.id.progress4);
                    break;
                case 4:
                    dragonBalls[i] = findViewById(R.id.ball5);
                    progress[i] = findViewById(R.id.progress5);
                    break;
                case 5:
                    dragonBalls[i] = findViewById(R.id.ball6);
                    progress[i] = findViewById(R.id.progress6);
                    break;
                case 6:
                    dragonBalls[i] = findViewById(R.id.ball7);
                    progress[i] = findViewById(R.id.progress7);
                    break;
                default:
                    break;
            }
            coordinatesN[i] = CreateGame.getCoordinateN(i);
            coordinatesW[i] = CreateGame.getCoordinateW(i);
            progress[i].setVisibility(View.VISIBLE);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.dragonradarsound);
        Button zoomButton = findViewById(R.id.button4);
        zoomButton.setOnClickListener(this);

        Button foundBall = findViewById(R.id.button5);
        foundBall.setOnClickListener(this);
    }

    public void onLocationChanged(Location location) {
//        clayout.setBackgroundResource(R.drawable.radar);
        latitude = (double)location.getLatitude();
        longitude = (double)location.getLongitude();
//        latitude = 48.4009650;
//        longitude = -4.503913;
        updateDragonBalls();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button4:

                clayout.setBackgroundResource(R.drawable.radarpressed);
                mp.start();
                zoom++;
                if (zoom>5){
                    zoom = 0;
                }
                updateDragonBalls();



                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clayout.setBackgroundResource(R.drawable.radar);
                    }
                }, 700);

                break;
            case R.id.button5:
                Intent i = new Intent(getApplicationContext(),Recognition.class);
                activityLauncher.launch(i);
                break;
            default:
                break;
        }
    }

    private void Balls(boolean success){
        if (success){
            successes++;
            double distance2small = 999999999;
            double distance2;
            int smallest = 0;
            for (int i = 0; i<numberOfDragonBalls; i++) {
                if(found[i]){
                    continue;
                }
                distance2 = Math.pow(relativeLatitude[i],2) + Math.pow(relativeLongitude[i],2);
                if (distance2<distance2small){
                    smallest = i;
                    distance2small = distance2;
                }
            }
            found[smallest] = true;
            dragonBalls[smallest].setVisibility(View.GONE);
            switch (successes){
                case 1:
                    progress[successes-1].setImageResource(R.drawable.onestar);
                    break;
                case 2:
                    progress[successes-1].setImageResource(R.drawable.twostar);
                    break;
                case 3:
                    progress[successes-1].setImageResource(R.drawable.threestar);
                    break;
                case 4:
                    progress[successes-1].setImageResource(R.drawable.fourstar);
                    break;
                case 5:
                    progress[successes-1].setImageResource(R.drawable.fivestar);
                    break;
                case 6:
                    progress[successes-1].setImageResource(R.drawable.sixstar);
                    break;
                case 7:
                    progress[successes-1].setImageResource(R.drawable.sevenstar);
                    break;
                default:
                    break;
            }
            if (successes==numberOfDragonBalls){
                Intent i = new Intent(this, Win.class);
                startActivity(i);
            }
        }
    }

    private void updateDragonBalls(){
        for(int i = 0; i<numberOfDragonBalls; i++){
            relativeLatitude[i] = coordinatesN[i] - latitude;
            relativeLongitude[i] = coordinatesW[i] - longitude;

            int pixelsLat =(int)( 200.0*relativeLatitude[i]*100.0/0.0009/zooms[zoom]);
            int pixelsLong = (int)(200.0*relativeLongitude[i]*100.0/0.00135/zooms[zoom]);
            int pixelsUp = (int) (pixelsLat*Math.cos(rotation) + pixelsLong*Math.sin(rotation));
            int pixelsRight = (int) (pixelsLong*Math.cos(rotation) - pixelsLat*Math.sin(rotation));
            if(!found[i]){
                dragonBalls[i].setVisibility(View.VISIBLE);
            }
            if (Math.pow( Math.pow(pixelsLat,2) + Math.pow(pixelsLong,2), 0.5) < 1100) {
//                if(i>=successes) {
//                    dragonBalls[i].setVisibility(View.VISIBLE);
//                }
            }else{
//                dragonBalls[i].setVisibility(View.GONE);
                double rad = Math.atan2(pixelsUp,pixelsRight);
                pixelsUp = (int)(975.0*Math.sin(rad));
                pixelsRight = (int)(975.0*Math.cos(rad));
            }
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(dragonBalls[i].getLayoutParams());
            setMargins(dragonBalls[i], pixelsUp,0,0,pixelsRight-330);

        }
    }
    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        return false;
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        clayout.setBackgroundResource(R.drawable.radar);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimut = orientation[0];
                rotation = Math.toRadians((double)(-azimut * 360 / (2 * 3.14159f)));
            }
        }
        updateDragonBalls();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}