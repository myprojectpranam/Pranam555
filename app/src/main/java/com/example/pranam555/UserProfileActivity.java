package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private String receiverUserID,currentState,senderUserID;

    private CircleImageView img_visit_user_profile_image;
    private TextView txt_visit_user_name,txt_visit_user_profile_status;
    private Button btn_visit_user_send_message_request,btn_decline_by_visited_user;
    private DatabaseReference databaseReference,chatRequestRef,contactsRef,notificationReferece;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        //Accessing all UID's from database first
        databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");
        notificationReferece = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().getString("visit_user_id");
        senderUserID = mAuth.getCurrentUser().getUid();



        //Creating a new reference of chat request inside the database, a separate one
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        img_visit_user_profile_image = findViewById(R.id.img_visit_profile_image);
        txt_visit_user_name = findViewById(R.id.txt_visit_user_name);
        txt_visit_user_profile_status = findViewById(R.id.txt_visit_profile_status);
        btn_visit_user_send_message_request = findViewById(R.id.btn_visit_send_message_request);
        btn_decline_by_visited_user = findViewById(R.id.btn_decline_by_visited_user);
        currentState = "New";

        retrieveVisitUserInfo();


    }

    private void retrieveVisitUserInfo(){

        //In this case ruma clicked my profile, then my profile information will be opened
        //This receiverUserId has my UID from the previous activity
        //Getting information of deepak from database which is in his UID
        databaseReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //As image is an optional , we have to create a separate conditions
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")){

                    String visitUserImage = dataSnapshot.child("image").getValue().toString();
                    String visitUserName = dataSnapshot.child("name").getValue().toString();
                    String visitUserStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(visitUserImage).placeholder(R.drawable.profile_image).into(img_visit_user_profile_image);
                    txt_visit_user_name.setText(visitUserName);
                    txt_visit_user_profile_status.setText(visitUserStatus);

                    manageChatRequest();


                }

                else {
                    //If profile image is not set
                    String visitUserName = dataSnapshot.child("name").getValue().toString();
                    String visitUserStatus = dataSnapshot.child("status").getValue().toString();

                    txt_visit_user_name.setText(visitUserName);
                    txt_visit_user_profile_status.setText(visitUserStatus);

                    manageChatRequest();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void manageChatRequest(){

        //if sender id has value
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receiverUserID)){

                    String requestType = dataSnapshot.child(receiverUserID).child("Request Type").getValue().toString();

                    if (requestType.equals("Sent")){

                        currentState = "Request_sent";
                        btn_visit_user_send_message_request.setText("Cancel chat request");



                    }


                    //This is the receive portion, when receiver received the request and open his/her account
                    else if (requestType.equals("received")){
                        //If the requested user log in , this will display to the sender user and can accept or decline request
                        //Because the received uid is the current user id.
                        Toast.makeText(UserProfileActivity.this,receiverUserID,Toast.LENGTH_SHORT).show();

                        currentState = "request_received";
                        btn_visit_user_send_message_request.setText("Accept Chat Request");
                        btn_decline_by_visited_user.setVisibility(View.VISIBLE);
                        btn_decline_by_visited_user.setEnabled(true);

                        btn_decline_by_visited_user.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                cancelChatRequest();

                            }
                        });
                    }
                }else {

                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(receiverUserID)){

                                currentState = "friends";
                                btn_visit_user_send_message_request.setText("Remove this contact");
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

        if (!senderUserID.equals(receiverUserID)){

            btn_visit_user_send_message_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //If send request button is already clicked it will be disabled
                    btn_visit_user_send_message_request.setEnabled(false);

                    //It means a request has sent to a new person
                    if (currentState.equals("New")){

                        sendChatRequest();
                    }if (currentState.equals("Request_sent")){

                        cancelChatRequest();

                        //If the receiver received the request
                    }if (currentState.equals("request_received")){

                        acceptChatRequest();
                    }if (currentState.equals("friends")){

                        removeSpecificContact();
                    }
                }
            });


        }else {

            //If the sender and receiver is same,send request button will be invisible
            btn_visit_user_send_message_request.setVisibility(View.INVISIBLE);
        }

    }

    //This method will be called when send request button will be pressed
    private void sendChatRequest(){

        //Storing sender and receiver uid with sent message
        //For the sender it will the sent and for the receiver if will be received in the database
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("Request Type").setValue("Sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("Request Type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){


                                                //Creating notification for new request, in database also
                                                HashMap<String,String> chatNotification = new HashMap<>();
                                                chatNotification.put("from",senderUserID);
                                                chatNotification.put("type","request");
                                                //We have to give unique key for every notification,thats why we use push
                                                //Inside the receiver uid, we are putting chat notification details
                                                notificationReferece.child(receiverUserID).push()
                                                        .setValue(chatNotification).addOnCompleteListener
                                                        (new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    //If the request sent the button will change to cancel chat request
                                                                    btn_visit_user_send_message_request.setEnabled(true);
                                                                    currentState = "Request_sent";
                                                                    //After request is sent, Button will be shown as cancel chat request
                                                                    btn_visit_user_send_message_request.setText("Cancel chat request");

                                                                }
                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void cancelChatRequest(){

        //Cancelling the request by the sender, removing the request type value from the database and
        // it will be turn to currentstate new again, button will be again send request
        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    chatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                btn_visit_user_send_message_request.setEnabled(true);
                                currentState = "New";
                                btn_visit_user_send_message_request.setText("Send Request");
                                btn_decline_by_visited_user.setVisibility(View.INVISIBLE);
                                btn_decline_by_visited_user.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

    private void acceptChatRequest(){

        //Creating a database when request accepted by the receiver
        //Chat request will be removed as both are now friends
        contactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                //Once request accepted, chat request will be removed
                                                chatRequestRef.child(senderUserID)
                                                        .child(receiverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    chatRequestRef.child(receiverUserID)
                                                                            .child(senderUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    btn_visit_user_send_message_request.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    //After adding there will be a remove option
                                                                                    btn_visit_user_send_message_request.setText("Remove this contacts");

                                                                                    btn_decline_by_visited_user.setVisibility(View.INVISIBLE);
                                                                                    btn_decline_by_visited_user.setEnabled(false);

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void removeSpecificContact(){

        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    contactsRef.child(receiverUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                btn_visit_user_send_message_request.setEnabled(true);
                                currentState = "New";
                                btn_visit_user_send_message_request.setText("Send Request");

                                btn_decline_by_visited_user.setVisibility(View.INVISIBLE);
                                btn_decline_by_visited_user.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }
}
