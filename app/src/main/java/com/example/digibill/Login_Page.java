package com.example.digibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Login_Page extends AppCompatActivity implements Forgot_Password.ForgotPasswordListener {
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView lblRegister,lblForgotPass;
    ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID
        emailEditText = findViewById(R.id.edtLoginEmail);
        passwordEditText = findViewById(R.id.edtLoginPass);
        loginButton = findViewById(R.id.btnLogin);
        lblRegister = findViewById(R.id.lblRegister);
        lblForgotPass = findViewById(R.id.lblForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        lblRegister.setOnClickListener(v -> toRegisterPage());
        // Set up the login button click listener
        loginButton.setOnClickListener(v -> loginUser());
        lblForgotPass.setOnClickListener(v -> forgotPass());

    }

    private void forgotPass() {
        Forgot_Password forgotPasswordDialog = new Forgot_Password(this, this);
        forgotPasswordDialog.showForgotPassDialog();
   }

    private void toRegisterPage() {
        lblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register_Page.class);
                startActivity(intent);
            }
        });
    }


    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login_Page.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);  // Disable the login button

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("EasyBill").document(userId)
                                    .collection("company_info")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            progressBar.setVisibility(View.GONE);
                                            loginButton.setEnabled(true);  // Re-enable the login button

                                            if (task.isSuccessful()) {
                                                QuerySnapshot querySnapshot = task.getResult();
                                                if (!querySnapshot.isEmpty()) {
                                                    Toast.makeText(Login_Page.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(Login_Page.this, "Company info not found. Redirecting to Company Info page.", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(getApplicationContext(), CompanyInfo.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                Toast.makeText(Login_Page.this, "Error checking company info: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);  // Re-enable the login button

                            Toast.makeText(Login_Page.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @Override
    public void onForgotPassword(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Check if the email is not empty

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Email sent successfully
                            Toast.makeText(getApplicationContext(), "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to send the email
                            Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_SHORT).show();
                        }
                    });
    }

}
