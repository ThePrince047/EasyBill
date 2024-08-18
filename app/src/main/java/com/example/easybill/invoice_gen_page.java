package com.example.easybill;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    private Button btnAddItem, btnSaveInvoice, btnCancelInvoice;
    private ListView listView;
    private List<AddInvoice> invoicesList;
    private CardAdapter cardAdapter;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_gen_page);
        getWindow().setStatusBarColor(getResources().getColor(R.color.Background));

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnAddItem = findViewById(R.id.btnAddItems);
        btnSaveInvoice = findViewById(R.id.btnSave);
        btnCancelInvoice = findViewById(R.id.btnCancel);
        listView = findViewById(R.id.lvAddedItems);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter(this, invoicesList);
        listView.setAdapter(cardAdapter);
    }

    private void setupListeners() {
        btnAddItem.setOnClickListener(v -> {
            Invoice_Dailog invoiceDialog = new Invoice_Dailog(this, this);
            invoiceDialog.showInvoiceDialog();
        });

        btnSaveInvoice.setOnClickListener(v -> {
            if (invoicesList.isEmpty()) {
                Toast.makeText(this, "No items to save. Please add items first.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveInvoice();
        });

        btnCancelInvoice.setOnClickListener(v -> {
            invoicesList.clear();
            cardAdapter.notifyDataSetChanged();
        });
    }

    private void saveInvoice() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        CollectionReference invoicesRef = firestore.collection("EasyBill").document(userId).collection("invoices");

        invoicesRef.orderBy("invoiceId", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextInvoiceId = 1;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastInvoice = queryDocumentSnapshots.getDocuments().get(0);
                        Number lastId = lastInvoice.getLong("invoiceId");
                        nextInvoiceId = lastId != null ? lastId.intValue() + 1 : 1;

                    Intent intent = new Intent(this, CustomerInfo.class);
                    intent.putExtra("nextInvoiceId",String.valueOf(nextInvoiceId));
                    intent.putExtra("invoiceData", new HashMap<>(getInvoiceData(nextInvoiceId))); // Assuming you have a method to get invoice data
                    startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error retrieving invoice ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> getInvoiceData(int invoiceId) {
        // Create and return a map containing the invoice data
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("invoiceId", invoiceId);
        invoiceData.put("date", formattedDate);

        double totalAmt = 0, totalTax = 0, grandTotal = 0;

        for (int i = 0; i < invoicesList.size(); i++) {
            AddInvoice invoice = invoicesList.get(i);
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("itemName", invoice.getItemName());
            itemData.put("itemQuantity", invoice.getItemQuantity());
            itemData.put("itemAmount", invoice.getItemAmount());
            itemData.put("itemTax", invoice.getItemTax());

            double amount = Double.parseDouble(invoice.getItemAmount());
            int quantity = Integer.parseInt(invoice.getItemQuantity());
            int taxSlab = Integer.parseInt(invoice.getItemTax());

            double itemTotalWithTax = amount * quantity;
            double taxAmount = itemTotalWithTax * (taxSlab / 100.0);
            double itemTotalBeforeTax = itemTotalWithTax - taxAmount;

            totalAmt += itemTotalBeforeTax;
            totalTax += taxAmount;
            grandTotal = totalAmt + totalTax;

            invoiceData.put("item" + i, itemData);
        }

        invoiceData.put("subtotal", totalAmt);
        invoiceData.put("tax", totalTax);
        invoiceData.put("grandTotal", grandTotal);

        return invoiceData;
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

class CardAdapter extends ArrayAdapter<AddInvoice> {

    private double totalAmt, totalTax, grandTotal;
    private final TextView totalTextView, taxTextView, grandTotalTextView,customerTextView;

    public CardAdapter(Context context, List<AddInvoice> invoices) {
        super(context, 0, invoices);
        totalTextView = ((AppCompatActivity) context).findViewById(R.id.tvTotalAmount);
        taxTextView = ((AppCompatActivity) context).findViewById(R.id.tvTax);
        grandTotalTextView = ((AppCompatActivity) context).findViewById(R.id.tvGrandTotal);
        customerTextView = ((AppCompatActivity) context).findViewById(R.id.tvCustomerName);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.added_item, parent, false);
        }

        AddInvoice invoice = getItem(position);
        if (invoice != null) {
            bindInvoiceData(convertView, invoice);
        } else {
            Log.e("CardAdapter", "Invoice object at position " + position + " is null.");
        }

        return convertView;
    }

    private void bindInvoiceData(View convertView, AddInvoice invoice) {
        TextView itemName = convertView.findViewById(R.id.tvAddedItemName);
        TextView quantity = convertView.findViewById(R.id.tvAddedQuantity);
        TextView amount = convertView.findViewById(R.id.tvAddedAmount);
        TextView tax = convertView.findViewById(R.id.tvTax);
        TextView itemTotal = convertView.findViewById(R.id.tvItemTotalAmount);

        if (itemName != null) itemName.setText(invoice.getItemName());
        if (quantity != null) quantity.setText(invoice.getItemQuantity());
        if (amount != null) amount.setText("₹ " + invoice.getItemAmount());
        if (tax != null) tax.setText("Tax: " + invoice.getItemTax() + "%");

        double taxedAmt = Double.parseDouble(invoice.getItemAmount());
        int qty = Integer.parseInt(invoice.getItemQuantity());
        int taxSlab = Integer.parseInt(invoice.getItemTax());

        double total = taxedAmt * qty;
        double taxAmt = total * (taxSlab / 100.0);
        total -= taxAmt;

        itemTotal.setText("₹ " + String.format("%.2f", total));
        recalculateTotal();
    }

    @Override
    public void notifyDataSetChanged() {
        recalculateTotal();
        super.notifyDataSetChanged();
    }

    private void recalculateTotal() {
        totalAmt = totalTax = grandTotal = 0;
        for (int i = 0; i < getCount(); i++) {
            AddInvoice invoice = getItem(i);
            double amount = Double.parseDouble(invoice.getItemAmount());
            int quantity = Integer.parseInt(invoice.getItemQuantity());
            int taxSlab = Integer.parseInt(invoice.getItemTax());

            double itemTotal = amount * quantity;
            double taxAmount = itemTotal * (taxSlab / 100.0);
            double subtotal = itemTotal - taxAmount;

            totalAmt += subtotal;
            totalTax += taxAmount;
            grandTotal = totalAmt + totalTax;
        }

        updateTotals();
    }

    private void updateTotals() {
        if (totalTextView != null) totalTextView.setText("₹ " + String.format("%.2f", totalAmt));
        if (taxTextView != null) taxTextView.setText("₹ " + String.format("%.2f", totalTax));
        if (grandTotalTextView != null) grandTotalTextView.setText("₹ " + String.format("%.2f", grandTotal));
    }
}

class AddInvoice {
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

    // Getters
    public String getItemName() { return itemName; }
    public String getItemQuantity() { return itemQuantity; }
    public String getItemAmount() { return itemAmount; }
    public String getItemTax() { return itemTax; }
}
