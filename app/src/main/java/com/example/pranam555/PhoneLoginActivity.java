package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edtPhoneNumberLogInInput,edtVerificationCode;
    Button btnSendVerificationCode,btnVerifyCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        edtPhoneNumberLogInInput = findViewById(R.id.edtPhoneNumberLoginInput);
        edtVerificationCode = findViewById(R.id.verificationCode);
        btnSendVerificationCode = findViewById(R.id.btnSendVerificationCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(PhoneLoginActivity.this);

        btnSendVerificationCode.setOnClickListener(PhoneLoginActivity.this);
        btnVerifyCode.setOnClickListener(PhoneLoginActivity.this);


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                //if the code verification completed successfully
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();
                //If something wrong
                Toast.makeText(PhoneLoginActivity.this,"Invalid phone number,Please enter with country code",Toast.LENGTH_SHORT).show();

                btnSendVerificationCode.setVisibility(View.VISIBLE);
                edtPhoneNumberLogInInput.setVisibility(View.VISIBLE);

                btnVerifyCode.setVisibility(View.INVISIBLE);
                edtVerificationCode.setVisibility(View.INVISIBLE);


            }

            //This method will be called when code is sent to a mobile phone
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
//                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this,"Code sent to your number",Toast.LENGTH_SHORT).show();

                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                edtPhoneNumberLogInInput.setVisibility(View.INVISIBLE);

                btnVerifyCode.setVisibility(View.VISIBLE);
                edtVerificationCode.setVisibility(View.VISIBLE);



            }
        };
    }




    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btnSendVerificationCode:

                //Getting the phone number first
                String phoneNumber = edtPhoneNumberLogInInput.getText().toString();

                if (phoneNumber.isEmpty()){

                    Toast.makeText(PhoneLoginActivity.this,"Please enter phone number first",Toast.LENGTH_SHORT).show();
                }else {

                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    //Server getting the phone number, will send the code in 60 seconds
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks);

//                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
//                            .setPhoneNumber(phoneNumber)
//                            .setTimeout(60L,TimeUnit.SECONDS)
//                            .setActivity(PhoneLoginActivity.this)
//                            .setCallbacks(callbacks).build();
//
//                    PhoneAuthProvider.verifyPhoneNumber(options);

//                    PhoneAuthProvider.verifyPhoneNumber(
//                            PhoneAuthOptions
//                            .newBuilder(FirebaseAuth.getInstance())
//                            .setActivity(PhoneLoginActivity.this)
//                            .setPhoneNumber(phoneNumber)
//                            .setTimeout(60L,TimeUnit.SECONDS)
//                            .setCallbacks(callbacks)
//                            .build()
//                    );


                }

                break;




            case R.id.btnVerifyCode:


                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                edtPhoneNumberLogInInput.setVisibility(View.INVISIBLE);

                String verificationCode = edtVerificationCode.getText().toString();
                if (verificationCode.isEmpty()){

                    Toast.makeText(PhoneLoginActivity.this,"Please write verification code",Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Code verification");
                    loadingBar.setMessage("Please wait...We are verifying the code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    //If the the same code sent, then signin
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }

                break;
        }

    }


    private void sendUserToTheMainActivity(){

        Intent moveUserToTheMainActivity = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(moveUserToTheMainActivity);
        finish();
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            String currentUserID = mAuth.getCurrentUser().getUid();

                            //Device token for notification
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("Tokens").child(currentUserID).setValue(deviceToken);
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Logged in Successfully",Toast.LENGTH_SHORT).show();
                            sendUserToTheMainActivity();


                        } else {


                            String errorMessage = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
