package com.example.localconnect.ui.issue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Issue;
import com.example.localconnect.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@dagger.hilt.android.AndroidEntryPoint
public class ReportIssueActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCamera, btnGallery, btnEditImage, btnSubmit;
    private EditText etDescription;
    private Bitmap currentBitmap;
    private Uri currentImageUri;

    @javax.inject.Inject
    com.example.localconnect.data.dao.IssueDao issueDao;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @javax.inject.Inject
    com.google.firebase.storage.FirebaseStorage firebaseStorage;

    // Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                Bitmap bitmap = (Bitmap) extras.get("data");
                                if (bitmap != null) {
                                    currentBitmap = ImageUtil.resize(bitmap, 800, 800);
                                    imageView.setImageBitmap(currentBitmap);
                                    
                                    // Save to temp file to get a URI for the editor
                                    File tempFile = new File(getCacheDir(), "camera_temp.jpg");
                                    String path = ImageUtil.compressImage(currentBitmap, tempFile);
                                    if (path != null) {
                                        currentImageUri = Uri.fromFile(new File(path));
                                        btnEditImage.setVisibility(android.view.View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String uriString = result.getData().getStringExtra("edited_image_uri");
                            if (uriString != null) {
                                currentImageUri = Uri.parse(uriString);
                                try {
                                    currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currentImageUri);
                                    imageView.setImageBitmap(currentBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

    // Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            currentImageUri = uri;
                            try {
                                // Save to temp file to use ImageUtil path-based decoding
                                File tempFile = new File(getCacheDir(), "temp_image.jpg");
                                try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                                     FileOutputStream out = new FileOutputStream(tempFile)) {
                                    byte[] buffer = new byte[1024];
                                    int len;
                                    while ((len = in.read(buffer)) > 0) {
                                        out.write(buffer, 0, len);
                                    }
                                }

                                currentBitmap = ImageUtil.decodeSampledBitmapFromPath(tempFile.getAbsolutePath(), 800, 800);
                                imageView.setImageBitmap(currentBitmap);
                                btnEditImage.setVisibility(android.view.View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        imageView = findViewById(R.id.issueImage);
        btnCamera = findViewById(R.id.btnCapture);
        btnGallery = findViewById(R.id.btnGallery);
        btnEditImage = findViewById(R.id.btnEditImage);
        btnSubmit = findViewById(R.id.btnSubmitIssue);
        etDescription = findViewById(R.id.etDescription);

        btnCamera.setOnClickListener(v -> checkPermissionAndLaunch());
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnEditImage.setOnClickListener(v -> {
            if (currentImageUri != null) {
                Intent intent = new Intent(this, ImageEditorActivity.class);
                intent.putExtra("image_uri", currentImageUri.toString());
                editLauncher.launch(intent);
            }
        });
        btnSubmit.setOnClickListener(v -> submitIssue());
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

    private void submitIssue() {
        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentBitmap == null) {
            Toast.makeText(this, "Please select or capture an image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");

        // Get user session
        SharedPreferences prefs = getSharedPreferences("local_connect_prefs", MODE_PRIVATE);
        String pincode = prefs.getString("user_pincode", "000000");
        String reporterName = prefs.getString("user_name", "Anonymous");

        // Save compressed image locally first
        String id = UUID.randomUUID().toString();
        String fileName = "issue_" + id + ".jpg";
        File file = new File(getExternalFilesDir(null), fileName);
        String imagePath = ImageUtil.compressImage(currentBitmap, file);

        if (imagePath == null) {
            Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Submit Issue");
            return;
        }

        // Upload to Firebase Storage
        com.google.firebase.storage.StorageReference ref = firebaseStorage.getReference()
                .child("issue_images/" + id + ".jpg");

        ref.putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String cloudImageUrl = uri.toString();
                        
                        // Create Issue object with cloud URL
                        Issue issue = new Issue(id, description, cloudImageUrl, pincode, System.currentTimeMillis(), reporterName);
                        
                        // Save to Firestore
                        firestore.collection("issues")
                                .document(id)
                                .set(issue)
                                .addOnSuccessListener(aVoid -> {
                                    // Sync to local Room
                                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                                        issueDao.insert(issue);
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "Issue reported and synced to cloud!", Toast.LENGTH_LONG).show();
                                            finish();
                                        });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    btnSubmit.setEnabled(true);
                                    btnSubmit.setText("Submit Issue");
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Storage Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Issue");
                });
    }

    // Removed old saveImageLocally as ImageUtil.compressImage handles it
}
