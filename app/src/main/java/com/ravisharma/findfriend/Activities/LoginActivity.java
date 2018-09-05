package com.ravisharma.findfriend.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ravisharma.findfriend.CountryData;
import com.ravisharma.findfriend.Models.UserInfo;
import com.ravisharma.findfriend.R;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputEditText editPhone, editName;
    private Spinner spinner;
    private LinearLayout enterDetail, verifying;

    private FirebaseAuth auth;
    private DatabaseReference db;

    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editName = findViewById(R.id.editTextName);
        editPhone = findViewById(R.id.editTextPhone);
        enterDetail = findViewById(R.id.enterDetailLayout);
        verifying = findViewById(R.id.verifyingLayout);
        progressBar = findViewById(R.id.progressbar);
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("UserInfo");

        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = editPhone.getText().toString().trim();
                String name = editName.getText().toString().trim();

                if (name.isEmpty()) {
                    editName.setError("Please Enter Your Name");
                    editName.requestFocus();
                    return;
                }

                if(spinner.getSelectedItemPosition()==0)
                {
                    Toast.makeText(LoginActivity.this, "Please Select Country", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (num.isEmpty() || num.length() < 10) {
                    editPhone.setError("Valid number is required");
                    editPhone.requestFocus();
                    return;
                }

                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];

                String number = "+"+code+num;

                enterDetail.setVisibility(View.GONE);

                verifying.setVisibility(View.VISIBLE);

                sendVerificationCode(number);

            }
        });
    }

    //Firebase Phone Authentication methods
    private void sendVerificationCode(String number) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d("firebaseexception", String.valueOf(e));
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(LoginActivity.this, "Please Enter Valid Phone Number", Toast.LENGTH_LONG).show();
            } else if (e instanceof FirebaseNetworkException) {
                Toast.makeText(LoginActivity.this, "Make sure you have a working internet", Toast.LENGTH_LONG).show();
            }
            verifying.setVisibility(View.GONE);
            enterDetail.setVisibility(View.VISIBLE);
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String name = editName.getText().toString().trim();
                            String number = editPhone.getText().toString().trim();
                            String uid = auth.getCurrentUser().getUid();

                            UserInfo info = new UserInfo(name, number, uid);

                            db.child(uid).setValue(info);

                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }
}