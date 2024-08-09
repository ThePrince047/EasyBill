package com.example.easybill;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class invoice_gen_page extends AppCompatActivity implements Invoice_Dailog.InvoiceDialogListener {
    Button btnAddItem, btnSaveInvoice, btnCancelInvoice;
    ListView listView;
    List<AddInvoice> invoicesList;
    CardAdapter1 cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_gen_page);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));
        btnAddItem = findViewById(R.id.btnAddItems);
        btnSaveInvoice = findViewById(R.id.btnSave);
        btnCancelInvoice = findViewById(R.id.btnCancel);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter1(this, invoicesList);
        listView.setAdapter(cardAdapter);

        btnAddItem.setOnClickListener(v -> {
            Invoice_Dailog invoiceDialog = new Invoice_Dailog(this, this);
            invoiceDialog.showInvoiceDialog();
        });

        btnSaveInvoice.setOnClickListener(v -> {
            if (invoicesList.isEmpty()) {
                Toast.makeText(invoice_gen_page.this, "No items to save. Please add items first.", Toast.LENGTH_SHORT).show();
                return;
            }

            getNextInvoiceIdAndSave();
        });

        btnCancelInvoice.setOnClickListener(v -> {
            invoicesList.clear();
            cardAdapter.notifyDataSetChanged();
        });
    }

    private void getNextInvoiceIdAndSave() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(invoice_gen_page.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        CollectionReference invoicesRef = firestore.collection("EasyBill")
                .document(userId)
                .collection("invoices");

        // Retrieve the last invoice ID and get the next one
        invoicesRef.orderBy("invoiceId", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextInvoiceId = 1;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastInvoice = queryDocumentSnapshots.getDocuments().get(0);
                        Number lastId = lastInvoice.getLong("invoiceId");
                        if (lastId != null) {
                            nextInvoiceId = lastId.intValue() + 1;
                        }
                    }
                    saveInvoicesWithId(invoicesRef, nextInvoiceId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(invoice_gen_page.this, "Error retrieving invoice ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveInvoicesWithId(CollectionReference invoicesRef, int invoiceId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(invoice_gen_page.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get today's date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        // Create a map to hold all invoice items and totals
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("invoiceId", invoiceId);
        invoiceData.put("date", formattedDate);  // Add date field

        double totalAmt = 0;
        double totalTax = 0;
        double grandTotal = 0;

        // Add each invoice item to the map
        for (int i = 0; i < invoicesList.size(); i++) {
            AddInvoice invoice = invoicesList.get(i);
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("itemName", invoice.getItemName());
            itemData.put("itemQuantity", invoice.getItemQuantity());
            itemData.put("itemAmount", invoice.getItemAmount());
            itemData.put("itemTax", invoice.getItemTax());

            // Calculate item totals
            double amount = Double.parseDouble(invoice.getItemAmount());
            int quantity = Integer.parseInt(invoice.getItemQuantity());
            int taxSlab = Integer.parseInt(invoice.getItemTax());

            double itemTotalWithTax = amount * quantity;
            double taxAmount = itemTotalWithTax * (taxSlab / 100.0);
            double itemTotalBeforeTax = itemTotalWithTax - taxAmount;

            totalAmt += itemTotalBeforeTax;
            totalTax += taxAmount;
            grandTotal = totalAmt + totalTax;

            invoiceData.put("item" + i, itemData);  // Save each item under a unique key
        }

        // Add subtotal, tax, and grand total to invoiceData
        invoiceData.put("subtotal", totalAmt);
        invoiceData.put("tax", totalTax);
        invoiceData.put("grandTotal", grandTotal);

        // Save the invoice with the items and totals map
        invoicesRef.document(String.valueOf(invoiceId))
                .set(invoiceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(invoice_gen_page.this, "Invoice saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(invoice_gen_page.this, "Error saving invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        invoicesList.clear();
        cardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInvoiceAdded(String itemName, String itemQuantity, String itemAmount, String itemTax) {
        if (isItemUnique(itemName)) {
            AddInvoice newInvoice = new AddInvoice(itemName, itemQuantity, itemAmount, itemTax);
            invoicesList.add(newInvoice);
            cardAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Item already exists in the list", Toast.LENGTH_SHORT).show();
        }
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
    private double totalAmt, totalTax, grandTotal;
    private TextView totalTextView, taxTextView, grandTotalTextView;

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
                if (tax != null) {
                    tax.setText("Tax: " + invoice.getItemTax() + "%");
                }

                TextView itemTotal = convertView.findViewById(R.id.tvItemTotalAmount);
                int taxSlab = Integer.parseInt(invoice.getItemTax());
                int qty = Integer.parseInt(invoice.getItemQuantity());
                double taxedAmt = Double.parseDouble(invoice.getItemAmount());
                double total = taxedAmt * qty;
                double taxAmt = total * (taxSlab / 100.0);
                total -= taxAmt;
                itemTotal.setText("₹ " + String.format("%.2f", total));

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
        totalTax = 0;
        grandTotal = 0;

        for (int i = 0; i < getCount(); i++) {
            AddInvoice invoice = getItem(i);
            if (invoice != null) {
                try {
                    double amount = Double.parseDouble(invoice.getItemAmount());
                    int quantity = Integer.parseInt(invoice.getItemQuantity());
                    int taxSlab = Integer.parseInt(invoice.getItemTax());

                    double itemTotalWithTax = amount * quantity;
                    double taxAmount = itemTotalWithTax * (taxSlab / 100.0);
                    double itemTotalBeforeTax = itemTotalWithTax - taxAmount;

                    totalAmt += itemTotalBeforeTax;
                    totalTax += taxAmount;
                    grandTotal = totalAmt + totalTax;
                } catch (NumberFormatException e) {
                    Log.e("CardAdapter1", "Number format exception while recalculating total", e);
                }
            }
        }

        if (totalTextView != null) {
            totalTextView.setText(String.format("₹ %.2f", totalAmt));
            taxTextView.setText(String.format("₹ %.2f", totalTax));
            grandTotalTextView.setText(String.format("₹ %.2f", grandTotal));
        } else {
            Log.e("CardAdapter1", "TextView for total amount is null.");
        }
    }

    public double getSubtotal() {
        return totalAmt;
    }

    public double getTotalTax() {
        return totalTax;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

}

class AddInvoice implements Parcelable {
    private String itemName;
    private String itemQuantity;
    private String itemAmount;
    private String itemTax;

    // Default constructor
    public AddInvoice() {}

    // Parameterized constructor
    public AddInvoice(String itemName, String itemQuantity, String itemAmount, String itemTax) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.itemAmount = itemAmount;
        this.itemTax = itemTax;
    }

    protected AddInvoice(Parcel in) {
        itemName = in.readString();
        itemQuantity = in.readString();
        itemAmount = in.readString();
        itemTax = in.readString();
    }

    public static final Parcelable.Creator<AddInvoice> CREATOR = new Creator<AddInvoice>() {
        @Override
        public AddInvoice createFromParcel(Parcel in) {
            return new AddInvoice(in);
        }

        @Override
        public AddInvoice[] newArray(int size) {
            return new AddInvoice[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemName);
        dest.writeString(itemQuantity);
        dest.writeString(itemAmount);
        dest.writeString(itemTax);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getItemName() { return itemName; }
    public String getItemQuantity() { return itemQuantity; }
    public String getItemAmount() { return itemAmount; }
    public String getItemTax() { return itemTax; }
}
