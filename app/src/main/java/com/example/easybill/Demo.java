package com.example.easybill;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Demo extends AppCompatActivity {

    private LinearLayout itemContainer;
    private TextView subTotalValue, taxValue, totalValue;
    private double subTotal = 0.0;
    private double taxRate = 0.05;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        itemContainer = findViewById(R.id.itemContainer);
        subTotalValue = findViewById(R.id.subTotalValue);
        taxValue = findViewById(R.id.taxValue);
        totalValue = findViewById(R.id.totalValue);

        // Example of adding items
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 1", 2, 10.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);
        addItem("Item 2", 1, 20.00);

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
        descriptionView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        ));
        descriptionView.setText(description);

        TextView quantityView = new TextView(this);
        quantityView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        quantityView.setText(String.valueOf(quantity));

        TextView priceView = new TextView(this);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        priceView.setText(String.format("$%.2f", price));

        TextView totalView = new TextView(this);
        totalView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        totalView.setText(String.format("$%.2f", total));

        itemRow.addView(descriptionView);
        itemRow.addView(quantityView);
        itemRow.addView(priceView);
        itemRow.addView(totalView);

        itemContainer.addView(itemRow);

        updateTotals(total);
    }

    private void updateTotals(double itemTotal) {
        subTotal += itemTotal;
        double tax = subTotal * taxRate;
        double total = subTotal + tax;

        subTotalValue.setText(String.format("$%.2f", subTotal));
        taxValue.setText(String.format("$%.2f", tax));
        totalValue.setText(String.format("$%.2f", total));
    }
}
