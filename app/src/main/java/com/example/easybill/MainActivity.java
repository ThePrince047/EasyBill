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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    TextView homeamt, Username, viewall;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    FirebaseUser currentUser = FirebaseAuth.getInstance() .getCurrentUser() ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.ThemeColor));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        disBalance();
        displayUsername();

        viewall = findViewById(R.id.viewAllButton);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fabButton);
        listView = findViewById(R.id.homeinvoicelist);
        homeamt = findViewById(R.id.homeamt);
        Username = findViewById(R.id.lblUser);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference invoicesRef = db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("invoices");



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

    void disBalance() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("EasyBill")
                .document(currentUser.getUid())
                .collection("user_info")
                .document(currentUser.getUid());

        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String user = documentSnapshot.getString("TodayRev");
                            if (user != null) {
                                homeamt.setText(user);
                            } else {
                                homeamt.setText("Unknown User");
                            }
                        } else {
                            homeamt.setText("0 Error");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Username.setText("Error: " + e.getMessage());
                    }
                });
    }

    void displayUsername() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("EasyBill")
                .document(currentUser.getUid()) // Replace with actual user ID
                .collection("company_info")
                .document(currentUser.getUid());

        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String user = documentSnapshot.getString("companyName");
                            if (user != null) {
                                Username.setText(user);
                            } else {
                                Username.setText("Unknown User");
                            }
                        } else {
                            Username.setText("Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Username.setText("Error: " + e.getMessage());
                    }
                });
    }
}


