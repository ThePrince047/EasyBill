package com.example.easybill;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class invoice_gen_page extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    Button button;
    EditText user, amount, quantity;
    String partyName;
    Double billValue;
    ListView listView;
    List<AddInvoice> invoicesList;
    CardAdapter1 cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_gen_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        user = findViewById(R.id.edtItemName);
        amount = findViewById(R.id.edtAmount);
        quantity = findViewById(R.id.edtQuantity);

        button = findViewById(R.id.btnAddInvoice);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fabButton);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter1(this, invoicesList);
        listView.setAdapter(cardAdapter);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));

        bottomNavigationView.setSelectedItemId(R.id.nav_new_invoice);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(getApplicationContext(), reports_page.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        button.setOnClickListener(v -> {
            String itemName = user.getText().toString();
            String itemAmount = amount.getText().toString();
            String itemQuantity = quantity.getText().toString();

            if (!itemName.isEmpty() && !itemAmount.isEmpty() && !itemQuantity.isEmpty()) {
                // Create new AddInvoice object and add it to the list
                AddInvoice newInvoice = new AddInvoice(itemName, itemQuantity, itemAmount);
                invoicesList.add(newInvoice);

                // Notify the adapter that the data set has changed
                cardAdapter.notifyDataSetChanged();
            }
        });
    }
}




class CardAdapter1 extends ArrayAdapter<AddInvoice> {

    public CardAdapter1(Context context, List<AddInvoice> invoices) {
        super(context, 0, invoices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.added_item, parent, false);
        }

        // Ensure the position is valid
        if (position < getCount()) {
            AddInvoice invoice = getItem(position);

            if (invoice != null) {
                // Initialize EditTexts
                EditText itemName = convertView.findViewById(R.id.edtItemName);
                EditText quantity = convertView.findViewById(R.id.edtQuantity);
                EditText amount = convertView.findViewById(R.id.edtAmount);

                // Set data to EditTexts
                if (itemName != null) {
                    itemName.setText(invoice.getName());
                }
                if (quantity != null) {
                    quantity.setText(invoice.getQuantity());
                    // Save original quantity
                    final String originalQuantity = invoice.getQuantity();

                    // Add FocusChangeListener for quantity
                    quantity.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            String newQuantityText = quantity.getText().toString();
                            if (newQuantityText.isEmpty()) {
                                newQuantityText = "0";
                            }

                            int newQuantity = Integer.parseInt(newQuantityText);

                            // Only remove item if the quantity is set to 0 and was originally non-zero
                            if (newQuantity == 0 && !originalQuantity.equals("0")) {
                                remove(invoice);
                                notifyDataSetChanged();
                            } else if (newQuantity != 0) {
                                // Update the invoice's quantity if it is not 0
                                invoice.setQuantity(newQuantityText);
                            }
                        }
                    });
                }
                if (amount != null) {
                    amount.setText(invoice.getAmount());
                }
            } else {
                Log.e("CardAdapter1", "Invoice object at position " + position + " is null.");
            }
        } else {
            Log.e("CardAdapter1", "Invalid position: " + position);
        }

        return convertView;
    }
}








class AddInvoice {
    private String name;
    private String quantity;
    private String amount;

    public AddInvoice() {
        // Default constructor required for calls to DataSnapshot.getValue(Invoice.class)
    }

    public AddInvoice(String name, String quantity, String amount) {
        this.name = name;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
