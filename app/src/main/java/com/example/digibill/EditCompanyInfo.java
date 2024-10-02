package com.example.digibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
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

public class EditCompanyInfo extends AppCompatActivity {

    private EditText edtCompanyName, edtEmail, edtNumber, edtAdd1, edtAdd2, edtCity, edtState, edtPin;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), setting.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_company_info);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize EditText fields
        edtCompanyName = findViewById(R.id.edtLoginEmail);
        edtEmail = findViewById(R.id.edtEmail);
        edtNumber = findViewById(R.id.edtNumber);
        edtAdd1 = findViewById(R.id.edtAdd1);
        edtAdd2 = findViewById(R.id.edtAdd2);
        edtCity = findViewById(R.id.edtCity);
        edtState = findViewById(R.id.edtState);
        edtPin = findViewById(R.id.edtPin);
        loadCompanyInfo();

        findViewById(R.id.btnSaveInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    saveCompanyInfo();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    private boolean validateFields() {
        String companyName = edtCompanyName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phoneNumber = edtNumber.getText().toString().trim();
        String address1 = edtAdd1.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String state = edtState.getText().toString().trim();
        String postalCode = edtPin.getText().toString().trim();

        if (companyName.isEmpty()) {
            edtCompanyName.setError("Company name is required");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Valid email address is required");
            return false;
        }

        if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            edtNumber.setError("Phone number must be at least 10 digits");
            return false;
        }

        if (address1.isEmpty()) {
            edtAdd1.setError("Address line 1 is required");
            return false;
        }

        if (city.isEmpty()) {
            edtCity.setError("City is required");
            return false;
        }

        if (state.isEmpty()) {
            edtState.setError("State is required");
            return false;
        }

        if (postalCode.isEmpty() || postalCode.length() < 5) {
            edtPin.setError("Postal code must be at least 5 digits");
            return false;
        }

        return true;
    }

    private void loadCompanyInfo() {
        // Reference to the document in Firestore
        DocumentReference docRef = firestore.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("company_info")
                .document(currentUser.getUid());

        // Retrieve the document
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Document exists, fill the EditText fields
                edtCompanyName.setText(documentSnapshot.getString("companyName"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtNumber.setText(documentSnapshot.getString("phoneNumber"));
                edtAdd1.setText(documentSnapshot.getString("address1"));
                edtAdd2.setText(documentSnapshot.getString("address2"));
                edtCity.setText(documentSnapshot.getString("city"));
                edtState.setText(documentSnapshot.getString("state"));
                edtPin.setText(documentSnapshot.getString("postalCode"));
            } else {
                Toast.makeText(getApplicationContext(), "No company info found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle error
            Toast.makeText(getApplicationContext(), "Error loading company info: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void saveCompanyInfo() {
        // Retrieve text from EditText fields
        String companyName = edtCompanyName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phoneNumber = edtNumber.getText().toString().trim();
        String address1 = edtAdd1.getText().toString().trim();
        String address2 = edtAdd2.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String state = edtState.getText().toString().trim();
        String postalCode = edtPin.getText().toString().trim();

        // Create a map to hold the data
        Map<String, Object> companyInfo = new HashMap<>();
        companyInfo.put("companyName", companyName);
        companyInfo.put("email", email);
        companyInfo.put("phoneNumber", phoneNumber);
        companyInfo.put("address1", address1);
        companyInfo.put("address2", address2);
        companyInfo.put("city", city);
        companyInfo.put("state", state);
        companyInfo.put("postalCode", postalCode);

        // Create a DocumentReference to the specific path
        DocumentReference docRef = firestore.collection("EasyBill")
                .document(currentUser.getUid()) // Replace with actual user ID
                .collection("company_info")
                .document(currentUser.getUid()); // Replace with actual document ID or generate a unique ID

        // Set the document with the data
        docRef.set(companyInfo)
                .addOnSuccessListener(aVoid -> {
                    // Success
                    Toast.makeText(getApplicationContext(),"Company info successfully saved!",Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Toast.makeText(getApplicationContext(), "Error writing company info: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}