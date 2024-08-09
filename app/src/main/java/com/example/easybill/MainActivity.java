package com.example.easybill;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    TextView homeamt, Username, viewall;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private List<Invoice> invoiceList = new ArrayList<>();
    private InvoiceAdapter invoiceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fabButton);
        listView = findViewById(R.id.homeinvoicelist);
        homeamt = findViewById(R.id.homeamt);
        Username = findViewById(R.id.lblUser);
        viewall = findViewById(R.id.viewAllButton);

        invoiceAdapter = new InvoiceAdapter(this, invoiceList);
        listView.setAdapter(invoiceAdapter);

        // Fetch and display the company name
        displayUsername();

        // Fetch and display all invoices
        fetchAllInvoices();

        // Calculate and display total revenue for today
        calculateTodayRevenue();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Toast.makeText(getApplicationContext(), "This Page is Running", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_reports) {
                    startActivity(new Intent(getApplicationContext(), reports_page.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), invoice_gen_page.class));
                overridePendingTransition(0, 0);
            }
        });

        viewall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), reports_page.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    // Fetches the company name from Firestore and sets it to the Username TextView
    void displayUsername() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("company_info")
                .document(currentUser.getUid())  // Use the correct document ID
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String companyName = documentSnapshot.getString("companyName");
                            if (companyName != null) {
                                Username.setText(companyName);
                            } else {
                                Username.setText("No Company Name Found");
                            }
                        } else {
                            Log.e("MainActivity", "Company Info Document does not exist.");
                            Username.setText("No Company Info Found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error fetching company info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Fetches all invoices from Firestore and displays them in the ListView
    void fetchAllInvoices() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference invoicesRef = db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("invoices");


        // Query to fetch and sort invoices by invoiceId (latest first)
        Query sortedInvoicesQuery = invoicesRef.orderBy("invoiceId", Query.Direction.DESCENDING).limit(10);

        sortedInvoicesQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        invoiceList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Double invoiceId = document.getDouble("invoiceId");
                            Double grandTotal = document.getDouble("grandTotal");

                            if (invoiceId != null && grandTotal != null) {
                                Invoice invoice = new Invoice(invoiceId, grandTotal);
                                invoiceList.add(invoice);
                            } else {
                                Log.e("MainActivity", "Missing fields in document: " + document.getId());
                            }
                        }
                        invoiceAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error fetching invoices: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Calculates today's total revenue by summing up the grandTotal of all invoices from today
    void calculateTodayRevenue() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference invoicesRef = db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("invoices");

        // Get today's date in the correct format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        // Query to get today's invoices
        Query todayInvoicesQuery = invoicesRef.whereEqualTo("date", todayDate);

        todayInvoicesQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        double totalRevenue = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Double grandTotal = document.getDouble("grandTotal");
                            if (grandTotal != null) {
                                totalRevenue += grandTotal;
                            }
                        }
                        // Set total revenue to home screen amount
                        homeamt.setText("₹" + totalRevenue);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error calculating today's revenue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


class InvoiceAdapter extends ArrayAdapter<Invoice> {

    public InvoiceAdapter(@NonNull Context context, @NonNull List<Invoice> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.added_invoices, parent, false);
        }

        Invoice invoice = getItem(position);

        TextView tvInvoiceNumber = convertView.findViewById(R.id.tvInvoiceNumber);
        TextView tvAddedQuantity = convertView.findViewById(R.id.tvAddedQuantity);
        TextView tvGrandTotal = convertView.findViewById(R.id.tvGrandTotal);
        TextView tvStatusPaid = convertView.findViewById(R.id.tvStatusPaid);

        if (invoice != null) {
            tvInvoiceNumber.setText("INV#" + String.format("%.0f", invoice.getInvoiceId()));
            tvGrandTotal.setText("₹" + invoice.getGrandTotal());
        }

        return convertView;
    }
}

class Invoice {
    private double invoiceId;
    private double grandTotal;

    public Invoice(double invoiceId, double grandTotal) {
        this.invoiceId = invoiceId;
        this.grandTotal = grandTotal;
    }

    public double getInvoiceId() {
        return invoiceId;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

}
