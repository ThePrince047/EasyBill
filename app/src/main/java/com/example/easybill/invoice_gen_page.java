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
    EditText user, amount, quantity;
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
        user = findViewById(R.id.edtItemName);
        amount = findViewById(R.id.edtAmount);
        quantity = findViewById(R.id.edtQuantity);
        total = findViewById(R.id.tvTotalAmount);
        button = findViewById(R.id.btnAddInvoice);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter1(this, invoicesList);
        listView.setAdapter(cardAdapter);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));

        button.setOnClickListener(v -> {
            String itemName = user.getText().toString();
            String itemAmount = amount.getText().toString();
            String itemQuantity = quantity.getText().toString();

            if (validateFields(itemName, itemAmount, itemQuantity)) {
                if (isItemUnique(itemName)) {
                    AddInvoice newInvoice = new AddInvoice(itemName, itemQuantity, itemAmount);
                    invoicesList.add(newInvoice);
                    cardAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Item already exists in the list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateFields(String itemName, String itemAmount, String itemQuantity) {
        boolean isValid = true;
        if (itemName.isEmpty()) {
            user.setError("Item name cannot be empty");
            isValid = false;
        }
        if (itemAmount.isEmpty()) {
            amount.setError("Amount cannot be empty");
            isValid = false;
        } else {
            try {
                double amt = Double.parseDouble(itemAmount);
                if (amt <= 0) {
                    amount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amount.setError("Invalid amount");
                isValid = false;
            }
        }
        if (itemQuantity.isEmpty()) {
            quantity.setError("Quantity cannot be empty");
            isValid = false;
        } else {
            try {
                int qty = Integer.parseInt(itemQuantity);
                if (qty <= 0) {
                    quantity.setError("Quantity must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                quantity.setError("Invalid quantity");
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean isItemUnique(String itemName) {
        String normalizedItemName = itemName.trim().toLowerCase(); // Normalize the input item name
        for (AddInvoice invoice : invoicesList) {
            if (invoice.getName().trim().toLowerCase().equals(normalizedItemName)) {
                return false; // Item already exists in the list
            }
        }
        return true; // Item is unique
    }

}



class CardAdapter1 extends ArrayAdapter<AddInvoice> {
    private double totalAmt = 0;
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
                EditText itemName = convertView.findViewById(R.id.edtAddedItemName);
                EditText quantity = convertView.findViewById(R.id.edtAddedQuantity);
                EditText amount = convertView.findViewById(R.id.edtAddedAmount);

                if (itemName != null) {
                    itemName.setText(invoice.getName());
                    final String originalItemName = invoice.getName();

                    itemName.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            String newItemName = itemName.getText().toString();
                            if (newItemName.isEmpty()) {
                                itemName.setError("Can not be empty");
                                return;
                            }

                            String newItemN = newItemName;

                            if (!newItemName.equals(originalItemName)) {
                                if (newItemN.isBlank()) {
                                    Toast.makeText(getContext(), "Item name is empty", Toast.LENGTH_SHORT).show();
                                    itemName.setError("Cant be empty");
                                    invoice.setName(originalItemName);
                                } else {
                                    invoice.setName(newItemName);
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
                }
                if (quantity != null) {
                    quantity.setText(invoice.getQuantity());
                    final String originalQuantity = invoice.getQuantity();

                    quantity.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            String newQuantityText = quantity.getText().toString();
                            if (newQuantityText.isEmpty()) {
                                quantity.setError("Please Enter Greater than 0");
                                return;
                            }

                            int newQuantity = Integer.parseInt(newQuantityText);

                            if (!newQuantityText.equals(originalQuantity)) {
                                if (newQuantity <= 0) {
                                    Toast.makeText(getContext(), "Quantity is zero", Toast.LENGTH_SHORT).show();
                                    quantity.setError("Please Enter Greater than 0");
                                    invoice.setQuantity(originalQuantity);
                                } else {
                                    invoice.setQuantity(newQuantityText);
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

                if (amount != null) {
                    amount.setText(invoice.getAmount());
                    final String originalAmount = invoice.getAmount();

                    amount.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            String newAmountText = amount.getText().toString();
                            if (newAmountText.isEmpty()) {
                                amount.setError("Please Enter Greater than 0");
                                return;
                            }

                            Double newAmount = Double.parseDouble(newAmountText);

                            if (!newAmountText.equals(originalAmount)) {
                                if (newAmount <= 0) {
                                    Toast.makeText(getContext(), "Amount is zero", Toast.LENGTH_SHORT).show();
                                    amount.setError("Please Enter Greater than 0");
                                    invoice.setAmount(originalAmount);
                                } else {
                                    invoice.setAmount(newAmountText);
                                }
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

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
                    double amount = Double.parseDouble(invoice.getAmount());
                    int quantity = Integer.parseInt(invoice.getQuantity());
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

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }
}

