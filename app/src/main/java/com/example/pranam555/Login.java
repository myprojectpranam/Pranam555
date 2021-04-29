package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Login extends AppCompatActivity implements View.OnClickListener {


    private Button btnLogin,btnLoginPhone;
    private EditText edtLogEmail,edtLogPassword;
    private TextView txtLogInForgetPassword,txtLoginNeedNewAccount,txtLoginUsing;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginPhone = findViewById(R.id.btnLoginPhone);
        edtLogEmail = findViewById(R.id.edtLoginEmail);
        edtLogPassword = findViewById(R.id.edtLoginPassword);
        txtLoginNeedNewAccount= findViewById(R.id.txtLoginNeedNeedAccount);
        txtLogInForgetPassword = findViewById(R.id.txtLoginForgetPassword);
        txtLoginUsing = findViewById(R.id.txtLogin_using);
        //It will help to login the user by checking with database
        mAuth = FirebaseAuth.getInstance();
        //It will check weather the current user is logged in or not

        loadingBar = new ProgressDialog(Login.this);

        txtLoginNeedNewAccount.setOnClickListener(Login.this);
        btnLogin.setOnClickListener(Login.this);
        btnLoginPhone.setOnClickListener(Login.this);


    }



    private void sendUserToMainActivity(){

        Intent moveUserToTheMainActivity = new Intent(Login.this,MainActivity.class);
        //This means back button will not applicable in the activity, it will not go back to the log in activity, has to press logout
        moveUserToTheMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(moveUserToTheMainActivity);
        finish();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.txtLoginNeedNeedAccount:
                sendUserToTheRegisterActivity();
                break;

            case R.id.btnLogin:
                allowUserToLogIn();
                break;

            case R.id.btnLoginPhone:
                Intent phoneLogInActivity = new Intent(Login.this,NewphoneActivity.class);
                startActivity(phoneLogInActivity);
                break;
        }
    }


    private void sendUserToTheRegisterActivity() {

        Intent registerActivity = new Intent(Login.this,RegisterActivity.class);
        startActivity(registerActivity);
}


    private void allowUserToLogIn() {


        String email = edtLogEmail.getText().toString();
        String password = edtLogPassword.getText().toString();

        if (email == null || password == null){

            Toast.makeText(Login.this,"Please enter both fields",Toast.LENGTH_SHORT).show();
        }else {

            loadingBar.setTitle("Sign in...Please wait");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){



                        String currentUserID = mAuth.getCurrentUser().getUid();

                        //Device token for notification
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        databaseReference.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){

                                    sendUserToMainActivity();
                                    Toast.makeText(Login.this,"Logged in successful",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });


                    }else {

                        //If error while log in
                        String errorMessage = task.getException().toString();
                        Toast.makeText(Login.this,"Error: " + errorMessage,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }

                }
            });
        }

    }


}
