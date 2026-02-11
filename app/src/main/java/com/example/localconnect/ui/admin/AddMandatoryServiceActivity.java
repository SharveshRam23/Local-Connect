package com.example.localconnect.ui.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.data.dao.MandatoryServiceDao;
import com.example.localconnect.databinding.ActivityAddMandatoryServiceBinding;
import com.example.localconnect.model.MandatoryService;
import com.example.localconnect.util.ImageUtil;
import com.example.localconnect.util.LocationHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddMandatoryServiceActivity extends AppCompatActivity {

    private ActivityAddMandatoryServiceBinding binding;
    private String serviceIdToEdit = null;
    private String encodedImage = null;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    private androidx.activity.result.ActivityResultLauncher<Intent> mapPickerLauncher;

    @javax.inject.Inject
    MandatoryServiceDao serviceDao;

    @javax.inject.Inject
    FirebaseFirestore firestore;

    private com.google.android.material.textfield.TextInputLayout tilCustomCategory;
    private com.google.android.material.textfield.TextInputEditText etCustomCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMandatoryServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize custom category views manually as they were added to layout but might not be in binding yet if not re-generated
        // Or if using ViewBinding, we can access them directly if the binding class is updated. 
        // Assuming binding is updated or we use findViewById for new views if binding class isn't re-generated in this environment logic.
        // For safety in this environment, I'll use findViewById for the new views to be sure, or rely on binding if I could trust it's regenerated.
        // Let's rely on finding them by ID to be safe with the binding object potentially being stale.
        tilCustomCategory = findViewById(R.id.tilCustomCategory);
        etCustomCategory = findViewById(R.id.etCustomCategory);

        setupCategorySpinner();
        setupMapPickerLauncher();

        serviceIdToEdit = getIntent().getStringExtra("service_id");
        if (serviceIdToEdit != null) {
            binding.tvTitle.setText("Edit Mandatory Service");
            loadServiceData(serviceIdToEdit);
        }

        binding.btnPickLocation.setOnClickListener(v -> checkLocationPermission());
        binding.btnPickOnMap.setOnClickListener(v -> launchMapPicker());
        binding.btnUploadImage.setOnClickListener(v -> openImagePicker());
        binding.btnSaveService.setOnClickListener(v -> saveService());
    }

    private void setupMapPickerLauncher() {
        mapPickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("lat", 0.0);
                        double lng = result.getData().getDoubleExtra("lng", 0.0);
                        binding.etLatitude.setText(String.valueOf(lat));
                        binding.etLongitude.setText(String.valueOf(lng));
                        Toast.makeText(this, "Location selected: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchMapPicker() {
        Intent intent = new Intent(this, PickLocationActivity.class);
        // Pass current coordinates if available
        String latStr = binding.etLatitude.getText().toString().trim();
        String lngStr = binding.etLongitude.getText().toString().trim();
        if (!latStr.isEmpty() && !lngStr.isEmpty()) {
            try {
                intent.putExtra("lat", Double.parseDouble(latStr));
                intent.putExtra("lng", Double.parseDouble(lngStr));
            } catch (NumberFormatException e) {
                // Use default coordinates
            }
        }
        mapPickerLauncher.launch(intent);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        binding.spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("OTHER".equalsIgnoreCase(selected)) {
                    tilCustomCategory.setVisibility(android.view.View.VISIBLE);
                } else {
                    tilCustomCategory.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadServiceData(String id) {
        firestore.collection("mandatory_services").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    MandatoryService service = documentSnapshot.toObject(MandatoryService.class);
                    if (service != null) {
                        populateFields(service);
                    }
                });
    }

    private void populateFields(MandatoryService service) {
        binding.etServiceName.setText(service.name);
        binding.etAddress.setText(service.address);
        binding.etPincode.setText(service.pincode);
        binding.etLatitude.setText(String.valueOf(service.latitude));
        binding.etLongitude.setText(String.valueOf(service.longitude));
        binding.etPhonePrimary.setText(service.phonePrimary);
        binding.etWorkingHours.setText(service.workingHours);
        binding.switch24x7.setChecked(service.is24x7);
        binding.switchEmergency.setChecked(service.isEmergency);

        if (service.imageUrl != null && service.imageUrl.length() > 500) {
            encodedImage = service.imageUrl;
            binding.ivServiceImage.setImageBitmap(ImageUtil.fromBase64(encodedImage));
        }

        // Set spinner selection
        ArrayAdapter adapter = (ArrayAdapter) binding.spinnerCategory.getAdapter();
        
        // Check if category is in the standard list
        int pos = adapter.getPosition(service.category);
        if (pos >= 0) {
            binding.spinnerCategory.setSelection(pos);
            tilCustomCategory.setVisibility(android.view.View.GONE);
        } else {
            // It's a custom category, select "OTHER" and fill text box
            int otherPos = adapter.getPosition("OTHER");
            if (otherPos < 0) otherPos = adapter.getPosition("Other"); // Case safety
            if (otherPos >= 0) {
                binding.spinnerCategory.setSelection(otherPos);
                tilCustomCategory.setVisibility(android.view.View.VISIBLE);
                etCustomCategory.setText(service.category);
            }
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        LocationHelper helper = new LocationHelper(this);
        helper.getCurrentPincode(this, new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationFound(String pincode) {
                binding.etPincode.setText(pincode);
                // In a real app, you'd get lat/lng from the Location object in LocationHelper
                // For now, let's assume the user can also see it or it's partially filled.
                Toast.makeText(AddMandatoryServiceActivity.this, "Pincode detected: " + pincode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddMandatoryServiceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                encodedImage = ImageUtil.toBase64(bitmap);
                binding.ivServiceImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveService() {
        String name = binding.etServiceName.getText().toString().trim();
        String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();
        String finalCategory = selectedCategory;
        
        if ("OTHER".equalsIgnoreCase(selectedCategory) && etCustomCategory != null) {
            finalCategory = etCustomCategory.getText().toString().trim();
            if (TextUtils.isEmpty(finalCategory)) {
                tilCustomCategory.setError("Please specify category");
                return;
            } else {
                tilCustomCategory.setError(null);
            }
        }

        String address = binding.etAddress.getText().toString().trim();
        String pincode = binding.etPincode.getText().toString().trim();
        String latStr = binding.etLatitude.getText().toString().trim();
        String lngStr = binding.etLongitude.getText().toString().trim();
        String phone = binding.etPhonePrimary.getText().toString().trim();
        String hours = binding.etWorkingHours.getText().toString().trim();
        boolean is24x7 = binding.switch24x7.isChecked();
        boolean isEmergency = binding.switchEmergency.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pincode) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) {
            Toast.makeText(this, "Please fill required fields (Name, Pincode, Lat, Lng)", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);

        String id = serviceIdToEdit != null ? serviceIdToEdit : UUID.randomUUID().toString();
        MandatoryService service = new MandatoryService(id, name, finalCategory, address, pincode, lat, lng,
                phone, null, hours, is24x7, isEmergency, true, encodedImage, System.currentTimeMillis());

        firestore.collection("mandatory_services").document(id)
                .set(service)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        serviceDao.insert(service);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Service Saved Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
