package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

public class Auth extends Fragment {
    private EditText email, pass;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_auth, container, false);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        Button registrationBtn = (Button) linearLayout.findViewById(R.id.registration);
        Button okBtn = (Button) linearLayout.findViewById(R.id.okAuth);
        email = (EditText) linearLayout.findViewById(R.id.emailAuth);
        pass = (EditText) linearLayout.findViewById(R.id.passwordAuth);

        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                authPlayer();
            }
        });

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GameActivity) getActivity()).fragmentTransaction(new Registration());
            }
        });
        return linearLayout;
    }

    private void authPlayer() {

        final String passwordText = pass.getText().toString().trim();
        final String emailText = email.getText().toString().trim();

        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(getActivity(), getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(getActivity(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        ((GameActivity) getActivity()).hideKeyboard();
        progressDialog.setMessage(getString(R.string.try_to_log_in));
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete()) {
                    final ProgressDialog pd = ProgressDialog.show(getActivity(), "",getString(R.string.please_wait), true);
                    pd.setCancelable(false);
                    pd.show();
                    new AsyncTask<Integer, Integer, String>() {


                        @Override
                        protected String doInBackground(Integer[] objects) {
                            try {
                                TimeUnit.SECONDS.sleep(objects[0]);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String o) {
                            super.onPostExecute(o);
                            pd.dismiss();
                            ((GameActivity) getActivity()).fragmentTransaction(new Profile());

                        }
                    }.execute(2);

                } else {
                    Toast.makeText(getActivity(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                    pass.setText("");
                }
                progressDialog.cancel();
            }
        });
    }
}
