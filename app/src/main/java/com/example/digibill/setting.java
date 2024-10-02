package com.example.digibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class setting extends AppCompatActivity {

    private TextView editcompanyinfo,privacypolicy,aboutus,faq;
    private Button logout;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        logout= findViewById(R.id.btnLogOut);
        logout.setOnClickListener(v -> logoutUser());
        // Initialize Firebase Firestore
        editcompanyinfo=findViewById(R.id.tvEditCompanyInfo);
        privacypolicy=findViewById(R.id.tvPrivacyPolicy);
        aboutus=findViewById(R.id.tvAboutUs);
        faq=findViewById(R.id.tvFAQ);

        editcompanyinfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),EditCompanyInfo.class)));
        privacypolicy.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),PrivacyPolicy.class)));
        aboutus.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),AboutUS.class)));
        faq.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),FAQ.class)));
    }

    private void logoutUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), Login_Page.class));
        finish();
    }
}