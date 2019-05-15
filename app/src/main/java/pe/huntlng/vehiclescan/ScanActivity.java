package pe.huntlng.vehiclescan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pe.huntlng.vehiclescan.mlkit.common.CameraSource;
import pe.huntlng.vehiclescan.mlkit.common.CameraSourcePreview;
import pe.huntlng.vehiclescan.mlkit.common.GraphicOverlay;
import pe.huntlng.vehiclescan.mlkit.common.TextRecognitionProcessor;

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private CameraSourcePreview preview; // To handle the camera
    private GraphicOverlay graphicOverlay; // To draw over the camera screen
    private CameraSource cameraSource = null; //To handle the camera
    private static final int PERMISSION_REQUESTS = 1; // To handle the runtime permissions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_main);

        preview = (CameraSourcePreview) findViewById(R.id.Preview);
        graphicOverlay = (GraphicOverlay) findViewById(R.id.Overlay);

        if (preview == null) {
            Log.d(TAG, " Preview is null ");
        }

        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null ");
        }
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    // Actual code to start the camera
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "startCameraSource resume: Preview is null ");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "startCameraSource resume: graphOverlay is null ");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.d(TAG, "startCameraSource : Unable to start camera source." + e.getMessage());
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    // Function to check if all permissions given by the user
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    // List of permissions required by the application to run.
    private String[] getRequiredPermissions() {
        return new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    // Checking a Runtime permission value
    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "isPermissionGranted Permission granted : " + permission);
            return true;
        }
        Log.d(TAG, "isPermissionGranted: Permission NOT granted -->" + permission);
        return false;
    }
    // getting runtime permissions
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    // Function to create a camera source and retain it.
    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {

            cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(this));

        } catch (Exception e) {
            Log.d(TAG, "createCameraSource can not create camera source: " + e.getCause());
            e.printStackTrace();
        }
    }

    public void returnResult(String text){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",text);
        setResult(RESULT_OK, returnIntent);
        Log.d("ScanActivity", text);
        finish();
    }
}

