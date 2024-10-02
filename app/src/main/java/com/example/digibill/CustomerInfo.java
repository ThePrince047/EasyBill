package com.example.digibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class CustomerInfo extends AppCompatActivity {

    private EditText edtCustomerName, edtEmail, edtNumber, edtAdd1, edtAdd2, edtCity, edtState, edtPin;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_info);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize EditText fields
        edtCustomerName = findViewById(R.id.edtLoginEmail);
        edtEmail = findViewById(R.id.edtEmail);
        edtNumber = findViewById(R.id.edtNumber);
        edtAdd1 = findViewById(R.id.edtAdd1);
        edtAdd2 = findViewById(R.id.edtAdd2);
        edtCity = findViewById(R.id.edtCity);
        edtState = findViewById(R.id.edtState);
        edtPin = findViewById(R.id.edtPin);

        // Save button click listener
        findViewById(R.id.btnSaveInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    saveinvoiceData();
                    Intent get = getIntent();
                    String nextInvoiceId = get.getStringExtra("nextInvoiceId");
                    Intent intent = new Intent(getApplicationContext(),Demo.class);
                    intent.putExtra("nextInvoiceId",String.valueOf(nextInvoiceId));
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validateFields() {
        String customerName = edtCustomerName.getText().toString().trim();

        if (customerName.isEmpty()) {
            edtCustomerName.setError("Customer name is required");
            return false;
        }
        return true;
    }

    private void saveinvoiceData() {
        // Retrieve text from EditText fields
        Intent intent = getIntent();
        Map<String, Object> invoiceData = (Map<String, Object>) intent.getSerializableExtra("invoiceData");
        String nextInvoiceId = intent.getStringExtra("nextInvoiceId");
        
        String customerName = edtCustomerName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phoneNumber = edtNumber.getText().toString().trim();
        String address1 = edtAdd1.getText().toString().trim();
        String address2 = edtAdd2.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String state = edtState.getText().toString().trim();
        String postalCode = edtPin.getText().toString().trim();
        
        invoiceData.put("customerName", customerName);
        invoiceData.put("email", email);
        invoiceData.put("phoneNumber", phoneNumber);
        invoiceData.put("address1", address1);
        invoiceData.put("address2", address2);
        invoiceData.put("city", city);
        invoiceData.put("state", state);
        invoiceData.put("postalCode", postalCode);

        // Create a DocumentReference to the specific path
        DocumentReference docRef = firestore.collection("EasyBill")
                .document(currentUser.getUid()) // Replace with actual user ID
                .collection("invoices")
                .document(nextInvoiceId); // Replace with actual document ID or generate a unique ID

        // Set the document with the data
        docRef.set(invoiceData)
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
