package com.example.digibill;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class setting extends AppCompatActivity {

    private TextView editcompanyinfo, privacypolicy, aboutus, faq;
    private Button logout;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));

        logout = findViewById(R.id.btnLogOut);
        logout.setOnClickListener(v -> logoutUser());

        editcompanyinfo = findViewById(R.id.tvEditCompanyInfo);
        privacypolicy = findViewById(R.id.tvPrivacyPolicy);
        aboutus = findViewById(R.id.tvAboutUs);
        faq = findViewById(R.id.tvFAQ);

        editcompanyinfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EditCompanyInfo.class)));
        aboutus.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AboutUS.class)));
        faq.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FAQ.class)));

        privacypolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
    }

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(setting.this);  // Use `setting.this` as the context
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_privacy_policy, null);
        builder.setView(dialogView)
                .setTitle("Privacy Policy")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logoutUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), Login_Page.class));
        finish();
    }
}
