package com.example.easybill;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class invoice_gen_page extends AppCompatActivity {
    Button button;
    EditText edtItemName, edtAmount, edtQuantity,edtTax;
    TextView total;
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
        edtItemName = findViewById(R.id.edtItemName);
        edtAmount = findViewById(R.id.edtAmount);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtTax = findViewById(R.id.edtTax);

        total = findViewById(R.id.tvTotalAmount);
        button = findViewById(R.id.btnAddInvoice);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter1(this, invoicesList);
        listView.setAdapter(cardAdapter);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));

        button.setOnClickListener(v -> {
            String itemName = edtItemName.getText().toString();
            String itemAmount = edtAmount.getText().toString();
            String itemQuantity = edtQuantity.getText().toString();
            String itemTax = edtTax.getText().toString();

            if (validateFields(itemName, itemAmount, itemQuantity,itemTax)) {
                if (isItemUnique(itemName)) {
                    AddInvoice newInvoice = new AddInvoice(itemName, itemQuantity, itemAmount,itemTax);
                    invoicesList.add(newInvoice);
                    cardAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Item already exists in the list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateFields(String itemName, String itemAmount, String itemQuantity,String itemTax) {
        boolean isValid = true;
        if (itemName.isEmpty()) {
            edtItemName.setError("Item name cannot be empty");
            isValid = false;
        }
        if (itemAmount.isEmpty()) {
            edtAmount.setError("Amount cannot be empty");
            isValid = false;
        } else {
            try {
                double amt = Double.parseDouble(itemAmount);
                if (amt <= 0) {
                    edtAmount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtAmount.setError("Invalid amount");
                isValid = false;
            }
        }
        if (itemQuantity.isEmpty()) {
            edtQuantity.setError("Quantity cannot be empty");
            isValid = false;
        } else {
            try {
                int qty = Integer.parseInt(itemQuantity);
                if (qty <= 0) {
                    edtQuantity.setError("Quantity must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtQuantity.setError("Invalid quantity");
                isValid = false;
            }
        }
        if (itemTax.isEmpty()) {
            edtTax.setError("Tax cannot be empty");
            isValid = false;
        } else {
            try {
                int tax = Integer.parseInt(itemTax);
                if (tax <= 0) {
                    edtTax.setError("Tax must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtTax.setError("Invalid quantity");
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean isItemUnique(String itemName) {
        String normalizedItemName = itemName.trim().toLowerCase(); // Normalize the input item name
        for (AddInvoice invoice : invoicesList) {
            if (invoice.getItemName().trim().toLowerCase().equals(normalizedItemName)) {
                return false; // Item already exists in the list
            }
        }
        return true; // Item is unique
    }

}



class CardAdapter1 extends ArrayAdapter<AddInvoice> {
    private double totalAmt,totalItemAmount;
    private TextView totalTextView;

    public CardAdapter1(Context context, List<AddInvoice> invoices) {
        super(context, 0, invoices);
        totalTextView = ((AppCompatActivity) context).findViewById(R.id.tvTotalAmount);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.added_item, parent, false);
        }

        if (position < getCount()) {
            AddInvoice invoice = getItem(position);
            ImageButton removeFromCart = convertView.findViewById(R.id.btnRemovefromcart);

            removeFromCart.setOnClickListener(v -> {
                remove(invoice);
                recalculateTotal();
            });

            if (invoice != null) {
                TextView itemName = convertView.findViewById(R.id.tvAddedItemName);
                TextView quantity = convertView.findViewById(R.id.tvAddedQuantity);
                TextView amount = convertView.findViewById(R.id.tvAddedAmount);

                if (itemName != null) {
                    itemName.setText(invoice.getItemName());
                }
                if (quantity != null) {
                    quantity.setText(invoice.getItemQuantity());
                }

                if (amount != null) {
                    amount.setText("₹ "+invoice.getItemAmount());
                }
                TextView itemTotal = convertView.findViewById(R.id.tvItemTotalAmount);
                int qty = Integer.parseInt(invoice.getItemQuantity());
                double amt = Double.parseDouble(invoice.getItemAmount());
                double total = amt * qty;

                itemTotal.setText("₹"+String.format("%.2f", total));


                recalculateTotal();
            } else {
                Log.e("CardAdapter1", "Invoice object at position " + position + " is null.");
            }
        } else {
            Log.e("CardAdapter1", "Invalid position: " + position);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        recalculateTotal();
    }

    private void recalculateTotal() {
        totalAmt = 0;
        for (int i = 0; i < getCount(); i++) {
            AddInvoice invoice = getItem(i);
            if (invoice != null) {
                try {

                    double amount = Double.parseDouble(invoice.getItemAmount());
                    int quantity = Integer.parseInt(invoice.getItemQuantity());
                    totalAmt += amount * quantity;

                } catch (NumberFormatException e) {
                    // Handle the case where amount or quantity is not a valid number
                }
            }
        }

        if (totalTextView != null) {


            totalTextView.setText(String.format("%.2f", totalAmt));
        } else {
            Log.e("CardAdapter1", "TextView for total amount is null.");
        }
    }
}


class AddInvoice {
    private String itemName;
    private String itemQuantity;
    private String itemAmount;
    private  String itemTax;

    public AddInvoice() {
        // Default constructor required for calls to DataSnapshot.getValue(Invoice.class)
    }

    public AddInvoice(String itemName, String itemQuantity, String itemAmount, String itemTax) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemAmount = itemAmount;
        this.itemTax = itemTax;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public String getItemTax() {
        return itemTax;
    }

    public void setItemTax(String itemTax) {
        this.itemTax = itemTax;
    }
}

