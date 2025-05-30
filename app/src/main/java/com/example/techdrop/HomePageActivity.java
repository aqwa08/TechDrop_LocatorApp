package com.example.techdrop;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {

    Button btnLocate, btnEvent, btnprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        btnLocate = findViewById(R.id.btnLocate);
        btnEvent = findViewById(R.id.btnEvent);
        btnprofile = findViewById(R.id.btnProfile);

        btnLocate.setOnClickListener(v -> startActivity(new Intent(this, LocateListActivity.class)));
        btnprofile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileUserActivity.class)));
        btnEvent.setOnClickListener(v -> recreate());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}