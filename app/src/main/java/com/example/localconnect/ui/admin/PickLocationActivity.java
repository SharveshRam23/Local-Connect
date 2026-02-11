package com.example.localconnect.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localconnect.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class PickLocationActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSM Configuration
        // Use getSharedPreferences instead of PreferenceManager to avoid dependency issues
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_pick_location);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Default to some location (e.g., center of map or user location if available)
        // For now, let's just set a default zoom
        map.getController().setZoom(15.0);
        
        // Try to get passed coordinates or default to something (e.g., Chennai generic)
        double lat = getIntent().getDoubleExtra("lat", 13.0827);
        double lng = getIntent().getDoubleExtra("lng", 80.2707);
        GeoPoint startPoint = new GeoPoint(lat, lng);
        map.getController().setCenter(startPoint);

        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnConfirm.setOnClickListener(v -> {
            GeoPoint center = (GeoPoint) map.getMapCenter();
            Intent result = new Intent();
            result.putExtra("lat", center.getLatitude());
            result.putExtra("lng", center.getLongitude());
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
