package com.example.easybill;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Utility {
    static CollectionReference getCollectionReference(){
        FirebaseUser currentUser = FirebaseAuth.getInstance() .getCurrentUser() ;
        return FirebaseFirestore.getInstance(). collection("EasyBill")
            .document(currentUser .getUid()).collection( "invoices");
    }
}
