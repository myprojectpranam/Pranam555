package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRegister;
    private EditText edtRegisterEmail,edtRegisterPassword;
    private TextView txtRegisterAlreadyHaveAnAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = findViewById(R.id.btnRegister);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        txtRegisterAlreadyHaveAnAccount = findViewById(R.id.txtRegisterAlreadyHaveAnAccount);
        loadingBar = new ProgressDialog(RegisterActivity.this);

        //This will help to signup user with email and password
        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        txtRegisterAlreadyHaveAnAccount.setOnClickListener(RegisterActivity.this);
        btnRegister.setOnClickListener(RegisterActivity.this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.txtRegisterAlreadyHaveAnAccount:
                sendUserToLoginActivity();
                break;

            case R.id.btnRegister:

                createNewAccount();

                break;
        }




    }
    private void sendUserToLoginActivity() {

        Intent moveUserToLoginActivity = new Intent(RegisterActivity.this,Login.class);
        startActivity(moveUserToLoginActivity);
    }


    private void createNewAccount() {

        String email = edtRegisterEmail.getText().toString();
        String password = edtRegisterPassword.getText().toString();

        if (email==null || password == null){

            Toast.makeText(RegisterActivity.this,"Please enter both feilds",Toast.LENGTH_SHORT).show();

        }else {

            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait... while we are creating new account");
            //until the account is not created it will not disappear
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //if task or account is created with email and password
                    if (task.isSuccessful()){

                        //Token for notification
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        //We can access the uid to create database of the current user
                        String currentUserID = mAuth.getCurrentUser().getUid();
                        // deepdutt27@gmail.com created an account and a special UID created for deepak in the realtime database
                        databaseReference.child("my_users").child(currentUserID).child("device_token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){


                                        sendUserToMainActivity();
                                        Toast.makeText(RegisterActivity.this,"Account created",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                        }

                                    }
                                });
//                        databaseReference.child("my_users").child("device_token").setValue(deviceToken).addOnCompleteListener
//                                (new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        sendUserToMainActivity();
//                                        Toast.makeText(RegisterActivity.this,"Account created",Toast.LENGTH_SHORT).show();
//                                        loadingBar.dismiss();
//                                    }
//                                });

                    }else {

                        //It will show what type of error while creating the account
                        String errorMessage = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + errorMessage,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }

    private void sendUserToMainActivity(){

        Intent moveUserToTheMainActivity = new Intent(RegisterActivity.this,MainActivity.class);
        moveUserToTheMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(moveUserToTheMainActivity);
        finish();
    }
}
