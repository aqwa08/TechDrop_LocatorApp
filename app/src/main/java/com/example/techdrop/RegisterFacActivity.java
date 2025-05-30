package com.example.techdrop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterFacActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextFacilityName, editTextPhoneNumber, editTextAddress;
    private Button btnSaveRegister, btnReturnLogFac;
    private ImageButton btnGetLocation;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_fac);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements
        editTextEmail = findViewById(R.id.email_fac);
        editTextPassword = findViewById(R.id.password_fac);
        editTextFacilityName = findViewById(R.id.facility_name);
        editTextPhoneNumber = findViewById(R.id.facility_phone_num);
        editTextAddress = findViewById(R.id.Location_details);
        btnSaveRegister = findViewById(R.id.btn_Register_fac_saved);
        btnReturnLogFac = findViewById(R.id.btn_return_log_fac);
        btnGetLocation = findViewById(R.id.userLocation);

        // Set listeners
        btnReturnLogFac.setOnClickListener(v -> startActivity(new Intent(this, ActivityLoginFacility.class)));

        btnGetLocation.setOnClickListener(v -> fetchCurrentLocation());

        btnSaveRegister.setOnClickListener(v -> registerFacility());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_fac_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Use Geocoder to get address from coordinates
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                editTextAddress.setText(address.getAddressLine(0));
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

    private void registerFacility() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String facilityName = editTextFacilityName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(facilityName)
                || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Geocode the address to get latitude and longitude
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList.isEmpty()) {
                Toast.makeText(this, "Invalid address.", Toast.LENGTH_SHORT).show();
                return;
            }
            Address location = addressList.get(0);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Proceed with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            FacilityUser facilityUser = new FacilityUser(facilityName, email, phoneNumber, address, latitude, longitude, "f_user");
                            mDatabase.child("Users").child(userId).setValue(facilityUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                            // Navigate to another activity if needed
                                        } else {
                                            Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoding failed.", Toast.LENGTH_SHORT).show();
        }
    }

    // FacilityUser model class
    public static class FacilityUser {
        public String facilityName;
        public String email;
        public String phoneNumber;
        public String address;
        public double latitude;
        public double longitude;
        public String category;

        public FacilityUser() {
            // Default constructor required for calls to DataSnapshot.getValue(FacilityUser.class)
        }

        public FacilityUser(String facilityName, String email, String phoneNumber, String address, double latitude, double longitude, String category) {
            this.facilityName = facilityName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category;
        }
    }
}
