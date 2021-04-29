package com.example.pranam555;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.pranam555.ui.NewGroupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import notinuse.FindFriends;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private TabLayout tableLayout;
    private DatabaseReference databaseReference,databaseReferenceforgroupNode,dtbaseReference,databaseReference2;
    String messagePushedID,calledBy="";
    private String currentUserID;
    String saveCurrentTime,saveCurrentDate;
    public static final int REQUEST_READ_CONTACTS = 79;
    String[] mPermission = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.my_Toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setTitle("wgatsapp");


        getPermission();


        viewPager = findViewById(R.id.viewPager);
        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        tableLayout = findViewById(R.id.tabLayout);
        tableLayout.setupWithViewPager(viewPager);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("0");
        Fragment fragment1 = (Fragment) fragment;

        mAuth = FirebaseAuth.getInstance();
        //It will check weather the current user is logged in or not

        //It will give the reference of the database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceforgroupNode = FirebaseDatabase.getInstance().getReference().child("Group_details");
//       dtbaseReference = FirebaseDatabase.getInstance().getReference().child("Group_details").child(messagePushedID);





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
       menu.findItem(R.id.Create_Group).setVisible(true);
       menu.findItem(R.id.search).setVisible(true);
        menu.findItem(R.id.find_friends).setVisible(true);
        menu.findItem(R.id.settings).setVisible(true);
        menu.findItem(R.id.logout).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){

            case R.id.logout:
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLogInActivity();

                break;

            case R.id.settings:
                sendUserToTheSettingsActivity();
                break;

            case R.id.find_friends:
                sendUserToTheFindFriendsActivity();
                break;

//            case R.id.createGroup:
//                requestNewGroup();
//                break;

            case R.id.Create_Group:

                Intent moveToNewGroupActivity = new Intent(MainActivity.this, NewGroupActivity.class);
                startActivity(moveToNewGroupActivity);
                break;



        }
        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if the user is not logged in
        if (currentUser==null) {

            sendUserToLogInActivity();

        }else {

            updateUserStatus("online");

            VerifyUserExistenceInDataBase();
        }
    }





    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){

            updateUserStatus("offline");
        }

    }

    @Override
    protected void onResume() {

        updateUserStatus("online");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){

            updateUserStatus("offline");
        }
    }

    private void sendUserToLogInActivity() {

        Intent logInIntent = new Intent(MainActivity.this,Login.class);
        logInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logInIntent);
    }

    private void sendUserToTheSettingsActivity(){

        Intent moveUserToTheSettingsActivity = new Intent(MainActivity.this,SettingsActivity.class);
        //Back button will not work in this activity
       // moveUserToTheSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(moveUserToTheSettingsActivity);
       // finish();
    }

    private void sendUserToTheFindFriendsActivity(){

        Intent moveUserToTheFindFriendsActivity = new Intent(MainActivity.this, FindFriends.class);
        startActivity(moveUserToTheFindFriendsActivity);
    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        //From this edittext we will get the group name
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Group name");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = groupNameField.getText().toString();

                if (groupName == null){

                    Toast.makeText(MainActivity.this,"Please enter group name",Toast.LENGTH_SHORT).show();
                }else {

                    //Putting the group information to the databse
                    newGroupCreated(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();



            }
        });

        builder.show();

    }

    private void VerifyUserExistenceInDataBase(){

        //First check with the database that UID exist or not, or deepak has created an account or not?
        String currentUserID = mAuth.getCurrentUser().getUid();

        databaseReference.child("my_users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("name").exists()){

                    //If in the database name exist against UID, it means deepak is not a new user, deepak can start using the app
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();

                }else {

                    //if deepak is a new user, he will send to setting activity to update the profile first
                    sendUserToTheSettingsActivity();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    private void newGroupCreated(final String groupName){
//
//        databaseReference.child("Groups").child(currentUserID).child(groupName).setValue("")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//
//                        if (task.isSuccessful()){
//
//                            Toast.makeText(MainActivity.this,groupName + " group is created successfully",Toast.LENGTH_SHORT).show();
//                        }
//
//
//                    }
//                });

        private void newGroupCreated(final String groupName){


        //Here we are getting the new group unique key and assigning the same key to the group node also
            DatabaseReference userMessagesKeyReference = databaseReference.child("my_users")
                    .child(currentUserID).child("Groups").push();

            //Here is the unique key
            messagePushedID = userMessagesKeyReference.getKey();

            databaseReference.child("my_users").child(currentUserID).child("Groups").child(messagePushedID).child("group_name")
                    .setValue(groupName)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {


                                if (task.isSuccessful()){



                                    databaseReference.child("my_users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                            if (dataSnapshot.hasChild("name")){

                                                String adminName = dataSnapshot.child("name").getValue().toString();



                                            DatabaseReference keys = databaseReference.child("Group_details")
                                                    .child(messagePushedID).push();
                                            //Here is the unique key
                                            String  uniqueKeyForAdmin = keys.getKey();



                                            HashMap<String,Object> groupDatabase = new HashMap<>();
                                            groupDatabase.put("adminID",uniqueKeyForAdmin);
                                            groupDatabase.put("id",messagePushedID);
                                            groupDatabase.put("adminName",adminName);
                                            groupDatabase.put("createdAt",saveCurrentDate);
                                         //   groupDatabase.put("members","");
                                            groupDatabase.put("name",groupName);

                                            currentUserID = mAuth.getCurrentUser().getUid();

                                            databaseReference.child("Group_details").child(messagePushedID).updateChildren(groupDatabase);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                                }

                                Toast.makeText(MainActivity.this,groupName + " group is created successfully",Toast.LENGTH_SHORT).show();



                        }
                    });

    }

    private void updateUserStatus(String state){



        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){

        currentUserID = mAuth.getCurrentUser().getUid();

        }

        databaseReference.child("my_users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);





    }


    private void getPermission(){

        if (Build.VERSION.SDK_INT>=23){
            if (ActivityCompat.checkSelfPermission(this,mPermission[0])
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,mPermission[1])!=PackageManager.PERMISSION_GRANTED)  {
                ActivityCompat.requestPermissions(this,mPermission,REQUEST_READ_CONTACTS);
            }

        }


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                } else {

                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }




}


