package com.example.easybill;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class reports_page extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    ListView listView;
    FirebaseFirestore db;
    FirebaseAuth auth;
    ProgressBar progressBar; // Add this line

    // TextViews for different earnings
    TextView amtAllTimeEarning;
    TextView amtThisYearEarning;
    TextView amtThisMonthEarning;
    TextView amtTodaysEarning;

    Map<String, MonthProfit> monthlyProfitMap = new HashMap<>();
    int allTimeEarnings = 0;
    int thisYearEarnings = 0;
    int thisMonthEarnings = 0;
    int todaysEarnings = 0;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0, 0);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports_page);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.Background));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fabButton);
        listView = findViewById(R.id.listMonthlyAddedInvoice);

        amtAllTimeEarning = findViewById(R.id.amtAllTimeEarning);
        amtThisYearEarning = findViewById(R.id.amtThisYearEarning);
        amtThisMonthEarning = findViewById(R.id.amtThisMonthEarning);
        amtTodaysEarning = findViewById(R.id.amtTodaysEarning);

        progressBar = findViewById(R.id.progressBar); // Initialize the ProgressBar



        bottomNavigationView.setSelectedItemId(R.id.nav_reports);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_reports) {
                Toast.makeText(getApplicationContext(), "This Page is Running", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        fab.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), invoice_gen_page.class));
            overridePendingTransition(0, 0);
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Call the method to fetch and display the profit totals
        calculateEarnings();
    }

    private void calculateEarnings() {
        // Show the ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        CollectionReference invoicesRef = db.collection("EasyBill")
                .document(auth.getCurrentUser().getUid())
                .collection("invoices");

        invoicesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String date = document.getString("date");
                    int grandTotal = document.getLong("grandTotal").intValue();

                    // Update All Time Earnings
                    allTimeEarnings += grandTotal;

                    // Check if the invoice is from this year
                    if (date != null && date.startsWith(String.valueOf(LocalDate.now().getYear()))) {
                        thisYearEarnings += grandTotal;
                    }

                    // Check if the invoice is from this month
                    String currentMonthYear = YearMonth.now().toString(); // e.g., "2024-08"
                    String monthYearKey = date.substring(0, 7);
                    if (monthYearKey.equals(currentMonthYear)) {
                        thisMonthEarnings += grandTotal;
                    }

                    // Check if the invoice is from today
                    if (date != null && date.equals(LocalDate.now().toString())) {
                        todaysEarnings += grandTotal;
                    }

                    // Update Monthly Profit Map
                    if (monthlyProfitMap.containsKey(monthYearKey)) {
                        MonthProfit currentMonthProfit = monthlyProfitMap.get(monthYearKey);
                        currentMonthProfit = new MonthProfit(
                                currentMonthProfit.getProfit() + grandTotal,
                                currentMonthProfit.getInvoiceCount() + 1
                        );
                        monthlyProfitMap.put(monthYearKey, currentMonthProfit);
                    } else {
                        monthlyProfitMap.put(monthYearKey, new MonthProfit(grandTotal, 1));
                    }
                }

                // Hide the ProgressBar and display the data
                progressBar.setVisibility(View.GONE);
                displayEarnings();
                displayMonthlyProfits();
            } else {
                Log.w("Firestore", "Error getting documents.", task.getException());
                progressBar.setVisibility(View.GONE); // Hide ProgressBar even if there's an error
            }
        });
    }

    private void displayEarnings() {
        amtAllTimeEarning.setText("₹ " + allTimeEarnings);
        amtThisYearEarning.setText("₹ " + thisYearEarnings);
        amtThisMonthEarning.setText("₹ " + thisMonthEarnings);
        amtTodaysEarning.setText("₹ " + todaysEarnings);
    }

    private void displayMonthlyProfits() {
        List<Map.Entry<String, MonthProfit>> monthProfitList = new ArrayList<>(monthlyProfitMap.entrySet());

        MonthProfitAdapter adapter = new MonthProfitAdapter(this, monthProfitList);
        listView.setAdapter(adapter);
    }

    private static class MonthProfit {
        private final int profit;
        private final int invoiceCount;

        public MonthProfit(int profit, int invoiceCount) {
            this.profit = profit;
            this.invoiceCount = invoiceCount;
        }

        public int getProfit() {
            return profit;
        }

        public int getInvoiceCount() {
            return invoiceCount;
        }
    }

    private static class MonthProfitAdapter extends ArrayAdapter<Map.Entry<String, MonthProfit>> {

        private final Context context;
        private final List<Map.Entry<String, MonthProfit>> monthProfitList;

        public MonthProfitAdapter(Context context, List<Map.Entry<String, MonthProfit>> monthProfitList) {
            super(context, R.layout.added_monthlyinvoicecount, monthProfitList);
            this.context = context;
            this.monthProfitList = monthProfitList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.added_monthlyinvoicecount, parent, false);
            }

            Map.Entry<String, MonthProfit> monthProfitEntry = monthProfitList.get(position);

            TextView lblMonthName = convertView.findViewById(R.id.lblMonthName);
            TextView lblTotalIncome = convertView.findViewById(R.id.lblTotalIncome);
            TextView lblInvoiceCount = convertView.findViewById(R.id.lblInvoiceCount);

            String monthYear = monthProfitEntry.getKey(); // e.g., "2024-03"
            MonthProfit monthProfit = monthProfitEntry.getValue();
            int profit = monthProfit.getProfit();
            int invoiceCount = monthProfit.getInvoiceCount();

            // Convert "2024-03" to "Mar 2024"
            YearMonth yearMonth = YearMonth.parse(monthYear);
            String formattedMonthYear = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + yearMonth.getYear();

            lblMonthName.setText(formattedMonthYear);
            lblTotalIncome.setText("₹ " + profit);
            lblInvoiceCount.setText("Invoice count: " + invoiceCount);

            return convertView;
        }
    }
}
