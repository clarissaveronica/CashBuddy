package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.example.asus.cashbuddy.Model.SplitBill;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public final class SplitBillUtil {

    // Firebase path to transactions
    private static final String FIREBASE_PATH = "splitbill";

    public static DatabaseReference insert(SplitBill splitBill) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).push();
        reference.setValue(splitBill);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
