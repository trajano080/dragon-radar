package com.printbot99.dragonradar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Recognition extends AppCompatActivity {

    // ------------------ Camera preview ------------------ //
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView cameraView;
    private Bitmap bitmapBuffer = null;
    // ---------------------------------------------------- //

    // ---------------- Set up recognition ---------------- //
    private ObjectDetector objectDetector = null;
    static String model = "efficientdet-lite0.tflite";
    float threshold = 0.5f;
    int maxResults = 3;
    int numThreads = 2;
    // ---------------------------------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        // Camera permission request
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1000);
        }

        cameraView = findViewById(R.id.CameraView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                setUpAnalyser(cameraProvider);
            }
            catch (ExecutionException | InterruptedException e){}
        }, ContextCompat.getMainExecutor(this));

        setupObjectDetector();
    }

    private void setupObjectDetector(){
        // Create the base options for the detector using specifies max results and score threshold
        ObjectDetector.ObjectDetectorOptions optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(BaseOptions.builder().setNumThreads(numThreads).build())
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)
                .build();

        try{
            objectDetector = ObjectDetector.createFromFileAndOptions(this, model, optionsBuilder);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpAnalyser(ProcessCameraProvider cameraProvider) {

        // ----------------- Camera X preview ----------------- //
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
        // ---------------------------------------------------- //

        // ------------------ Image Analyser ------------------ //
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetResolution(new Size(640, 480))
                .build();

        imageAnalysis.setAnalyzer(getMainExecutor(), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy){

                if (bitmapBuffer == null){
                    bitmapBuffer = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                }
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                bitmapBuffer = cameraView.getBitmap();
                detectObjects(bitmapBuffer, rotationDegrees);

                imageProxy.close();
            }
        });
        // ---------------------------------------------------- //

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }

    private void detectObjects(Bitmap imageBitmap, int imageRotation){
        if (objectDetector == null) {
            setupObjectDetector();
        }

        if (imageBitmap != null){
            // Preprocess the image and convert it into a TensorImage for detection.
            TensorImage tensorImage = TensorImage.fromBitmap(imageBitmap);

            try{
                List<Detection> result = objectDetector.detect(tensorImage);
                onResults(result);
            }
            catch (java.lang.NullPointerException e){
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    void onResults(List<Detection>results){
        int len = results.size();
        if (len >= 1){
            for (int i=0; i<len; i++){
                List<Category> categories = results.get(i).getCategories();
                float score = categories.get(0).getScore();
                String label = categories.get(0).getLabel();
                if (score >= 0.60 && (label.equals("orange"))){
                    Victory();
                }
            }
        }
    }

    public void BackButton(View view){
        Intent intent = new Intent();
        intent.putExtra("result",false);
        setResult(78, intent);
        finish();
    }

    public void Victory(){
        Intent intent = new Intent();
        intent.putExtra("result",true);
        setResult(78, intent);
        finish();
    }
}