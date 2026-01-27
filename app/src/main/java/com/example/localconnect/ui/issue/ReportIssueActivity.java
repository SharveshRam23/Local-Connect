package com.example.localconnect.ui.issue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.localconnect.R;
import com.example.localconnect.util.ImageUtil;

public class ReportIssueActivity extends AppCompatActivity {

    ImageView imageView;
    Button btnCamera;

    // Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                Bitmap bitmap = (Bitmap) extras.get("data");
                                if (bitmap != null) {
                                    Bitmap resized = ImageUtil.resize(bitmap, 600, 600);
                                    Bitmap gray = ImageUtil.toGrayScale(resized);
                                    imageView.setImageBitmap(gray);
                                }
                            }
                        }
                    }
            );

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            launchCamera();
                        } else {
                            Toast.makeText(this, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        imageView = findViewById(R.id.issueImage);
        btnCamera = findViewById(R.id.btnCapture);

        btnCamera.setOnClickListener(v -> checkPermissionAndLaunch());
    }

    private void checkPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }
}
