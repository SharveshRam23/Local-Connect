package com.example.localconnect.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.localconnect.R;
import com.example.localconnect.databinding.ActivityMandatoryServiceDetailBinding;
import com.example.localconnect.util.ImageUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MandatoryServiceDetailActivity extends AppCompatActivity {

    private ActivityMandatoryServiceDetailBinding binding;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMandatoryServiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        loadData();

        binding.btnCall.setOnClickListener(v -> {
            String phone = getIntent().getStringExtra("service_phone");
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        binding.btnNavigate.setOnClickListener(v -> {
            if (latitude != 0.0 && longitude != 0.0) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Fallback to browser if Maps app isn't installed
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude));
                    startActivity(browserIntent);
                }
            }
        });
    }

    private void loadData() {
        Intent intent = getIntent();
        String name = intent.getStringExtra("service_name");
        String category = intent.getStringExtra("service_category");
        String address = intent.getStringExtra("service_address");
        String phone = intent.getStringExtra("service_phone");
        String hours = intent.getStringExtra("service_hours");
        String imageUrl = intent.getStringExtra("service_image");
        boolean is24x7 = intent.getBooleanExtra("service_is24x7", false);
        boolean isEmergency = intent.getBooleanExtra("service_emergency", false);
        latitude = intent.getDoubleExtra("service_lat", 0.0);
        longitude = intent.getDoubleExtra("service_lng", 0.0);

        binding.tvServiceName.setText(name);
        binding.tvCategory.setText(category);
        binding.tvAddress.setText(address);
        binding.tvWorkingHours.setText(is24x7 ? "Open 24x7" : hours);

        if (isEmergency) {
            binding.tvEmergencyBadge.setVisibility(View.VISIBLE);
        }

        if (is24x7) {
            binding.tvStatus.setText("OPEN 24x7");
            binding.tvStatus.setTextColor(0xFF4CAF50);
        } else {
             binding.tvStatus.setText("CHECK HOURS");
             binding.tvStatus.setTextColor(0xFFE91E63);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.length() > 500) {
                binding.ivServiceImage.setImageBitmap(ImageUtil.fromBase64(imageUrl));
            } else {
                Glide.with(this).load(imageUrl).into(binding.ivServiceImage);
            }
        }
    }
}
