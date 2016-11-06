package com.pkiykov.netchess.fragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.pojo.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class Registration extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText password, email, login;
    private DatePicker birthdate;
    private ProgressDialog progressDialog;
    private static Player player;

    public Registration() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_registration, container, false);
        initViews(linearLayout);
        birthdate.setMinDate(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40000));
        birthdate.setMaxDate(System.currentTimeMillis());
        birthdate.updateDate(2000, 0, 1);
        progressDialog = new ProgressDialog(getActivity());
        mAuth = FirebaseAuth.getInstance();
        linearLayout.getBackground().setAlpha(80);
        return linearLayout;
    }

    private void initViews(LinearLayout linearLayout) {
        Button ok = (Button) linearLayout.findViewById(R.id.ok);
        Button cancel = (Button) linearLayout.findViewById(R.id.cancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        login = (EditText) linearLayout.findViewById(R.id.login);
        password = (EditText) linearLayout.findViewById(R.id.password);
        email = (EditText) linearLayout.findViewById(R.id.email);
        birthdate = (DatePicker) linearLayout.findViewById(R.id.date_picker);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                registerUser();
                break;
            case R.id.cancel:
                goToTopFragment();
                break;
        }
    }

    public void goToTopFragment() {
        Fragment fragment;
        if (mAuth.getCurrentUser() != null) {
            fragment = new Profile();
        } else {
            fragment = new Auth();
        }
        ((GameActivity) getActivity()).fragmentTransaction(fragment);
    }


    private void registerUser() {

        final String passwordText = password.getText().toString().trim();
        final String emailText = email.getText().toString().trim();
        final String nameText = login.getText().toString();
        if (TextUtils.isEmpty(nameText)) {
            Toast.makeText(getActivity(), getString(R.string.enter_login), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(getActivity(), getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(getActivity(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage(getString(R.string.registering_player));
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (mAuth.getCurrentUser() != null) {
                        int month = birthdate.getMonth() + 1;
                        String date = birthdate.getYear() + "-" + String.format("%02d", month) + "-" + String.format("%02d", birthdate.getDayOfMonth());
                        player = new Player();
                        player.setBirthdate(date);
                        player.setId(mAuth.getCurrentUser().getUid());
                        player.setName(nameText);
                        FirebaseDatabase.getInstance().getReference().child(Player.PLAYERS).child(player.getId()).setValue(player);
                        progressDialog.cancel();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(player.getName()).build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates);
                        Toast.makeText(getActivity(), getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                    }
                    goToTopFragment();
                } else {
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressDialog.cancel();
                }
            }


        });
    }

    public static Player getPlayer() {
        return player;
    }

    public static void setPlayer(Player player) {
        Registration.player = player;
    }
}

