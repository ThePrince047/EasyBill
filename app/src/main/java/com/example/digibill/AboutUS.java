package com.example.digibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutUS extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), setting.class));
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_us);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
    }
}