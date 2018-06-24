package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.Withdraw;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public final class WithdrawUtil {

    // Firebase path to transactions
    private static final String FIREBASE_PATH = "withdrawrequest";

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static DatabaseReference insert(Withdraw withdraw) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).push();
        reference.setValue(withdraw);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
