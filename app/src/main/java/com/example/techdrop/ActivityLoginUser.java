package com.example.techdrop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class ActivityLoginUser extends AppCompatActivity {

    Button btnUserlogin,btnRegisterUser;

    EditText editTextEmail,editTextPassword;

    TextView forgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(ActivityLoginUser.this, HomePageActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_login_user);

        btnUserlogin = findViewById(R.id.btn_user_login);
        btnRegisterUser = findViewById(R.id.btn_Register_user);

        forgotPassword = findViewById(R.id.forgotPass);

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(this,  ResetPasswordActivity.class)));

        btnRegisterUser.setOnClickListener(v -> startActivity(new Intent(this, RegisterUserActivity.class)));

        editTextEmail = findViewById(R.id.email_user);
        editTextPassword = findViewById(R.id.password_user);

        btnUserlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, username;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
                    Toast.makeText(ActivityLoginUser.this,"There are empty field",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //String user_id = mAuth.getCurrentUser().getEmail();
                                Toast.makeText(ActivityLoginUser.this,email+" logging in",Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ActivityLoginUser.this, HomePageActivity.class);
                                startActivity(intent);
                                finish();}
                            else{
                                Toast.makeText(ActivityLoginUser.this, "Log in Failed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_menu_user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}