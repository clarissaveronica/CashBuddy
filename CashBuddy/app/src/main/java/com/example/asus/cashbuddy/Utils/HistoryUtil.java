package com.example.asus.cashbuddy.Utils;

import com.example.asus.cashbuddy.Model.History;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public final class HistoryUtil {

    // Firebase path to transactions
    private static final String FIREBASE_PATH = "history";

    public static DatabaseReference insert(History history, String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FIREBASE_PATH).child(uid).push();
        reference.setValue(history);
        return reference;
    }

    public static Query query() {
        return FirebaseDatabase.getInstance().getReference(FIREBASE_PATH);
    }

    public static Query query(String orderBy, String value) {
        return query().orderByChild(orderBy).equalTo(value);
    }
}
