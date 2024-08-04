package com.example.easybill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Invoice_Dailog {
    private Context context;
    private InvoiceDialogListener listener;

    public Invoice_Dailog(Context context, InvoiceDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showInvoiceDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.invoice_dialog, null);
        EditText edtItemName = dialogView.findViewById(R.id.editDialogItemName);
        EditText edtQuantity = dialogView.findViewById(R.id.editDialogQuantity);
        EditText edtAmount = dialogView.findViewById(R.id.editDialogAmount);
        EditText edtTax = dialogView.findViewById(R.id.editDialogTax);

        new MaterialAlertDialogBuilder(context)
                .setTitle("Add Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemName = edtItemName.getText().toString();
                    String quantity = edtQuantity.getText().toString();
                    String amount = edtAmount.getText().toString();
                    String tax = edtTax.getText().toString();
                    listener.onInvoiceAdded(itemName, quantity, amount,tax);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public interface InvoiceDialogListener {
        void onInvoiceAdded(String itemName, String quantity, String amount,String tax);
    }
}