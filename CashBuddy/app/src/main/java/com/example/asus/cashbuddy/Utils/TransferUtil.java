package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.Transfer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public final class TransferUtil {

    // Firebase path
    private static final String FIREBASE_PATH = "transfer";

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static DatabaseReference insert(Transfer transfer) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).push();
        reference.setValue(transfer);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
