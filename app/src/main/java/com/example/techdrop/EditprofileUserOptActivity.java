package com.example.techdrop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditprofileUserOptActivity extends AppCompatActivity {

    Button btnBack, btnSave;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // EditTexts for profile fields
    private EditText editUserName, editContactNumber, editEmail, editFullName, editLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editprofile_user_opt);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btnSave);

        editUserName = findViewById(R.id.UserName);
        editContactNumber = findViewById(R.id.ContactNumber);
        editEmail = findViewById(R.id.Email);
        editFullName = findViewById(R.id.full_name);
        editLocation = findViewById(R.id.Location);

        // Load existing data as hints
        loadUserData();

        btnBack.setOnClickListener(v -> startActivity(new Intent(this, EditProfileUserActivity.class)));

        btnSave.setOnClickListener(v -> saveUserData());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_user_opt), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Set hints based on existing data
                    if (snapshot.hasChild("username")) {
                        editUserName.setHint(snapshot.child("username").getValue(String.class));
                    }
                    if (snapshot.hasChild("phoneNumber")) {
                        editContactNumber.setHint(snapshot.child("phoneNumber").getValue(String.class));
                    }
                    if (snapshot.hasChild("email")) {
                        editEmail.setHint(snapshot.child("email").getValue(String.class));
                    }
                    if (snapshot.hasChild("fullName")) {
                        editFullName.setHint(snapshot.child("fullName").getValue(String.class));
                    }
                    if (snapshot.hasChild("address")) {
                        editLocation.setHint(snapshot.child("address").getValue(String.class));
                    }
                } else {
                    Toast.makeText(EditprofileUserOptActivity.this, "No existing user data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditprofileUserOptActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        // Get new input values (or keep existing hint if left empty)
        String newUsername = TextUtils.isEmpty(editUserName.getText().toString().trim()) ? editUserName.getHint().toString() : editUserName.getText().toString().trim();
        String newPhone = TextUtils.isEmpty(editContactNumber.getText().toString().trim()) ? editContactNumber.getHint().toString() : editContactNumber.getText().toString().trim();
        String newEmail = TextUtils.isEmpty(editEmail.getText().toString().trim()) ? editEmail.getHint().toString() : editEmail.getText().toString().trim();
        String newFullName = TextUtils.isEmpty(editFullName.getText().toString().trim()) ? editFullName.getHint().toString() : editFullName.getText().toString().trim();
        String newLocation = TextUtils.isEmpty(editLocation.getText().toString().trim()) ? editLocation.getHint().toString() : editLocation.getText().toString().trim();

        // Update values in the database
        userRef.child("username").setValue(newUsername);
        userRef.child("phoneNumber").setValue(newPhone);
        userRef.child("email").setValue(newEmail);
        userRef.child("fullName").setValue(newFullName);
        userRef.child("address").setValue(newLocation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditprofileUserOptActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditprofileUserOptActivity.this, EditProfileUserActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EditprofileUserOptActivity.this, "Profile update failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
