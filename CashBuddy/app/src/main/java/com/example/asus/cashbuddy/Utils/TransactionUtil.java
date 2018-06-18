package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by steve on 2/10/2018.
 */

public final class TransactionUtil {

    // Firebase path
    private static final String FIREBASE_PATH = "transactions";

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static DatabaseReference insert(Transaction transaction) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).push();
        reference.setValue(transaction);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
