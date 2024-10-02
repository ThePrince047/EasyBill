package com.example.digibill;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class Demo extends AppCompatActivity {

    private LinearLayout itemContainer;
    private TextView subTotalValue, taxValue, totalValue, date;
    private TextView companyName, companyAddress, companyPhoneEmail,invoiceIdNo,customerName,customerAdd,customerPhoneEmail;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private String invoiceId; // Changed from final to non-final
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        invoiceId = intent.getStringExtra("nextInvoiceId");

        // Initialize the views
        itemContainer = findViewById(R.id.itemContainer);
        subTotalValue = findViewById(R.id.subTotalValue);
        taxValue = findViewById(R.id.taxValue);
        totalValue = findViewById(R.id.totalValue);
        date = findViewById(R.id.invoiceDate);
        companyName = findViewById(R.id.companyName);
        companyAddress = findViewById(R.id.companyAddress);
        companyPhoneEmail = findViewById(R.id.companyPhoneEmail);
        customerName = findViewById(R.id.customerName);
        customerAdd = findViewById(R.id.customerAddress);
        customerPhoneEmail = findViewById(R.id.customerPhoneEmail);
        // Get today's date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        // Set today's date to the TextView
        date.setText("Date: " + formattedDate);

        // Check if invoiceId is null
        if (invoiceId != null) {
            // Fetch invoice data from Firestore
            fetchInvoiceData();
        } else {
            Toast.makeText(this, "No invoice ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        super.onBackPressed();
    }

    private void fetchInvoiceData() {
        db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("invoices")
                .document(String.valueOf(invoiceId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Extract basic fields
                            String date1 = document.getString("date");
                            Double grandTotal = document.getDouble("grandTotal");
                            Double subtotal = document.getDouble("subtotal");
                            Double tax = document.getDouble("tax");
                            String custName = document.getString("customerName");
                            String add1 = document.getString("address1");
                            String add2 = document.getString("address2");
                            String city = document.getString("city");
                            String state = document.getString("state");
                            String postalCode = document.getString("postalCode");
                            String phoneNumber = document.getString("phoneNumber");
                            String email = document.getString("email");



                            // Ensure that these fields are not null
                            String displayDate = (date != null) ? date1 : "No date provided";
                            double grandTotalValue = (grandTotal != null) ? grandTotal : 0.0;
                            double subtotalValue = (subtotal != null) ? subtotal : 0.0;
                            double taxValue1 = (tax != null) ? tax : 0.0;

                            if (add1 != null && !add1.isEmpty()) {
                                customerAdd.setText(add1);
                            }
                            if (add2 != null && !add2.isEmpty()) {
                                customerAdd.append("\n" + add2);
                            }
                            if (city != null && !city.isEmpty()) {
                                customerAdd.append("\n" + city);
                            }
                            if (state != null && !state.isEmpty()) {
                                customerAdd.append("," + state);
                            }
                            if (postalCode != null && !postalCode.isEmpty()) {
                                customerAdd.append("," + postalCode);
                            }

                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                customerPhoneEmail.setText("Phone no: " + phoneNumber);
                            }
                            if (email != null && !email.isEmpty()) {
                                if (!phoneNumber.isEmpty()) {
                                    customerPhoneEmail.append("\n");
                                }
                                customerPhoneEmail.append("Email: " + email);
                            }

                            // Set the basic fields

                            customerName.setText("Customer Name : "+ custName);
                            date.setText("Date: " + displayDate);
                            subTotalValue.setText(String.format("₹ %.2f", subtotalValue));
                            taxValue.setText(String.format("₹ %.2f", taxValue1));
                            totalValue.setText(String.format("₹ %.2f", grandTotalValue));


                            // Process items
                            for (int i = 0; i < 100; i++) {  // Assuming a maximum of 100 items
                                Map<String, Object> itemMap = (Map<String, Object>) document.get("item" + i);
                                if (itemMap == null) break;

                                String itemName = (String) itemMap.get("itemName");
                                String itemQuantity = (String) itemMap.get("itemQuantity");
                                String itemAmount = (String) itemMap.get("itemAmount");
                                String itemTax = (String) itemMap.get("itemTax");

                                // Default values if any field is missing
                                itemName = (itemName != null) ? itemName : "No name provided";
                                itemQuantity = (itemQuantity != null) ? itemQuantity : "0";
                                itemAmount = (itemAmount != null) ? itemAmount : "0";
                                itemTax = (itemTax != null) ? itemTax : "0";

                                // Convert strings to numerical values
                                int quantity = Integer.parseInt(itemQuantity);
                                double amount = Double.parseDouble(itemAmount);
                                double taxAmount = Double.parseDouble(itemTax);

                                // Add item to the view
                                addItem(itemName, quantity, amount);
                            }

                            // Fetch and set company info
                            fetchCompanyInfo();
                        } else {
                            Toast.makeText(Demo.this, "No such invoice", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Demo.this, "Error getting invoice data", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void addItem(String description, int quantity, double price) {
        double total = quantity * price;

        LinearLayout itemRow = new LinearLayout(this);
        itemRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemRow.setOrientation(LinearLayout.HORIZONTAL);
        invoiceIdNo = findViewById(R.id.invoiceNumber);
        invoiceIdNo.setText("Invoice ID : INV#"+invoiceId);

        TextView descriptionView = new TextView(this);
        descriptionView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        descriptionView.setText(description);
        descriptionView.setTextColor(Color.BLACK);

        TextView quantityView = new TextView(this);
        quantityView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        quantityView.setText(String.valueOf(quantity));
        quantityView.setTextColor(Color.BLACK);
        quantityView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView priceView = new TextView(this);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        priceView.setText(String.format("₹ %.2f", price));
        priceView.setTextColor(Color.BLACK);
        priceView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView totalView = new TextView(this);
        totalView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        totalView.setText(String.format("₹ %.2f", total));
        totalView.setTextColor(Color.BLACK);
        totalView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        itemRow.addView(descriptionView);
        itemRow.addView(quantityView);
        itemRow.addView(priceView);
        itemRow.addView(totalView);

        itemContainer.addView(itemRow);
    }

    private void fetchCompanyInfo() {
        db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("company_info")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("companyName");
                            String address1 = document.getString("address1");
                            String address2 = document.getString("address2");
                            String city = document.getString("city");
                            String state = document.getString("state");
                            String postalCode = document.getString("postalCode");
                            String phoneNumber = document.getString("phoneNumber");
                            String email = document.getString("email");



                            // Set data to the TextViews
                            companyName.setText(name);
                            companyAddress.setText(address1 + ",\n" + address2 + ",\n" + city + ", " + state + ", " + postalCode);
                            companyPhoneEmail.setText("Phone: " + phoneNumber + "\nEmail: " + email );


                            // Capture and upload snapshot after setting company info
                            captureAndUploadSnapshot();
                        } else {
                            // Handle case where document doesn't exist
                        }
                    } else {
                        // Handle errors
                    }
                });
    }

    private void captureAndUploadSnapshot() {
        // Capture the RelativeLayout as a bitmap
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout); // Adjust the ID as needed
        Bitmap bitmap = captureView(relativeLayout);
        byte[] data = bitmapToByteArray(bitmap);

        // Upload to Firebase Storage
        String fileName = currentUser.getUid() +"_"+ invoiceId + ".png";
        String fileNameWithoutEx = currentUser.getUid() +"_"+ invoiceId;
        String fileQR = currentUser.getUid() +"_"+ invoiceId + "QR.png";
        uploadToFirebaseStorage(data, fileName,fileNameWithoutEx,fileQR);
    }

    private void uploadToFirebaseStorage(byte[] data, String fileName, String fileNameWithoutEx, String fileQR) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(fileNameWithoutEx).child(fileName);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                // Generate QR code from the download URL
                Bitmap qrCodeBitmap = generateQRCode(downloadUrl);

                // Upload QR code bitmap as a separate file
                uploadQRCodeBitmap(qrCodeBitmap, fileNameWithoutEx, fileQR);

                // Set the QR code image to ImageView after uploading
                ImageView qrCodeImageView = findViewById(R.id.imgQr);
                progressBar.setVisibility(View.GONE);
                qrCodeImageView.setImageBitmap(qrCodeBitmap);

            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(Demo.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadQRCodeBitmap(Bitmap qrCodeBitmap, String fileNameWithoutEx, String fileQR) {
        byte[] qrCodeData = bitmapToByteArray(qrCodeBitmap);
        StorageReference storageRef = storage.getReference();
        StorageReference qrImageRef = storageRef.child(fileNameWithoutEx).child(fileQR);

        UploadTask qrUploadTask = qrImageRef.putBytes(qrCodeData);
        qrUploadTask.addOnSuccessListener(taskSnapshot -> {
            qrImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String qrDownloadUrl = uri.toString();
                // Optionally, you can handle the QR code URL (e.g., store in Firestore or display)
                Log.d("QR Code URL", qrDownloadUrl);
//                Toast.makeText(Demo.this, "QR code uploaded successfully", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(Demo.this, "QR code upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap generateQRCode(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 1024, 1024);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
