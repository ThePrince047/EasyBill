package com.example.easybill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Demo extends AppCompatActivity {

    private LinearLayout itemContainer;
    private TextView subTotalValue, taxValue, totalValue,date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        itemContainer = findViewById(R.id.itemContainer);
        subTotalValue = findViewById(R.id.subTotalValue);
        taxValue = findViewById(R.id.taxValue);
        totalValue = findViewById(R.id.totalValue);
        date = findViewById(R.id.invoiceDate);


// Get today's date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

// Set today's date to the TextView
        date.setText("Date: " + formattedDate);


        // Retrieve the list of invoices from the Intent
        ArrayList<AddInvoice> invoiceList = getIntent().getParcelableArrayListExtra("invoice_list");

        // Retrieve subtotal, tax, and grand total from the Intent
        double subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        double tax = getIntent().getDoubleExtra("tax", 0.0);
        double grandTotal = getIntent().getDoubleExtra("grandTotal", 0.0);

        if (invoiceList != null) {
            // Iterate over the list and add each item to the view
            for (AddInvoice invoice : invoiceList) {
                addItem(invoice.getItemName(), Integer.parseInt(invoice.getItemQuantity()), Double.parseDouble(invoice.getItemAmount()));
            }
        }

        // Set the values to TextViews
        subTotalValue.setText(String.format("₹ %.2f", subtotal));
        taxValue.setText(String.format("₹ %.2f", tax));
        totalValue.setText(String.format("₹ %.2f", grandTotal));
    }

    private void addItem(String description, int quantity, double price) {
        double total = quantity * price;

        LinearLayout itemRow = new LinearLayout(this);
        itemRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView descriptionView = new TextView(this);
        descriptionView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        descriptionView.setText(description);

        TextView quantityView = new TextView(this);
        quantityView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        quantityView.setText(String.valueOf(quantity));

        TextView priceView = new TextView(this);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        priceView.setText(String.format("₹ %.2f", price));

        TextView totalView = new TextView(this);
        totalView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        totalView.setText(String.format("₹ %.2f", total));

        itemRow.addView(descriptionView);
        itemRow.addView(quantityView);
        itemRow.addView(priceView);
        itemRow.addView(totalView);

        itemContainer.addView(itemRow);
    }
}
