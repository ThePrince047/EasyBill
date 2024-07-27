package com.example.easybill;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    List<Invoice> invoicesList;
    CardAdapter cardAdapter;
    TextView homeamt,Username;

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
        disBalance();
        disUsername();
        listView = findViewById(R.id.homeinvoicelist);
        invoicesList = new ArrayList<>();
        cardAdapter = new CardAdapter(this, invoicesList);
        listView.setAdapter(cardAdapter);
        homeamt = findViewById(R.id.homeamt);
        Username = findViewById(R.id.lblUser);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference invoicesRef = database.getReference("invoices");

        invoicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                invoicesList.clear();
                for (DataSnapshot invoiceSnapshot : dataSnapshot.getChildren()) {
                    Invoice invoice = invoiceSnapshot.getValue(Invoice.class);
                    if (invoice != null) {
                        invoicesList.add(invoice);
                    } else {
                        Log.e("Firebase", "Invoice is null");
                        Log.w("MainActivity", "Failed to convert Invoice object from DataSnapshot: " + invoiceSnapshot.getKey());
                    }
                }
                cardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });


    }
    void disBalance() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference balanceRef = database.getReference("users").child("userID_007").child("balance");

        balanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long balance = snapshot.getValue(Long.class);
                if (balance != null) {
                    homeamt.setText(balance.toString());
                } else {
                    homeamt.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                homeamt.setText("Error: " + error.getMessage());
            }
        });
    }

    void disUsername(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usernameRef = database.getReference("users").child("userID_007").child("username");
        usernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user = snapshot.getValue(String.class);
                Username.setText(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}

class CardAdapter extends ArrayAdapter<Invoice> {
    public CardAdapter(Context context, List<Invoice> invoices) {
        super(context, 0, invoices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.invoice_list_item, parent, false);
        }

        Invoice invoice = getItem(position);

        if (invoice != null) {
            TextView lblPartyname = convertView.findViewById(R.id.lblPartyname);
            TextView lbldate = convertView.findViewById(R.id.lbldate);
            TextView lblAmt = convertView.findViewById(R.id.lblAmt);

            lblPartyname.setText(invoice.getDescription());
            lbldate.setText(invoice.getDate());
            lblAmt.setText(invoice.getAmount().toString());
        } else {
            Log.e("CardAdapter", "Invoice object at position " + position + " is null.");
        }

        return convertView;
    }
}


class Invoice {
    private String description;
    private String date;
    private Double amount;

    public Invoice() {
        // Default constructor required for calls to DataSnapshot.getValue(Invoice.class)
    }

    public Invoice(String description, String date, Double amount) {
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}



