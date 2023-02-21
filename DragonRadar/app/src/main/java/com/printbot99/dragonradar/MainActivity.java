package com.printbot99.dragonradar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static int numberOfBalls;
    EditText enterNumberOfDragonBalls;
    //Context cxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startGame = findViewById(R.id.button);
        enterNumberOfDragonBalls = findViewById(R.id.number);
        startGame.setOnClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
    }

    public void onClick(View v){
        try {
            numberOfBalls = Integer.parseInt(enterNumberOfDragonBalls.getText().toString());
            if (numberOfBalls > 7){
                numberOfBalls = 7;
            }
            Intent i = new Intent(this, CreateGame.class);
            startActivity(i);
        }
        catch(Exception e) {
        }
    }

    public static int getNumberOfBalls(){
        return numberOfBalls;
    }
}