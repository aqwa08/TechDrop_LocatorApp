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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileUserActivity extends AppCompatActivity {

    Button btnBack, btnEdit, btnLogout, btnDelete;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare TextViews
    private TextView txtUserName, txtContactNumber, txtEmail, txtFullName, txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile_user);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogOut);
        btnDelete = findViewById(R.id.btn_delete);  // Initialize delete button

        // Initialize TextViews
        txtUserName = findViewById(R.id.UserName);
        txtContactNumber = findViewById(R.id.ContactNumber);
        txtEmail = findViewById(R.id.Email);
        txtFullName = findViewById(R.id.full_name);
        txtLocation = findViewById(R.id.Location);

        // Load user data from Firebase
        loadUserData();

        btnBack.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditprofileUserOptActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(EditProfileUserActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(EditProfileUserActivity.this, ActivityLoginUser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> deleteUserAccount());  // 🔥 Delete account logic

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_user), (v, insets) -> {
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
                    if (snapshot.hasChild("username")) {
                        txtUserName.setText(snapshot.child("username").getValue(String.class));
                    }
                    if (snapshot.hasChild("phoneNumber")) {
                        txtContactNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                    }
                    if (snapshot.hasChild("email")) {
                        txtEmail.setText(snapshot.child("email").getValue(String.class));
                    }
                    if (snapshot.hasChild("fullName")) {
                        txtFullName.setText(snapshot.child("fullName").getValue(String.class));
                    }
                    if (snapshot.hasChild("address")) {
                        txtLocation.setText(snapshot.child("address").getValue(String.class));
                    }
                } else {
                    Toast.makeText(EditProfileUserActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileUserActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 Delete User Account Method
    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("Users").child(userId);

            // 🔥 Delete user data from Realtime Database
            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // 🔥 Delete Authentication account
                    user.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Toast.makeText(EditProfileUserActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditProfileUserActivity.this, ActivityLoginUser.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(EditProfileUserActivity.this, "Failed to delete account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EditProfileUserActivity.this, "Failed to delete user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}
