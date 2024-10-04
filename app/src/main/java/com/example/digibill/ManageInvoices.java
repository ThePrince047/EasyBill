package com.example.digibill;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ManageInvoices extends AppCompatActivity {
    ImageView showInvoice;
    EditText invoiceID;
    ImageButton searchInvoice;
    ProgressBar progressBar;
    MaterialButton showQRButton,deleteInvoiceButton;
    String invoiceIdValue;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_invoices);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        showInvoice = findViewById(R.id.imgShowInvoice);
        searchInvoice = findViewById(R.id.btnSearchInvoice);
        invoiceID = findViewById(R.id.edtSearchInvoice);
        progressBar = findViewById(R.id.progressBar);
        showQRButton = findViewById(R.id.showQRButton);
        deleteInvoiceButton = findViewById(R.id.deleteInvoiceButton);

        searchInvoice.setOnClickListener(v -> searchInvoiceWithID());
        showQRButton.setOnClickListener(v -> showQRDialog());
        deleteInvoiceButton.setOnClickListener(v -> deleteInvoice());
    }

    private void searchInvoiceWithID() {

        progressBar.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        invoiceIdValue = invoiceID.getText().toString().trim();


        if (invoiceIdValue.isEmpty()) {
            invoiceID.setError("Invoice ID is required");
            invoiceID.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Reference to the invoice image in Firebase Storage
        StorageReference invoiceImageRef = storageRef.child(currentUser.getUid() +"_"+ invoiceIdValue + "/" + currentUser.getUid() +"_"+ invoiceIdValue + ".png");

        final long ONE_MEGABYTE = 1024 * 1024;

        // Download and display the invoice image
        invoiceImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // Display the Bitmap in the ImageView
                showInvoice.setImageBitmap(bitmap);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ManageInvoices.this, "Invoice not found", Toast.LENGTH_SHORT).show();
                Log.e("ManageInvoices", "Please enter valid ID", exception);
                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), ManageInvoices.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void deleteInvoice() {
        if (invoiceIdValue == null || invoiceIdValue.isEmpty()) {
            invoiceID.setError("Search invoice first");
            return;
        }

        // Initialize Firebase Storage and Firestore
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = currentUser.getUid();  // Assuming you have a method to get the current user ID

        // Reference to the invoice image and QR code in Firebase Storage
        StorageReference storageRef = storage.getReference();
        StorageReference invoiceImageRef = storageRef.child(userId + "_" + invoiceIdValue + "/" + userId + "_" + invoiceIdValue + ".png");
        StorageReference qrCodeImageRef = storageRef.child(userId + "_" + invoiceIdValue + "/" + userId + "_" + invoiceIdValue + "QR.png");

        // Delete the invoice image from Firebase Storage
        invoiceImageRef.delete().addOnSuccessListener(aVoid -> {
            // Delete the QR code image from Firebase Storage
            qrCodeImageRef.delete().addOnSuccessListener(aVoid1 -> {
                // Delete the invoice record from Firestore
                db.collection("EasyBill")
                        .document(currentUser.getUid())
                        .collection("invoices")
                        .document(String.valueOf(invoiceIdValue)).delete().addOnSuccessListener(aVoid2 -> {
                            Intent intent = new Intent(getApplicationContext(), ManageInvoices.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                    Toast.makeText(ManageInvoices.this, "Invoice deleted successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(exception -> {
                    Toast.makeText(ManageInvoices.this, "Failed to delete invoice record from Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("ManageInvoices", "Failed to delete invoice record from Firestore", exception);
                });
            }).addOnFailureListener(exception -> {
                Toast.makeText(ManageInvoices.this, "Failed to delete QR code", Toast.LENGTH_SHORT).show();
                Log.e("ManageInvoices", "Failed to delete QR code", exception);
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(ManageInvoices.this, "No invoice found", Toast.LENGTH_SHORT).show();
            Log.e("ManageInvoices", "Failed to delete invoice image", exception);
        });
    }



    private void showQRDialog() {
        if (invoiceIdValue == null || invoiceIdValue.isEmpty()) {
            invoiceID.setError("Search invoice first");
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_code);

        ImageView imgDialogQRCode = dialog.findViewById(R.id.imgDialogQRCode);

        // Load the QR code image
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeImageRef = storageRef.child(currentUser.getUid() +"_"+ invoiceIdValue + "/" + currentUser.getUid() +"_"+ invoiceIdValue + "QR.png");
        final long ONE_MEGABYTE = 1024 * 1024;

        // Download and display the QR code image
        qrCodeImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a Bitmap
                Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // Display the QR code Bitmap in the ImageView
                imgDialogQRCode.setImageBitmap(qrCodeBitmap);
                dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ManageInvoices.this, "No QR found", Toast.LENGTH_SHORT).show();
                Log.e("ManageInvoices", "Error loading QR code", exception);
            }
        });


    }
}