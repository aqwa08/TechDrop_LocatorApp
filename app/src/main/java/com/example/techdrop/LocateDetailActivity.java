package com.example.techdrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocateDetailActivity extends AppCompatActivity {

    Button btnBack, btnNavigate;
    TextView facilityName, facDis, operatingHour, contactNumber, email, wasteTypes, description;
    DatabaseReference databaseReference;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locate_detail);

        btnBack = findViewById(R.id.btn_back);
        btnNavigate = findViewById(R.id.btn_navigate);
        facilityName = findViewById(R.id.facility_name);
        facDis = findViewById(R.id.FacDis);
        operatingHour = findViewById(R.id.operatingHour);
        contactNumber = findViewById(R.id.contactNum);
        email = findViewById(R.id.email);
        wasteTypes = findViewById(R.id.ewasteType);
        description = findViewById(R.id.descriptionArea);

        btnBack.setOnClickListener(v -> startActivity(new Intent(this, LocateListActivity.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.facility_detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String facilityId = getIntent().getStringExtra("facilityId");
        if (facilityId == null) {
            Toast.makeText(this, "No facility selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("facilities").child(facilityId);
        loadFacilityDetails();

        btnNavigate.setOnClickListener(v -> openMapsNavigation());
    }

    private void loadFacilityDetails() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                Facility f = snapshot.getValue(Facility.class);
                if (f != null) {
                    facilityName.setText(f.facilityName);
                    facDis.setText(String.format("~%.1f KM", parseDistance(f.description)));
                    operatingHour.setText("Operating Hours: " + f.operatingHour);
                    contactNumber.setText("Contact: " + f.phoneNumber);
                    email.setText("Email: " + f.email);
                    wasteTypes.setText("Waste Types: " + f.wasteTypes);
                    description.setText(f.description);
                    latitude = f.latitude;
                    longitude = f.longitude;
                }
            } else {
                Toast.makeText(this, "Facility data not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openMapsNavigation() {
        if (latitude != 0 && longitude != 0) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Location unavailable for navigation", Toast.LENGTH_SHORT).show();
        }
    }

    private double parseDistance(String desc) {
        try { return Double.parseDouble(desc.replace(" KM", "")); }
        catch (Exception e) { return 0.0; }
    }
}
