package com.example.easybill;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ManageInvoices extends AppCompatActivity {
    ImageView showInvoice,showInvoiceQR;
    EditText invoiceID;
    ImageButton searchInvoice;
    ProgressBar progressBar;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0, 0);
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_invoices);
        showInvoice = findViewById(R.id.imgShowInvoice);
        showInvoiceQR = findViewById(R.id.imgShowInvoiceQR);
        searchInvoice = findViewById(R.id.btnSearchInvoice);
        invoiceID = findViewById(R.id.edtSearchInvoice);
        progressBar = findViewById(R.id.progressBar);
        searchInvoice.setOnClickListener(v -> searchInvoiceWithID());

    }

    private void searchInvoiceWithID() {

        progressBar.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String invoiceIdValue = invoiceID.getText().toString().trim();

        if (invoiceIdValue.isEmpty()) {
            invoiceID.setError("Invoice ID is required");
            invoiceID.requestFocus();
            return;
        }

        // Reference to the invoice image in Firebase Storage
        StorageReference invoiceImageRef = storageRef.child(invoiceIdValue + "/" + invoiceIdValue + ".png");

        final long ONE_MEGABYTE = 1024 * 1024;


        // Download and display the invoice image
        invoiceImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // Display the Bitmap in the ImageView
                showInvoice.setImageBitmap(bitmap);

                // After loading the invoice image, load the QR code image
                loadQRCodeImage(invoiceIdValue);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ManageInvoices.this, "Please enter valid ID", Toast.LENGTH_SHORT).show();
                Log.e("ManageInvoices", "Please enter valid ID", exception);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadQRCodeImage(String invoiceIdValue) {
        // Reference to the QR code image in Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeImageRef = storageRef.child(invoiceIdValue + "/" + invoiceIdValue + "QR.png");

        final long ONE_MEGABYTE = 1024 * 1024;

        // Download and display the QR code image
        qrCodeImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a Bitmap

                Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                progressBar.setVisibility(View.GONE);
                // Display the QR code Bitmap in the ImageView
                showInvoiceQR.setImageBitmap(qrCodeBitmap);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ManageInvoices.this,"Please enter valid ID", Toast.LENGTH_SHORT).show();
                Log.e("ManageInvoices", "Error loading QR code image", exception);
                progressBar.setVisibility(View.GONE);
            }
        });
    }



}