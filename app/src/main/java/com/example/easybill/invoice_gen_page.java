package com.example.easybill;

import android.content.Context;
import android.os.Bundle;
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

public class invoice_gen_page extends AppCompatActivity implements Invoice_Dailog.InvoiceDialogListener {
    Button button;
    EditText edtItemName, edtAmount, edtQuantity, edtTax;
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

        //edtTax = findViewById(R.id.edtTax);

        total = findViewById(R.id.tvTotalAmount);
        button = findViewById(R.id.btnAddItems);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter1(this, invoicesList);
        listView.setAdapter(cardAdapter);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));

        button.setOnClickListener(v -> {
            Invoice_Dailog invoiceDialog = new Invoice_Dailog(this, this);
            invoiceDialog.showInvoiceDialog();
        });
    }

    @Override
    public void onInvoiceAdded(String itemName, String itemQuantity, String itemAmount , String itemTax) {
//       if (validateFields(itemName, itemAmount, itemQuantity,itemTax)) {
            if (isItemUnique(itemName)) {
                AddInvoice newInvoice = new AddInvoice(itemName, itemQuantity, itemAmount,itemTax);
                invoicesList.add(newInvoice);
                cardAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Item already exists in the list", Toast.LENGTH_SHORT).show();
            }
        //}
    }

    private boolean validateFields(String itemName, String itemAmount, String itemQuantity) {
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
//        if (itemTax.isEmpty()) {
//            edtTax.setError("Tax cannot be empty");
//            isValid = false;
//        } else {
//            try {
//                int tax = Integer.parseInt(itemTax);
//                if (tax <= 0) {
//                    edtTax.setError("Tax must be greater than zero");
//                    isValid = false;
//                }
//            } catch (NumberFormatException e) {
//                edtTax.setError("Invalid tax");
//                isValid = false;
//            }
//        }
        return isValid;
    }

    private boolean isItemUnique(String itemName) {
        String normalizedItemName = itemName.trim().toLowerCase();
        for (AddInvoice invoice : invoicesList) {
            if (invoice.getItemName().trim().toLowerCase().equals(normalizedItemName)) {
                return false;
            }
        }
        return true;
    }
}


class CardAdapter1 extends ArrayAdapter<AddInvoice> {
    private double totalAmt,totalTax,grandTotal;
    private TextView totalTextView,taxTextView,grandTotalTextView;

    public CardAdapter1(Context context, List<AddInvoice> invoices) {
        super(context, 0, invoices);
        totalTextView = ((AppCompatActivity) context).findViewById(R.id.tvTotalAmount);
        taxTextView = ((AppCompatActivity) context).findViewById(R.id.tvTax);
        grandTotalTextView = ((AppCompatActivity) context).findViewById(R.id.tvGrandTotal);
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
                TextView tax = convertView.findViewById(R.id.tvTax);

                if (itemName != null) {
                    itemName.setText(invoice.getItemName());
                }
                if (quantity != null) {
                    quantity.setText(invoice.getItemQuantity());
                }

                if (amount != null) {
                    amount.setText("₹ " + invoice.getItemAmount());
                }
                if(tax != null){
                    tax.setText("Tax : "+invoice.getItemTax()+"%" );
                }
                TextView itemTotal = convertView.findViewById(R.id.tvItemTotalAmount);
                TextView taxTotal = convertView.findViewById(R.id.tvTax);
                int taxSlab = Integer.parseInt(invoice.getItemTax());
                int qty = Integer.parseInt(invoice.getItemQuantity());
                double TaxedAmt = Double.parseDouble(invoice.getItemAmount());
                double total = TaxedAmt * qty;
                double TaxAmt = total * (taxSlab/100);
                total-=TaxAmt;

                        itemTotal.setText("₹" + String.format("%.2f", total));

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
        totalTax =0;
        grandTotal=0;
        for (int i = 0; i < getCount(); i++) {
            AddInvoice invoice = getItem(i);
            if (invoice != null) {
                try {
                    // Parse amount, quantity, and tax from invoice
                    double amount = Double.parseDouble(invoice.getItemAmount());
                    int quantity = Integer.parseInt(invoice.getItemQuantity());
                    int taxSlab = Integer.parseInt(invoice.getItemTax());

                    // Calculate total amount including tax for the current invoice
                    double itemTotalWithTax = amount * quantity;
                    double taxAmount = itemTotalWithTax * (taxSlab / 100.0);
                    double itemTotalBeforeTax = itemTotalWithTax - taxAmount;


                    // Accumulate total amount before tax
                    totalAmt += itemTotalBeforeTax;
                    totalTax += taxAmount;
                    grandTotal = totalAmt + totalTax;

                } catch (NumberFormatException e) {
                    // Log an error or handle the case where amount, quantity, or tax is not a valid number
                    Log.e("CardAdapter1", "Number format exception while recalculating total", e);
                }
            }
        }

        if (totalTextView != null) {
            // Display the total amount before tax
            totalTextView.setText(String.format("₹ %.2f", totalAmt));
            taxTextView.setText(String.format("₹ %.2f", totalTax));
            grandTotalTextView.setText(String.format("₹ %.2f", grandTotal));
        } else {
            Log.e("CardAdapter1", "TextView for total amount is null.");
        }
    }

}

class AddInvoice {
    private String itemName;
    private String itemQuantity;
    private String itemAmount;
    private String itemTax;

    public AddInvoice() {
        // Default constructor required for calls to DataSnapshot.getValue(Invoice.class)
    }

    public AddInvoice(String itemName, String itemQuantity, String itemAmount,String itemTax) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemAmount = itemAmount;
        this.itemTax = itemTax;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public String getItemTax() {
        return itemTax;
    }
}

