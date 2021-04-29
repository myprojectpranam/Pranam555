package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivty extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn,acceptCallBtn;

    private String receiverUserId,receiverUserImage,receiverUserName;
    private String senderUserId,senderUserImage,senderUserName,checker="";
    private DatabaseReference databaseReference;
    private String callingID ="",ringingID="";

    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_activty);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().getString("visit_user_id");
        databaseReference = FirebaseDatabase.getInstance().getReference("my_users");


        mediaPlayer = MediaPlayer.create(this,R.raw.ringing);

        nameContact = findViewById(R.id.name_Contact);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancelCall);
        acceptCallBtn = findViewById(R.id.makeCall);

        getAndSetUserProfileInfo();




        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                cancelCallingUser();
                checker = "clicked";

            }

        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                final HashMap<String,Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked","picked");

                databaseReference.child(senderUserId).child("Ringing").updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
////
                                    Intent intent = new Intent(CallingActivty.this,VideoChatActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });


    }

    private  void getAndSetUserProfileInfo(){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUserId).exists()){

                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);

                    try {

                        receiverUserImage = dataSnapshot.child(receiverUserImage).child("image").getValue().toString();

                        Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);


                    }catch (Exception e){

                        e.printStackTrace();
                    }

                }if (dataSnapshot.child(senderUserId).exists()){

                    try {

                        senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();

                    }catch (Exception e){

                        e.printStackTrace();
                    }
                    senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();

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

        try {

            mediaPlayer.start();
            databaseReference.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //This conditions means the receiver is not busy on another call
                    //If cancelled button is not clicked
                    if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){


                        final HashMap<String,Object> callingInfo = new HashMap<>();

                        callingInfo.put("calling",receiverUserId); // calling to store in the sender id

                        databaseReference.child(senderUserId).child("Calling").updateChildren(callingInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            final HashMap<String,Object> ringingInfo = new HashMap<>();
                                            ringingInfo.put("ringing",senderUserId);//called by store in the receiver id

                                            databaseReference.child(receiverUserId).child("Ringing")
                                                    .updateChildren(ringingInfo);

                                        }

                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (Exception e){


        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //Here sender is current user
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")){

                    acceptCallBtn.setVisibility(View.VISIBLE);

                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){

                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivty.this,VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void cancelCallingUser() {

        databaseReference.child(senderUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")){

                            callingID = dataSnapshot.child("calling").getValue().toString();


                            //Callingid has receiver UID
                        ;    databaseReference.child(callingID)
                                    .child("Ringing")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        databaseReference.child(senderUserId)
                                                .child("Calling")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {


                                             Intent intent = new Intent(CallingActivty.this,ChatActivity.class);
                                             intent.putExtra("uid",callingID);
                                             intent.putExtra("name",receiverUserName);
                                             intent.putExtra("photo",receiverUserImage);
                                             startActivity(intent);



                                            }
                                        });
                                    }

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

     //  senderUserId is now receiver current user
        databaseReference.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")){

                            ringingID = dataSnapshot.child("ringing").getValue().toString();

                            //ringingID contain the sender calling uid
                            databaseReference.child(ringingID)
                                    .child("Calling")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        databaseReference.child(senderUserId)
                                                .child("Ringing")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                startActivity(new Intent(CallingActivty.this,MainActivity.class));
                                                finish();

                                            }
                                        });
                                    }

                                }
                            });
                        }
//                        else {
//
//                            startActivity(new Intent(CallingActivty.this,ChatActivity.class));
//                            finish();
//
//                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void candel(String callingID){

        databaseReference.child(callingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.hasChild("Ringing")){

                    startActivity(new Intent(CallingActivty.this,MainActivity.class));
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}