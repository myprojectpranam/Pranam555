package com.example.pranam555.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import notinuse.Contactsusers;
import com.example.pranam555.CountyToPhonePrefix;
import com.example.pranam555.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewGroupParticipantsAddActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerViewForAddingInGroup;
    private ActionBar actionBar;
    private FirebaseAuth mAuth;
    private String groupId,myGroupRole;
    //private ArrayList<Contactsusers> getAllUsersArrayList;
    //private AdapterPartcipantsAddForGroup adapterPartcipantsAdd;
    private Toolbar toolbar;

   // private RecyclerView contactsRecyclerView;
    private List<Contactsusers> phoneContactsArrayList;
    private ArrayList<Contactsusers> appUsersList;
    private AdapterPartcipantsAddForGroup adapter;
    String PhoneName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_participants);

        groupId = getIntent().getExtras().getString("groupId");

        toolbar = findViewById(R.id.participantToolbar);
        setSupportActionBar(toolbar);
         actionBar = getSupportActionBar();
         actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
//
//        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
//
//        contactsRecyclerView.setHasFixedSize(true);
//        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersRecyclerViewForAddingInGroup = findViewById(R.id.usersRecyclerViewForAddingInGroup);
        usersRecyclerViewForAddingInGroup.setHasFixedSize(true);
        usersRecyclerViewForAddingInGroup.setLayoutManager(new LinearLayoutManager(this));

        phoneContactsArrayList = new ArrayList<>();
        appUsersList = new ArrayList<>();

        loadMyGroupRole();
        loadGroupInfo();




        getContactList();

       // usersRecyclerViewForAddingInGroup = findViewById(R.id.usersRecyclerViewForAddingInGroup);



    }


    private void getContactList(){

        String IsoPrefix = getCountryIso();

        String [] projection = new String[]{
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };

        String selectionFields =  ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        String[] selectionArgs = new String[]{"com.google"};

        Cursor phones = this.getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,selectionFields,selectionArgs,null);

        while (phones.moveToNext()){

            PhoneName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String phoneUri = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            phoneNumber = phoneNumber.replace(" ","");
            phoneNumber = phoneNumber.replace("-","");
            phoneNumber = phoneNumber.replace("(","");
            phoneNumber = phoneNumber.replace(")","");

            if (!String.valueOf(phoneNumber.charAt(0)).equals("+")){

                phoneNumber = IsoPrefix + phoneNumber;
            }

           Contactsusers contactsusers = new Contactsusers(PhoneName,"",phoneUri,"",phoneNumber);
            phoneContactsArrayList.add(contactsusers);


            getUserDetails(contactsusers);
        }
    }

    private void getUserDetails(Contactsusers contactsusers){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");
        Query query = databaseReference.orderByChild("phone").equalTo(contactsusers.getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String phone = "";
                    String name = "";
                    String image = "";
                    String status = "";

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){

                        if (childSnapshot.child("phone").getValue()!=null){

                            phone = childSnapshot.child("phone").getValue().toString();
                        }

                        if (childSnapshot.child("name").getValue()!=null){

                            name = childSnapshot.child("name").getValue().toString();
                        }

                        if (childSnapshot.child("image").getValue()!=null){

                            image = childSnapshot.child("image").getValue().toString();
                        }
                        if (childSnapshot.child("status").getValue()!=null){

                            status = childSnapshot.child("status").getValue().toString();
                        }

                        //childSnapshot.getKey() is the user id of user according to phone number

                        Contactsusers appUsers = new Contactsusers(name,status,image,childSnapshot.getKey(),phone);


                        //Setting the name according to saved name in contact list of phone like whatsapp
                        if (!name.equals(PhoneName)){

                            for (Contactsusers contactIterator:phoneContactsArrayList){

                                if (contactIterator.getPhoneNumber().equals(appUsers.getPhoneNumber())){

                                    appUsers.setName(contactIterator.getName());
                                }
                            }
                        }
                        appUsersList.add(appUsers);
                        adapter = new AdapterPartcipantsAddForGroup(NewGroupParticipantsAddActivity.this,appUsersList,groupId,myGroupRole);

                        usersRecyclerViewForAddingInGroup.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private String getCountryIso(){

        String iso = "";
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);

        if (telephonyManager.getNetworkCountryIso()!=null)

            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))

                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountyToPhonePrefix.getPhone(iso);

    }




    private void loadGroupInfo(){

        DatabaseReference  databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                            String groupId = dataSnapshot1.child("groupId").getValue().toString();
                            String groupTitle = dataSnapshot1.child("groupTitle").getValue().toString();
                            String groupDescription = dataSnapshot1.child("groupDescription").getValue().toString();
                            String groupIcon = dataSnapshot1.child("groupIcon").getValue().toString();
                            String createdBy = dataSnapshot1.child("createdBy").getValue().toString();
                            String timestamp = dataSnapshot1.child("timestamp").getValue().toString();
                           actionBar.setTitle("Add Participants");


                            databaseReference1.child(groupId).child("Participants")
                                    .child(mAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {

                                            if (dataSnapshot2.exists()){

                                                //Getting the role of each uid in the group
                                                myGroupRole = dataSnapshot2.child("role").getValue().toString();
//
                                                actionBar.setTitle(groupTitle + "(" + myGroupRole+")");



                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    private void loadMyGroupRole(){


    }





}