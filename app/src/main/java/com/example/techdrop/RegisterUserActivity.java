package com.example.techdrop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUserActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextUserName;
    Button btnSaveRegister, btnReturnLogUser;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnReturnLogUser = findViewById(R.id.btn_return_log_user);
        btnReturnLogUser.setOnClickListener(v -> startActivity(new Intent(this, ActivityLoginUser.class)));

        editTextEmail = findViewById(R.id.email_user);
        editTextPassword = findViewById(R.id.password_user);
        editTextUserName = findViewById(R.id.User_name);
        btnSaveRegister = findViewById(R.id.btn_Register_user_saved);

        btnSaveRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, username;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                username = editTextUserName.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
                    Toast.makeText(RegisterUserActivity.this, "There are empty fields", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                // 🔥 Create User object with category "n_user"
                                User newUser = new User(username, email, "n_user");

                                mDatabase.child("Users").child(userId).setValue(newUser)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(RegisterUserActivity.this, "Registration Complete", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(RegisterUserActivity.this, "Username Save Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(RegisterUserActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_user_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 🔥User model class
    public static class User {
        public String username;
        public String email;
        public String category;

        public User() {
        }

        public User(String username, String email, String category) {
            this.username = username;
            this.email = email;
            this.category = category;
        }
    }
}
