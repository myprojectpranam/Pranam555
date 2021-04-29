package com.example.pranam555.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import notinuse.Contactsusers;
import com.example.pranam555.MainActivity;
import com.example.pranam555.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ImageView imgGroupInfoIcon;
    private TextView txtInfoDescription,txtCreatedBy,txtEditGroup
            ,txtAddParticipant,txtLeaveGroup,txtParticipants;
    private RecyclerView infoParticipantsRecyclerView;
    private String myGroupRole = "";
    private FirebaseAuth mAuth;
    private ArrayList<Contactsusers> groupUsersArraylist;
    private AdapterPartcipantsAddForGroup adapterPartcipantsAddForGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_info);


        imgGroupInfoIcon = findViewById(R.id.imgGroupInfoIcon);
        txtInfoDescription=findViewById(R.id.txtInfoDescription);
        txtCreatedBy = findViewById(R.id.txtCreatedBy);
        txtEditGroup = findViewById(R.id.txtEditGroup);
        txtAddParticipant = findViewById(R.id.txtAddParticipant);
        txtLeaveGroup = findViewById(R.id.txtLeaveGroup);
        txtParticipants = findViewById(R.id.txtParticipants);
        infoParticipantsRecyclerView = findViewById(R.id.infoParticipants);

        groupId = getIntent().getExtras().getString("groupId");

        mAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();


        txtAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(GroupInfoActivity.this,NewGroupParticipantsAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);


            }
        });

        txtEditGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);

            }
        });

        txtLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If user is participant/admin:leave group
                //if user is creator:delete group

                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButton = "";

                if (myGroupRole.equals("creator")){

                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to Delete group permanently?";
                    positiveButton = "Delete";


                }else {

                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to Leave group permanently?";
                    positiveButton = "LEAVE";

                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);

                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (myGroupRole.equals("creator")){

                                    deleteGroup();

                                }else {

                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        }).show();

            }
        });


    }

    private void deleteGroup(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                Toast.makeText(GroupInfoActivity.this,"Group deleted Successfully",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this,MainActivity.class));
                finish();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(GroupInfoActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void leaveGroup(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").child(mAuth.getUid())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(GroupInfoActivity.this,"Group left successfully...",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(GroupInfoActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadGroupInfo(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
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

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            loadCreatorInfo(dateTime,createdBy);

//                            actionBar.setTitle(groupTitle);
                            txtInfoDescription.setText(groupDescription);

                            try {

                                Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(imgGroupInfoIcon);

                            }catch (Exception e){


                            }

                        }

                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadMyGroupRole(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                    myGroupRole = dataSnapshot1.child("role").getValue().toString();
//                    actionBar.setSubtitle(mAuth.getCurrentUser().getPhoneNumber() +" ("+myGroupRole+")");

                    if (myGroupRole.equals("participant")){

                        txtEditGroup.setVisibility(View.GONE);
                        txtAddParticipant.setVisibility(View.GONE);
                        txtLeaveGroup.setText("Leave Group");


                    }else if (myGroupRole.equals("admin")){

                        txtEditGroup.setVisibility(View.GONE);
                        txtAddParticipant.setVisibility(View.VISIBLE);
                        txtLeaveGroup.setText("Leave Group");


                    }else if (myGroupRole.equals("creator")){

                        txtEditGroup.setVisibility(View.VISIBLE);
                        txtAddParticipant.setVisibility(View.VISIBLE);
                        txtLeaveGroup.setText("Delete Group");


                    }
                }

                loadParticiPants();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCreatorInfo(String dateTime, String createdBy) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("my_users");
        databaseReference.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                    String name = dataSnapshot1.child("name").getValue().toString();
                    txtCreatedBy.setText("Created By "+name + " on "+dateTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadParticiPants(){

        groupUsersArraylist = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                    String uid = dataSnapshot1.child("uid").getValue().toString();

                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("my_users");
                    databaseReference1.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {

                            for (DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()){

                                Contactsusers contactsusers = dataSnapshot3.getValue(Contactsusers.class);
                                groupUsersArraylist.add(contactsusers);
                            }
                            adapterPartcipantsAddForGroup = new AdapterPartcipantsAddForGroup
                                    (GroupInfoActivity.this,groupUsersArraylist,groupId,myGroupRole);
                            infoParticipantsRecyclerView.setAdapter(adapterPartcipantsAddForGroup);

                            txtAddParticipant.setText("Participants ("+groupUsersArraylist.size()+")");


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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.group_info).setVisible(true);

        return super.onCreateOptionsMenu(menu);

    }
}