package com.example.localconnect.ui.issue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToonFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter;

public class ImageEditorActivity extends AppCompatActivity {

    private GPUImageView gpuImageView;
    private Uri sourceUri;
    private File outputFile;
    private float scaleFactor = 1.0f;
    private View resizeScroll, filterScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString == null) {
            finish();
            return;
        }
        sourceUri = Uri.parse(uriString);

        gpuImageView = findViewById(R.id.gpuImageView);
        gpuImageView.setImage(sourceUri);

        resizeScroll = findViewById(R.id.resizeScroll);
        filterScroll = findViewById(R.id.filterScroll);

        initFilters();
        setupListeners();
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnToolCrop).setOnClickListener(v -> startCrop());
        
        findViewById(R.id.btnToolFilter).setOnClickListener(v -> {
            resizeScroll.setVisibility(View.GONE);
            filterScroll.setVisibility(filterScroll.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.btnToolResize).setOnClickListener(v -> {
            filterScroll.setVisibility(View.GONE);
            resizeScroll.setVisibility(resizeScroll.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.btnResizeSmall).setOnClickListener(v -> setScale(0.5f));
        findViewById(R.id.btnResizeMedium).setOnClickListener(v -> setScale(0.75f));
        findViewById(R.id.btnResizeOriginal).setOnClickListener(v -> setScale(1.0f));

        findViewById(R.id.btnToolRotate).setOnClickListener(v -> {
            gpuImageView.setRotation(gpuImageView.getRotation() == 270 ? 0 : gpuImageView.getRotation() + 90);
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> saveAndFinish());
    }

    private void initFilters() {
        LinearLayout container = findViewById(R.id.filterContainer);
        addFilterPreview(container, "Normal", null);
        addFilterPreview(container, "B&W", new GPUImageGrayscaleFilter());
        addFilterPreview(container, "Sepia", new GPUImageSepiaToneFilter());
        addFilterPreview(container, "Toon", new GPUImageToonFilter());
        addFilterPreview(container, "Vignette", new GPUImageVignetteFilter());
        addFilterPreview(container, "Bg Remove", new GPUImageEmbossFilter()); // Placeholder for BG removal styling
    }

    private void setScale(float scale) {
        this.scaleFactor = scale;
        Toast.makeText(this, "Scale set to " + (int)(scale * 100) + "%", Toast.LENGTH_SHORT).show();
    }

    private void addFilterPreview(LinearLayout container, String name, jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter filter) {
        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setTextColor(0xFFFFFFFF);
        tv.setPadding(32, 16, 32, 16);
        tv.setOnClickListener(v -> gpuImageView.setFilter(filter));
        container.addView(tv);
    }

    private void startCrop() {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "trimmed.jpg"));
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .start(this);
    }

    private void saveAndFinish() {
        new Thread(() -> {
            try {
                // Get the processed bitmap with filters applied
                Bitmap bitmap = gpuImageView.getGPUImage().getBitmapWithFilterApplied();
                
                // Apply scaling if scaleFactor is not 1.0
                if (scaleFactor < 1.0f) {
                    int width = Math.round(bitmap.getWidth() * scaleFactor);
                    int height = Math.round(bitmap.getHeight() * scaleFactor);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    bitmap.recycle(); // Free original bitmap
                    bitmap = scaledBitmap;
                }
                
                outputFile = new File(getExternalFilesDir(null), "edited_" + System.currentTimeMillis() + ".jpg");
                java.io.FileOutputStream out = new java.io.FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                
                final String uriString = Uri.fromFile(outputFile).toString();
                runOnUiThread(() -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("edited_image_uri", uriString);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                sourceUri = resultUri;
                gpuImageView.setImage(sourceUri);
            }
        }
    }
}
