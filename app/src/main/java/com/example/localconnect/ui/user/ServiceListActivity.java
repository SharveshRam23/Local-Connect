package com.example.localconnect.ui.user;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {

    private RecyclerView rvProviders;
    private Spinner spinnerCategory;
    private EditText etFilterPincode;
    private Button btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        rvProviders = findViewById(R.id.rvProviders);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etFilterPincode = findViewById(R.id.etFilterPincode);
        btnFilter = findViewById(R.id.btnFilter);

        rvProviders.setLayoutManager(new LinearLayoutManager(this));

        setupSpinner();

        btnFilter.setOnClickListener(v -> filterProviders());

        loadAllProviders();
    }

    private void setupSpinner() {
        // Example categories
        String[] categories = { "All", "Plumber", "Electrician", "Carpenter", "Maid" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadAllProviders() {
        // TODO: Load all approved providers from DB
    }

    private void filterProviders() {
        String category = spinnerCategory.getSelectedItem().toString();
        String pincode = etFilterPincode.getText().toString().trim();
        // TODO: Filter logic using DAO
    }
}
