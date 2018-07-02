package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public final class PaymentRequestUtil {

    // Firebase path to transactions
    private static final String FIREBASE_PATH = "paymentrequest";

    public static DatabaseReference insert(PaymentRequest request) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).push();
        reference.setValue(request);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
