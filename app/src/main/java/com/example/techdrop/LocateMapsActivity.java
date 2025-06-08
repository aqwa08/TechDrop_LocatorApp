package com.example.techdrop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.*;

public class LocateMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    Button btnHome, btnList, btnprofile;
    EditText editTextLocation;
    ImageButton userLocation;

    private MapView mapView;
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference facilitiesRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locate_maps);

        userLocation = findViewById(R.id.userLocation);
        btnHome = findViewById(R.id.btnHome);
        btnList = findViewById(R.id.btnList);
        btnprofile = findViewById(R.id.btnProfile);
        editTextLocation = findViewById(R.id.editTextLocation);

        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        facilitiesRef = FirebaseDatabase.getInstance().getReference("facilities");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        btnList.setOnClickListener(v -> startActivity(new Intent(this, LocateListActivity.class)));
        btnprofile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileUserActivity.class)));

        userLocation.setOnClickListener(v -> showUserLocation());

        editTextLocation.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String locationName = editTextLocation.getText().toString().trim();
                if (TextUtils.isEmpty(locationName)) {
                    Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
                } else {
                    GeocodingHelper.getCoordinatesFromAddress(locationName, latLng -> runOnUiThread(() -> {
                        if (latLng != null && gMap != null) {
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            gMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                        } else {
                            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.maps_locate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        gMap.setMyLocationEnabled(true);

        // Add facility markers from both global and user-specific paths
        addAllFacilityMarkers();
    }

    private void addAllFacilityMarkers() {
        int[] totalMarkers = {0};  // Count of markers

        // Add markers from global "facilities"
        facilitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot facilitySnapshot : snapshot.getChildren()) {
                    Facility facility = facilitySnapshot.getValue(Facility.class);
                    if (facility != null && facility.latitude != 0 && facility.longitude != 0) {
                        LatLng location = new LatLng(facility.latitude, facility.longitude);
                        gMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(facility.facilityName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        totalMarkers[0]++;
                    }
                }
                Toast.makeText(LocateMapsActivity.this, "Added " + totalMarkers[0] + " markers from global facilities.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocateMapsActivity.this, "Failed to load global facilities.", Toast.LENGTH_SHORT).show();
            }
        });

        // Add markers from "Users/{userId}/facilities"
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot facilities = userSnapshot.child("facilities");
                    for (DataSnapshot facilitySnapshot : facilities.getChildren()) {
                        Facility facility = facilitySnapshot.getValue(Facility.class);
                        if (facility != null && facility.latitude != 0 && facility.longitude != 0) {
                            LatLng location = new LatLng(facility.latitude, facility.longitude);
                            gMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(facility.facilityName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            totalMarkers[0]++;
                        }
                    }
                }
                Toast.makeText(LocateMapsActivity.this, "tag for each facility " + totalMarkers[0] + " displayed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocateMapsActivity.this, "Failed to load user facilities.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        gMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Lifecycle methods
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}

