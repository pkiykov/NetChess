package com.pkiykov.netchess.others;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class FirebaseHelper {
    private DatabaseReference ref;
    private ValueEventListener valueEventListener;
    private ChildEventListener childEventListener;

   public  FirebaseHelper(DatabaseReference ref, ChildEventListener childEventListener) {
        this.childEventListener = childEventListener;
        this.ref = ref;
        ref.addChildEventListener(childEventListener);
    }

    public FirebaseHelper(DatabaseReference ref, ValueEventListener valueEventListener) {
        this.valueEventListener = valueEventListener;
        this.ref = ref;
        ref.addValueEventListener(valueEventListener);
    }

    public DatabaseReference getRef() {
        return ref;
    }

    public ValueEventListener getValueEventListener() {
        return valueEventListener;
    }

    public ChildEventListener getChildEventListener() {
        return childEventListener;
    }
}
