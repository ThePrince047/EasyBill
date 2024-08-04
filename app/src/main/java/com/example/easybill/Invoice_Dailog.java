package com.example.easybill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
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

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_Dialog)
                .setTitle("Add Item")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(android.R.color.white));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(android.R.color.white));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String itemName = edtItemName.getText().toString();
                String quantity = edtQuantity.getText().toString();
                String amount = edtAmount.getText().toString();
                String tax = edtTax.getText().toString();

                if (validateFields(edtItemName, edtQuantity, edtAmount, edtTax)) {
                    listener.onInvoiceAdded(itemName, quantity, amount, tax);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validateFields(EditText edtItemName, EditText edtQuantity, EditText edtAmount, EditText edtTax) {
        boolean isValid = true;

        if (edtItemName.getText().toString().isEmpty()) {
            edtItemName.setError("Item name cannot be empty");
            isValid = false;
        }

        if (edtQuantity.getText().toString().isEmpty()) {
            edtQuantity.setError("Quantity cannot be empty");
            isValid = false;
        } else {
            try {
                int qty = Integer.parseInt(edtQuantity.getText().toString());
                if (qty <= 0) {
                    edtQuantity.setError("Quantity must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtQuantity.setError("Invalid quantity");
                isValid = false;
            }
        }

        if (edtAmount.getText().toString().isEmpty()) {
            edtAmount.setError("Amount cannot be empty");
            isValid = false;
        } else {
            try {
                double amt = Double.parseDouble(edtAmount.getText().toString());
                if (amt <= 0) {
                    edtAmount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtAmount.setError("Invalid amount");
                isValid = false;
            }
        }
        if (edtTax.getText().toString().isEmpty()) {
            edtTax.setError("Tax cannot be empty");
            isValid = false;
        } else {
            try {
                int tax = Integer.parseInt(edtTax.getText().toString());
                if (!((tax >= 0) && (tax <= 28))) {
                    edtTax.setError("Tax should be in the range of 0 to 28");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                edtTax.setError("Invalid amount");
                isValid = false;
            }
        }
        return isValid;
    }

    public interface InvoiceDialogListener {
        void onInvoiceAdded(String itemName, String quantity, String amount, String tax);
    }
}