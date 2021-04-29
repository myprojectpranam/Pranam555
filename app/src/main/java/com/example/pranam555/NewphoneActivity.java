package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class NewphoneActivity extends AppCompatActivity implements View.OnClickListener {

    private CountryCodePicker ccp;
    private EditText numberText;
    private EditText codeText;
    private Button btnContinue;
    private String checker = "",phoneNumber = "";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newphone);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        numberText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        btnContinue = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(numberText);

        btnContinue.setOnClickListener(NewphoneActivity.this);

        mcallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                loadingBar.dismiss();
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(NewphoneActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.VISIBLE);

                btnContinue.setText("Continue");
                codeText.setVisibility(View.GONE);


            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId =s;
                mResendToken = forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";

                btnContinue.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();

                Toast.makeText(NewphoneActivity.this,"Code sent",Toast.LENGTH_SHORT).show();


            }
        };
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.continueNextButton:
                if (btnContinue.getText().equals("Submit")|| checker.equals("Code Sent")){

                    String verificationCode = codeText.getText().toString();

                    if (verificationCode==null){

                        Toast.makeText(NewphoneActivity.this,"Write the code first",Toast.LENGTH_SHORT).show();
                    }else {

                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait");
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);

                    }


                }else {

                    phoneNumber = ccp.getFullNumberWithPlus();

                    if (!phoneNumber.equals("")){

                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait");
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);

                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(mAuth)
                                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(this)                 // Activity (for callback binding)
                                        .setCallbacks(mcallbacks)          // OnVerificationStateChangedCallbacks
                                        .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);


                    }else {

                        Toast.makeText(this,"Please enter valid number",Toast.LENGTH_SHORT).show();
                    }
                }

                break;

        }

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user!=null){

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users")
                                        .child(user.getUid());
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){

                                            Map<String,Object> phoneNumber = new HashMap<>();
                                            phoneNumber.put("phone",user.getPhoneNumber());
                                            databaseReference.updateChildren(phoneNumber);

//                                            updateToken(FirebaseInstanceId.getInstance().getToken());
//
                                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
//
//
                                            if (mAuth.getCurrentUser()!=null){

                                            String currentUserID = mAuth.getCurrentUser().getUid();

                                            DatabaseReference  databaseReference1 = FirebaseDatabase.getInstance().getReference();
                                            databaseReference1.child("Tokens").child(currentUserID).setValue(deviceToken);

                                            }

//                                            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
//                                            SharedPreferences.Editor editor = sp.edit();
//                                            editor.putString("CURRENT_USERID",currentUserID);
//                                            editor.apply();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            loadingBar.dismiss();
                            Toast.makeText(NewphoneActivity.this,"Logged in successfully",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {

                            String error = task.getException().toString();
                            Toast.makeText(NewphoneActivity.this,error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity (){

        Intent moveUserToMainActivity = new Intent(NewphoneActivity.this,MainActivity.class);
        startActivity(moveUserToMainActivity);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null){

            Intent homeIntent = new Intent(NewphoneActivity.this,MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

//    private void updateToken (String token){
//
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token mToken = new Token(token);
//        databaseReference.child(mAuth.getUid()).setValue(mToken);
//    }
}