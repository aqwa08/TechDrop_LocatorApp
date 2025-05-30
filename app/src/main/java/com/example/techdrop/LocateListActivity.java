package com.example.techdrop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocateListActivity extends AppCompatActivity {

    Button btnViewOnMap, btnHome, btnEvent, btnprofile;
    ImageButton userLocation;
    EditText editTextLocation;
    RecyclerView recyclerView;
    FacilityAdapter facilityAdapter;
    List<Facility> facilityList;
    DatabaseReference databaseReference;

    FusedLocationProviderClient fusedLocationClient;
    LatLng selectedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locate_list);

        // UI components
        btnViewOnMap = findViewById(R.id.btnViewOnMap);
        btnHome = findViewById(R.id.btnHome);
        btnprofile = findViewById(R.id.btnProfile);
        userLocation = findViewById(R.id.userLocation);
        editTextLocation = findViewById(R.id.editTextLocation);
        recyclerView = findViewById(R.id.recyclerViewFacilities);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        userLocation.setOnClickListener(v -> getCurrentLocation());
        btnprofile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileUserActivity.class)));

        // 🔥 Add Enter key search to EditText
        editTextLocation.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchLocation();
                return true;
            }
            return false;
        });

        btnViewOnMap.setOnClickListener(v -> searchLocation());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        facilityList = new ArrayList<>();
        facilityAdapter = new FacilityAdapter(facilityList, this);
        recyclerView.setAdapter(facilityAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("facilities");

        // 🔥 Initial fetch to display facilities even without location
        fetchAndSortFacilities();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.locate_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void searchLocation() {
        String locationName = editTextLocation.getText().toString().trim();
        if (!TextUtils.isEmpty(locationName)) {
            GeocodingHelper.getCoordinatesFromAddress(locationName, latLng -> {
                if (latLng != null) {
                    selectedLocation = latLng;
                    fetchAndSortFacilities();
                    Toast.makeText(this, "Location found!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (selectedLocation != null) {
            fetchAndSortFacilities();
        } else {
            // Open map view even if no location entered
            startActivity(new Intent(this, LocateMapsActivity.class));
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                fetchAndSortFacilities();
                Toast.makeText(this, "Using current location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndSortFacilities() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                facilityList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Facility facility = dataSnapshot.getValue(Facility.class);
                    if (facility != null) {
                        facilityList.add(facility);
                    }
                }

                if (facilityList.isEmpty()) {
                    Toast.makeText(LocateListActivity.this, "No facilities found", Toast.LENGTH_SHORT).show();
                }

                if (selectedLocation != null) {
                    for (Facility f : facilityList) {
                        float[] results = new float[1];
                        Location.distanceBetween(selectedLocation.latitude, selectedLocation.longitude,
                                f.latitude, f.longitude, results);
                        double distanceKm = results[0] / 1000.0;  // meters to KM
                        f.description = String.format("%.1f KM", distanceKm);  // TEMP: store distance in description
                    }
                }

                Collections.sort(facilityList, (f1, f2) -> {
                    int openCompare = Boolean.compare(!f1.isOpen(), !f2.isOpen());
                    if (openCompare != 0) return openCompare;
                    if (selectedLocation != null) {
                        double d1 = parseDistance(f1.description);
                        double d2 = parseDistance(f2.description);
                        return Double.compare(d1, d2);
                    }
                    return 0;
                });

                facilityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocateListActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double parseDistance(String desc) {
        try {
            return Double.parseDouble(desc.replace(" KM", ""));
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }
}
