package com.example.techdrop;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class HomePageActivityFac extends AppCompatActivity {

    Button btnEdit, btnSetting, btnStatus, btnEvent;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // TextViews for facility info
    private TextView facStatus, facilityName, contactNumber, email, wasteTypes, location, description, operatingHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page_fac);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize buttons
        btnEdit = findViewById(R.id.btnEdit);
        btnSetting = findViewById(R.id.btn_setting);
        btnStatus = findViewById(R.id.btnStatus);
        btnEvent = findViewById(R.id.btnEvent);

        // Initialize TextViews
        facStatus = findViewById(R.id.facStatus);
        facilityName = findViewById(R.id.facilityName);
        contactNumber = findViewById(R.id.contactNumber);
        email = findViewById(R.id.email);
        wasteTypes = findViewById(R.id.wasteTypes);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        operatingHour = findViewById(R.id.operating_hour);

        btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileFacActivity.class)));
        btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        btnStatus.setOnClickListener(v -> toggleFacilityStatus());

        loadFacilityData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page_fac), (v, insets) -> {
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
                if (snapshot.exists() && snapshot.hasChild("category")) {
                    String category = snapshot.child("category").getValue(String.class);
                    if ("f_user".equals(category)) {
                        facStatus.setText(snapshot.hasChild("status") ? snapshot.child("status").getValue(String.class) : getString(R.string.facility_operation_status));
                        facilityName.setText(snapshot.hasChild("facilityName") ? snapshot.child("facilityName").getValue(String.class) : getString(R.string.facility_name_homepage));
                        contactNumber.setText(snapshot.hasChild("phoneNumber") ? snapshot.child("phoneNumber").getValue(String.class) : getString(R.string.contact_number_homepage));
                        email.setText(snapshot.hasChild("email") ? snapshot.child("email").getValue(String.class) : getString(R.string.email_homepage));
                        wasteTypes.setText(snapshot.hasChild("wasteTypes") ? snapshot.child("wasteTypes").getValue(String.class) : getString(R.string.types_waste_homepage));
                        location.setText(snapshot.hasChild("address") ? snapshot.child("address").getValue(String.class) : getString(R.string.location_address_homepage));
                        description.setText(snapshot.hasChild("description") ? snapshot.child("description").getValue(String.class) : getString(R.string.facility_description_homepage));
                        operatingHour.setText(snapshot.hasChild("operatingHour") ? snapshot.child("operatingHour").getValue(String.class) : getString(R.string.operating_hour_detail));

                        btnStatus.setText(snapshot.hasChild("status") ? snapshot.child("status").getValue(String.class) : "Closed");
                    }
                } else {
                    Toast.makeText(HomePageActivityFac.this, "Facility data not found or incorrect category.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivityFac.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFacilityStatus() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);
        DatabaseReference globalRef = mDatabase.child("facilities").child(userId);  // 🔥 Assuming userId = facilityId

        userRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentStatus = snapshot.exists() ? snapshot.getValue(String.class) : "Closed";
                String newStatus = "Open".equalsIgnoreCase(currentStatus) ? "Closed" : "Open";

                // Update in both user and global paths
                userRef.child("status").setValue(newStatus);
                globalRef.child("status").setValue(newStatus)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                facStatus.setText(newStatus);
                                btnStatus.setText(newStatus);
                                Toast.makeText(HomePageActivityFac.this, "Facility status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(HomePageActivityFac.this, "Failed to update global status.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivityFac.this, "Error retrieving status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
