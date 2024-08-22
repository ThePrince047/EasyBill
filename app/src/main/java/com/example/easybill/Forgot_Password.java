package com.example.easybill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Forgot_Password {
    private Context context;
    private ForgotPasswordListener listener;

    public Forgot_Password(Context context, ForgotPasswordListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showForgotPassDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.forgot_password, null);
        EditText forgotPassEmail = dialogView.findViewById(R.id.ForgotPasswordEmail);


        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_Dialog)
                .setView(dialogView)
                .setPositiveButton("Reset", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // Customize button text color
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(android.R.color.black));

            // Set OnClickListener for the positive button
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateFields(forgotPassEmail)) {
                    listener.onForgotPassword(forgotPassEmail.getText().toString());
                    dialog.dismiss();
                }

            });
        });

        dialog.show();

    }

    private boolean validateFields(EditText forgotPassEmail) {
        if (forgotPassEmail.getText().toString().isEmpty()) {
            forgotPassEmail.setError("Email Can not be Empty");
            return false;
        }

        return true;
    }


    public interface ForgotPasswordListener {
        void onForgotPassword(String email);
    }
}
