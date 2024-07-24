package com.example.easybill;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ListView invoiceListView = findViewById(R.id.invoiceListView);

        List<String> placeholderItems = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            placeholderItems.add("Invoice #" + i + " - $100.00");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.invoice_list_item,R.id.tvInvoiceNumber, placeholderItems);
        invoiceListView.setAdapter(adapter);
    }
}