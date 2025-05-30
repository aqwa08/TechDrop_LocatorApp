package com.example.techdrop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    Button btnUserMenu,btnFacMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnUserMenu = findViewById(R.id.btn_user_menu);
        btnFacMenu = findViewById(R.id.btn_facility_menu);

        btnUserMenu.setOnClickListener(v -> startActivity(new Intent(this, ActivityLoginUser.class)));
        btnFacMenu.setOnClickListener(v -> startActivity(new Intent(this, ActivityLoginFacility.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}