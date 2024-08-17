package com.example.easybill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easybill.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register_Page extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView lblLogin;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),Login_Page.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));

        edtEmail = findViewById(R.id.edtLoginEmail);
        edtPassword = findViewById(R.id.edtPass);
        edtConfirmPassword = findViewById(R.id.edtConfirmPass);
        btnRegister = findViewById(R.id.btnRegister);
        lblLogin = findViewById(R.id.lblLogin);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());
        lblLogin.setOnClickListener(v -> toLoginPage());
    }

    private void toLoginPage() {
        Intent intent = new Intent(getApplicationContext(),Login_Page.class);
        startActivity(intent);
    }


    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(Register_Page.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Register_Page.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email);
                            Intent intent = new Intent(getApplicationContext(), CompanyInfo.class);
                            startActivity(intent);
                        }
                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(Register_Page.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email) {
        User user = new User(email,0);
        firestore.collection("EasyBill").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Register_Page.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to another activity or perform any other action
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register_Page.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    public class User {
        private String email;
        private int invoiceId;

        // Default constructor required for Firestore serialization
        public User() {
        }

        // Constructor with parameters
        public User(String email, int invoiceId) {
            this.email = email;
            this.invoiceId = invoiceId;
        }

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(int invoiceId) {
            this.invoiceId = invoiceId;
        }
    }

}
