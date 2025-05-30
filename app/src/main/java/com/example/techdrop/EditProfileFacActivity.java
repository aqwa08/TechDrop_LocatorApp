package com.example.techdrop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileFacActivity extends AppCompatActivity {

    Button btnBack, btnSave, btnSetting, btnEvent;
    ImageButton btnGetLocation;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText facilityName, contactNumber, email, wasteTypes, location, description, operatingHour;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile_fac);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btnSave);
        btnGetLocation = findViewById(R.id.userLocation);
        btnSetting = findViewById(R.id.btn_setting);
        btnEvent = findViewById(R.id.btnEvent);

        facilityName = findViewById(R.id.facilityName);
        contactNumber = findViewById(R.id.contactNumber);
        email = findViewById(R.id.email);
        wasteTypes = findViewById(R.id.wasteTypes);
        location = findViewById(R.id.Location);
        description = findViewById(R.id.description);
        operatingHour = findViewById(R.id.operating_hour);

        btnBack.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivityFac.class)));
        btnSave.setOnClickListener(v -> saveFacilityData());
        btnGetLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        loadFacilityData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_fac), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadFacilityData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("facilityName")) {
                        facilityName.setHint(snapshot.child("facilityName").getValue(String.class));
                    }
                    if (snapshot.hasChild("phoneNumber")) {
                        contactNumber.setHint(snapshot.child("phoneNumber").getValue(String.class));
                    }
                    if (snapshot.hasChild("email")) {
                        email.setHint(snapshot.child("email").getValue(String.class));
                    }
                    if (snapshot.hasChild("wasteTypes")) {
                        wasteTypes.setHint(snapshot.child("wasteTypes").getValue(String.class));
                    }
                    if (snapshot.hasChild("address")) {
                        location.setHint(snapshot.child("address").getValue(String.class));
                    }
                    if (snapshot.hasChild("description")) {
                        description.setHint(snapshot.child("description").getValue(String.class));
                    }
                    if (snapshot.hasChild("operatingHour")) {
                        operatingHour.setHint(snapshot.child("operatingHour").getValue(String.class));
                    }
                } else {
                    Toast.makeText(EditProfileFacActivity.this, "No existing facility data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileFacActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                this.location.setText(address.getAddressLine(0));
                            } else {
                                Toast.makeText(this, "Unable to fetch address.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Geocoder service not available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFacilityData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        String newFacilityName = TextUtils.isEmpty(facilityName.getText().toString().trim()) ? facilityName.getHint().toString() : facilityName.getText().toString().trim();
        String newPhone = TextUtils.isEmpty(contactNumber.getText().toString().trim()) ? contactNumber.getHint().toString() : contactNumber.getText().toString().trim();
        String newEmail = TextUtils.isEmpty(email.getText().toString().trim()) ? email.getHint().toString() : email.getText().toString().trim();
        String newWasteTypes = TextUtils.isEmpty(wasteTypes.getText().toString().trim()) ? wasteTypes.getHint().toString() : wasteTypes.getText().toString().trim();
        String newLocation = TextUtils.isEmpty(location.getText().toString().trim()) ? location.getHint().toString() : location.getText().toString().trim();
        String newDescription = TextUtils.isEmpty(description.getText().toString().trim()) ? description.getHint().toString() : description.getText().toString().trim();
        String newOperatingHour = TextUtils.isEmpty(operatingHour.getText().toString().trim()) ? operatingHour.getHint().toString() : operatingHour.getText().toString().trim();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(newLocation, 1);
            double latitude = 0, longitude = 0;
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }

            // Update user-specific data
            userRef.child("facilityName").setValue(newFacilityName);
            userRef.child("phoneNumber").setValue(newPhone);
            userRef.child("email").setValue(newEmail);
            userRef.child("wasteTypes").setValue(newWasteTypes);
            userRef.child("address").setValue(newLocation);
            userRef.child("description").setValue(newDescription);
            userRef.child("operatingHour").setValue(newOperatingHour);
            userRef.child("latitude").setValue(latitude);
            userRef.child("longitude").setValue(longitude);

            // 🔥 Add/update facility in global "facilities"
            String facilityId = userId;  // Or generate unique key if multiple facilities per user
            DatabaseReference globalRef = FirebaseDatabase.getInstance().getReference("facilities").child(facilityId);

            Facility facility = new Facility(facilityId, userId, newFacilityName, newLocation, newDescription,
                    newEmail, latitude, longitude, newOperatingHour, newPhone, "Open", newWasteTypes, "f_user");

            globalRef.setValue(facility).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Facility profile updated globally and for user.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomePageActivityFac.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update global facility.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to geocode address.", Toast.LENGTH_SHORT).show();
        }
    }
}
