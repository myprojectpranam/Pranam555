package com.example.pranam555;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pranam555.MessageAdapter;
import com.example.pranam555.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String messageReceiverID,messageReceiverName,messageReceiverImage;
    private TextView txtUserName,txtLastSeen;
    private CircleImageView imgProfileImage;
    private Toolbar chatToolbar;
    private ImageButton btnSendMessage,sendFileButton,imgVideoCallBtn,imgMakeAudioCall;
    private EditText edtInputTypeMessage;
    private FirebaseAuth mAuth;
    private String messageSenderID;
    private DatabaseReference databaseReference,databaseReference1,databaseReference2;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private  String saveCurrentTime,saveCurrentDate;
    private String fileChecker = "",myUrl="",calledBy="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    boolean notify = false;
    FirebaseUser fUser;
    String messageSenderReference;
    String messageReceiverReference;
    String messagePushedID;
//    SinchClient sinchClient;
//    Call call;
//    AudioCall audioCall;

    ValueEventListener seenListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser()!=null){
            messageSenderID = mAuth.getCurrentUser().getUid();
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();


        try {
            messageReceiverID = getIntent().getExtras().getString("uid");
            messageReceiverName = getIntent().getExtras().getString("name");
            messageReceiverImage = getIntent().getExtras().getString("photo");

        }catch (Exception e){

        }


        chatToolbar = findViewById(R.id.chat_Toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        txtUserName = findViewById(R.id.custom_profile_name);
        txtLastSeen = findViewById(R.id.custom_user_lastSeen);
        imgProfileImage = findViewById(R.id.custom_profile_image);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        edtInputTypeMessage = findViewById(R.id.edt_input_type_msg);
        sendFileButton = findViewById(R.id.btnSendFiles);
        loadingBar = new ProgressDialog(ChatActivity.this);
        imgVideoCallBtn = findViewById(R.id.videoCallBtn);
        imgMakeAudioCall = findViewById(R.id.makeAudioCall);


        messageAdapter = new MessageAdapter(messagesList,messageReceiverID,ChatActivity.this);
        userMessagesList = findViewById(R.id.private_chat_msg_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        seenMessage(messageReceiverID);

        txtUserName.setText(messageReceiverName);

        try {

            Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(imgProfileImage);

        }catch (Exception e){

            imgProfileImage.setImageResource(R.drawable.profile_image);

        }



        imgProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent fullImage = new Intent(ChatActivity.this,fullprofileImageView.class);
                fullImage.putExtra("fullImage",messageReceiverImage);
                startActivity(fullImage);

            }

        });



        imgVideoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent callingActivity = new Intent(ChatActivity.this,CallingActivty.class);
                callingActivity.putExtra("visit_user_id",messageReceiverID);

                startActivity(callingActivity);
            }
        });

        imgMakeAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (messageReceiverID!=null){


                Intent callingActivity = new Intent(ChatActivity.this,AudioCall.class);
                callingActivity.putExtra("visit_user_id_forAudioCall",messageReceiverID);
                callingActivity.putExtra("visit_user_id_whoMakeCall",messageSenderID);

                startActivity(callingActivity);
                }
            }
        });



        btnSendMessage.setOnClickListener(ChatActivity.this);
        sendFileButton.setOnClickListener(ChatActivity.this);



        displayLastSeen();



        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());





        try {

            //Whenever the new child will be added or messages it will show us to the screen,like forloop of firebase
            databaseReference.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                            Messages messages = dataSnapshot.getValue(Messages.class);
                            //Adding message to message list or arraylist
                            messagesList.add(messages);
                            //messageslist or arrylist already added to adapter, whenever new message added adapter will retrive to the recycler view
                            messageAdapter.notifyDataSetChanged();
                            //Whenever new message, automatically scrolling the screen for new message, it will set to recyclerview
                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }catch (Exception e){


        }



    }




    private void displayLastSeen(){

       databaseReference.child("my_users").child(messageReceiverID).addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("userState").hasChild("state")){

                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){

                                txtLastSeen.setText("online");

                            }else if (state.equals("offline")){

                                txtLastSeen.setText("Last seen: " + date + " " + time);
                            }
                        }else {

                            txtLastSeen.setText("offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnSendMessage:
                sendMessage();

                break;

            case R.id.btnSendFiles:
                sendFiles();
                break;

        }
    }



    private void sendMessage(){

        notify = true;
        //Getting the value of edittext or typed message
        String messageText = edtInputTypeMessage.getText().toString();
        if (messageText.isEmpty()){
            Toast.makeText(ChatActivity.this,"Type a message",Toast.LENGTH_SHORT).show();

        }else{
            //Creating a nod directly by the name of Messages in the database
             messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
             messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;
            //Creating a key in the database for every messages///
            DatabaseReference userMessagesKeyReference = databaseReference.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();
            messagePushedID = userMessagesKeyReference.getKey();

            // message details
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushedID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("isseen",false);



            //Putting the message details separately in sender and receiver uid in the database with same key of one msg
            //It will separate data of sender and receiver information
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderReference + "/" + messagePushedID,messageTextBody);

            messageBodyDetails.put(messageReceiverReference + "/" + messagePushedID,messageTextBody);

            //Now chaining the database in 1 to 1 chat
            databaseReference.updateChildren(messageBodyDetails);


        }
        edtInputTypeMessage.setText("");



    }



    @Override
    protected void onStart() {


        updateUserStatus("online");

        super.onStart();
    }

    private void sendFiles(){

        CharSequence options [] = new CharSequence[3];

        options[0] = "Images";
        options[1] = "PDF Files";
        options[2] = "Word Files";



        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select file");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {

                if (position == 0){

                    fileChecker = "image";


                    //Moving user to gallery to select pic to send
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    //Moving user to gallery to select pic to send
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select image"),1000);


                }if (position == 1){

                    fileChecker = "pdf";

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent.createChooser(intent,"Select pdf file"),1000);


                }if (position == 2){


                    fileChecker = "docx";

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent.createChooser(intent,"Select word file"),1000);


                }

            }
        });

        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending file");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            

            //Getting image from gallery and storing it to fileUri
            fileUri = data.getData();

            if (!fileChecker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

                //Creating a key in the database for every messages///
                DatabaseReference userMessagesKeyReference = databaseReference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();
                messagePushedID = userMessagesKeyReference.getKey();

                StorageReference filePath = storageReference.child(messagePushedID + "." + fileChecker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {


                                String downloadUrl = uri.toString();

                                Map messageDocseBody = new HashMap();
                                messageDocseBody.put("message",downloadUrl);
                                messageDocseBody.put("name",fileUri.getLastPathSegment());
                                messageDocseBody.put("type",fileChecker);
                                messageDocseBody.put("from",messageSenderID);
                                messageDocseBody.put("to",messageReceiverID);
                                messageDocseBody.put("messageID",messagePushedID);
                                messageDocseBody.put("time",saveCurrentTime);
                                messageDocseBody.put("date",saveCurrentDate);

                                Map messageDocsBodyDetails =new HashMap();
                                messageDocsBodyDetails.put(messageSenderReference + "/" + messagePushedID,messageDocseBody);
                                messageDocsBodyDetails.put(messageReceiverReference + "/" + messagePushedID, messageDocseBody);

                                databaseReference.updateChildren(messageDocsBodyDetails);
                                loadingBar.dismiss();
                            }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progess = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        loadingBar.setMessage((int) progess + " % uploading....");
                    }
                });


            }else if (fileChecker.equals("image")){

                //Creating a separate folder of image which user is sending to other users
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image File");

                //Creating a nod directly by the name of Messages in the database
                String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

                //Creating a key in the database for every messages///
                DatabaseReference userMessagesKeyReference = databaseReference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();
                String messagePushedID = userMessagesKeyReference.getKey();

                StorageReference filePath = storageReference.child(messagePushedID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){

                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()){

                            //Getting url of the image so that it shows to the other user
                            Uri downloadUrl = (Uri) task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());//This is same url in the storage
                            messageImageBody.put("type",fileChecker);
                            messageImageBody.put("from",messageSenderID);
                            messageImageBody.put("to",messageReceiverID);
                            messageImageBody.put("messageID",messagePushedID);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);

                            Map messageImageBodyDetails =new HashMap();
                            messageImageBodyDetails.put(messageSenderReference + "/" + messagePushedID,messageImageBody);
                            messageImageBodyDetails.put(messageReceiverReference + "/" + messagePushedID, messageImageBody);

                            databaseReference.updateChildren(messageImageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){

                                        loadingBar.dismiss();

                                        Toast.makeText(ChatActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                                    }else {

                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                    }
                                    edtInputTypeMessage.setText("");
                                }
                            });


                        }

                    }
                });


            }else {

                loadingBar.dismiss();

                Toast.makeText(ChatActivity.this,"Nothing Selected",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){

            databaseReference1.removeEventListener(seenListener);

            updateUserStatus("offline");
        }

    }

    @Override
    protected void onResume() {

        updateUserStatus("online");
        super.onResume();
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

        messageSenderID = mAuth.getCurrentUser().getUid();

        databaseReference.child("my_users").child(messageSenderID).child("userState")
                .updateChildren(onlineStateMap);

    }

    private void seenMessage(String userId){

        databaseReference1 = FirebaseDatabase.getInstance().getReference("Messages").child(messageSenderID).child(messageReceiverID);

        seenListener = databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    Messages messages = dataSnapshot1.getValue(Messages.class);

                    if (messages.getTo()!=null){
                    if (messages.getTo().equals(messageSenderID) && messages.getFrom().equals(userId)){

                        Map hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        dataSnapshot1.getRef().updateChildren(hashMap);
//                                .addOnCompleteListener(new OnCompleteListener() {
//                            @Override
//                            public void onComplete(@NonNull Task task) {
//
//                                if (task.isSuccessful()){
//
//
//                                    databaseReference2 = FirebaseDatabase.getInstance().getReference("Messages").child(messageReceiverID).child(messageSenderID);
//
//                                    databaseReference2.addValueEventListener(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                            for (DataSnapshot dataSnapshot2:snapshot.getChildren()){
//
//                                                Messages messages1 = dataSnapshot2.getValue(Messages.class);
//
//                                                if (messages1.getFrom().equals(userId)){
//
//                                                    Map hashMap = new HashMap<>();
//                                                    hashMap.put("isseen",true);
//                                                    dataSnapshot2.getRef().updateChildren(hashMap);
//                                                }
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });
//
//
//                                }
//                            }
//                        });
                    }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
