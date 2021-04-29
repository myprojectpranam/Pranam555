package com.example.pranam555.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pranam555.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupChatActivity extends AppCompatActivity {

    private String groupId;
    String myGroupRole="";
    private Toolbar toolbar;
    private CircleImageView groupChatIcon;
    private TextView txtGroupChatTitle,txtUnreadMessage;
    private ImageButton imgAttachFile,btnSendGroupChatMessage;
    private EditText edtGroupChatMessages;
    private RecyclerView groupChatRecyclerView;
    private ArrayList<NewGroupModelChat> groupChatArrayList;
    private AdapterGroupChat adapterGroupChat;
    String fileChecker = "",myUrl="";
    private ProgressDialog loadingBar;
    private Uri fileUri;
    private StorageTask uploadTask;
    String allParticipantsUids;
    String key;
    ValueEventListener seenListener;
    DatabaseReference databaseReference2;
    String timeStamp;
    NewGroupModelChat model;

    private FirebaseAuth mAuth;
    String currentUserID;
    String typedMessage;
    String senderID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_chat);




        loadingBar = new ProgressDialog(this);
        groupChatIcon = findViewById(R.id.groupChatIcon);
        txtGroupChatTitle = findViewById(R.id.txtGroupChatTitle);
        imgAttachFile = findViewById(R.id.imgAttachFile);
        edtGroupChatMessages = findViewById(R.id.edtGroupChatMessages);
        btnSendGroupChatMessage = findViewById(R.id.btnSendGroupChatMessage);
        groupChatRecyclerView = findViewById(R.id.groupChatRecyclerView);
   //     txtUnreadMessage = findViewById(R.id.txtUnreadMessage);
        //   databaseReference = FirebaseDatabase.getInstance().getReference();






        toolbar = findViewById(R.id.group_chat_activity_toolbar);
        setSupportActionBar(toolbar);
        groupId = getIntent().getExtras().getString("groupId");
        senderID = getIntent().getExtras().getString("current");
//        allParticipantsUids = getIntent().getExtras().getString("uid");
//
//        Toast.makeText(NewGroupChatActivity.this,String.valueOf(allParticipantsUids),Toast.LENGTH_LONG).show();

        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser()!=null){

            currentUserID = mAuth.getCurrentUser().getUid();

        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();
       // seenMessage();

        }



     //   unseenMessage1();





        btnSendGroupChatMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                 typedMessage = edtGroupChatMessages.getText().toString().trim();


                if (typedMessage.isEmpty()){

                    Toast.makeText(NewGroupChatActivity.this,"Can't send empty message",Toast.LENGTH_SHORT).show();

                }else {


                    sendMessage(typedMessage);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");


                    databaseReference.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                               // String key = dataSnapshot.getKey();

                            try {

                                String uid = dataSnapshot.getKey();



                                    HashMap<String,Object> messageBody1 = new HashMap<>();
                                    messageBody1.put("from",mAuth.getCurrentUser().getUid());
                                    messageBody1.put("timeStamp",timeStamp);
                                    messageBody1.put("type","text");
                                    messageBody1.put("isseen",false);
                                    messageBody1.put("to",uid);
                                    databaseReference.child(groupId).child("UreadMessage").child(uid).child(timeStamp).setValue(messageBody1);




//                                else if (uid.equals(senderID) && !uid.equals(model.getSender())){
//
//                                    HashMap<String,Object> messageBody1 = new HashMap<>();
//                                    messageBody1.put("sender",mAuth.getCurrentUser().getUid());
//                                    messageBody1.put("timeStamp",timeStamp);
//                                    messageBody1.put("type","text");
//                                    messageBody1.put("isseen","true");
//                                    messageBody1.put("to",uid);
//                                    databaseReference.child(groupId).child("UreadMessage").child(uid).child(timeStamp).setValue(messageBody1);
//
//                                }

//                                else {
//
//                                    HashMap<String,Object> messageBody1 = new HashMap<>();
//                                    messageBody1.put("sender",mAuth.getCurrentUser().getUid());
//                                    messageBody1.put("timeStamp",timeStamp);
//                                    messageBody1.put("type","text");
//                                    messageBody1.put("isseen","false");
//                                    messageBody1.put("to",key);
//                                    databaseReference.child(groupId).child("UreadMessage").child(key).child(timeStamp).setValue(messageBody1);
//
//                                }


                                //    databaseReference.child(groupId).child("Participants").child(uid).child(timeStamp).updateChildren(messageBody1);




                                }catch (Exception e){


                                }

                            }



//                                if (!uid.equals(senderID)){
//
//                                    HashMap<String,Object> messageBody2 = new HashMap<>();
//                                    messageBody2.put("sender",mAuth.getCurrentUser().getUid());
//                                    messageBody2.put("timeStamp",timeStamp);
//                                    messageBody2.put("type","text");
//                                    messageBody2.put("isseen","false");
//                                    messageBody2.put("to",mAuth.getUid());
//
//                                    databaseReference.child(groupId).child("Participants").child(uid).child(timeStamp).setValue(messageBody2);
//
//                                }



//                    if (!uid.equals(currentUserID)){
//
//                        HashMap<String,String> messageBody = new HashMap<>();
//                        messageBody.put("sender",mAuth.getCurrentUser().getUid());
//                        messageBody.put("timeStamp",timeStamp);
//                        messageBody.put("type","text");
//                        messageBody.put("isseen","false");
//
//                        databaseReference.child(groupId).child("Participants").child(uid).child(timeStamp).setValue(messageBody);
//
//                   }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                edtGroupChatMessages.setText("");


            }
        });

      //  unseenMessage1();
        saff();






        imgAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendFiles();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
   //   databaseReference2.child(groupId).child("Participants").removeEventListener(seenListener);

    }

    private void loadMyGroupRole(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                            myGroupRole = dataSnapshot1.child("role").getValue().toString();
                            invalidateOptionsMenu();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message){

        timeStamp = "" + System.currentTimeMillis();
        //Creating database when message is sent
        HashMap<String,String> messageBody = new HashMap<>();
        if (mAuth.getCurrentUser()!=null){
        messageBody.put("sender",mAuth.getCurrentUser().getUid());
        messageBody.put("message",message);
        messageBody.put("timeStamp",timeStamp);
        messageBody.put("type","text");

        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("GpMessages").child(timeStamp)
                .setValue(messageBody);

//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//                if (task.isSuccessful()){
//
//                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups");
//
//                                databaseReference1.child(groupId).child("Participants")
//                                        .addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                            for (DataSnapshot dataSnapshot2:snapshot.getChildren()){
//
//                                                String uid = dataSnapshot2.child("uid").getValue().toString();
//
//
//                                                if (uid.equals(currentUserID)){
//                                                 //   Log.e("uids",model.getSender());
//
//
//                                                    HashMap<String,String> messageBody = new HashMap<>();
//                                                    messageBody.put("sender",mAuth.getCurrentUser().getUid());
//                                                    messageBody.put("timeStamp",timeStamp);
//                                                    messageBody.put("type","text");
//                                                    messageBody.put("isseen","true");
//
//                                                    databaseReference1.child(groupId).child("Participants").child(uid).child(timeStamp).setValue(messageBody);
//
//                                                }
//
//                                                if (!uid.equals(currentUserID) && !uid.equals(model.getSender())){
//
//                                                    HashMap<String,Object> messageBody = new HashMap<>();
//                                                    messageBody.put("sender",mAuth.getCurrentUser().getUid());
//                                                    messageBody.put("timeStamp",timeStamp);
//                                                    messageBody.put("type","text");
//                                                    messageBody.put("isseen","false");
//
//                                                    databaseReference1.child(groupId).child("Participants").child(uid).child(timeStamp).updateChildren(messageBody);
//                                                    Toast.makeText(NewGroupChatActivity.this,timeStamp,Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//
//
//
//
//
//
//
//
//
////                                        else {
////
////                                            HashMap<String,Object> messageBody = new HashMap<>();
//////                                            messageBody.put("timeStamp",timeStamp);
////                                            messageBody.put("sender",mAuth.getCurrentUser().getUid());
//////                                            messageBody.put("message",message);
////////                                            messageBody.put("isseen",false);
//////                                            messageBody.put("to",key);
////
////                                            dataSnapshot.child(timeStamp).getRef().setValue(messageBody);
////
////                                        }
//
//
//
//
//                }
//            }
//        });

    }

    private void sendFiles(){


        CharSequence options [] = new CharSequence[3];

        options[0] = "Images";
        options[1] = "PDF Files";
        options[2] = "Word Files";

        AlertDialog.Builder builder = new AlertDialog.Builder(NewGroupChatActivity.this);
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

                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Group Document Files");


                //Creating a key in the database for every messages///
//                DatabaseReference userMessagesKeyReference = databaseReference.child("Groups")
//                        .child(groupId).push();
//                String messagePushedID = userMessagesKeyReference.getKey();

                String timeStamp = "" + System.currentTimeMillis();

                StorageReference filePath = storageReference.child(timeStamp + "." + fileChecker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {


                                String downloadUrl = uri.toString();

                                Map messageDocseBody = new HashMap();
                                messageDocseBody.put("message",downloadUrl);
                                messageDocseBody.put("name",fileUri.getLastPathSegment());//This is same name url in the storage
                                messageDocseBody.put("sender",currentUserID);
                                messageDocseBody.put("timeStamp",timeStamp);
                                messageDocseBody.put("type",fileChecker);


                                Map messageDocsBodyDetails =new HashMap();
                                messageDocsBodyDetails.put(timeStamp,messageDocseBody);

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("GpMessages");
                                databaseReference.updateChildren(messageDocsBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                loadingBar.dismiss();
                                Toast.makeText(NewGroupChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

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
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Group Image File");

                //Creating a nod directly by the name of Messages in the database

                //Creating a key in the database for every messages///
//                DatabaseReference userMessagesKeyReference = databaseReference.child("Groups")
//                        .child(groupId).push();
//                String messagePushedID = userMessagesKeyReference.getKey();

                String timeStamp = "" + System.currentTimeMillis();

                StorageReference filePath = storageReference.child(timeStamp + "." + "jpg");

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
                            String timeStamp = "" + System.currentTimeMillis();


                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());//This is same name url in the storage
                            messageImageBody.put("sender",currentUserID);
                            messageImageBody.put("timeStamp",timeStamp);
                            messageImageBody.put("type",fileChecker);
                            //   messageImageBody.put("to",groupId);
                            //  messageImageBody.put("messageID",messagePushedID);


                            Map messageImageBodyDetails =new HashMap();
                            messageImageBodyDetails.put(timeStamp,messageImageBody);

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("GpMessages");
                            databaseReference.updateChildren(messageImageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){

                                        loadingBar.dismiss();

                                        Toast.makeText(NewGroupChatActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                                    }else {

                                        loadingBar.dismiss();
                                        Toast.makeText(NewGroupChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                    }
                                    edtGroupChatMessages.setText("");
                                }
                            });


                        }

                    }
                });


            }else {

                loadingBar.dismiss();

                Toast.makeText(NewGroupChatActivity.this,"Nothing Selected",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void loadGroupInfo(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.orderByChild("groupId").equalTo(groupId).addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                            //Getting details of group to show it on group toolbar

                            String groupTitle = (String) dataSnapshot1.child("groupTitle").getValue();
                            String groupDescription = (String) dataSnapshot1.child("groupDescriptiom").getValue();
                            String groupIcon = (String) dataSnapshot1.child("groupIcon").getValue();
                            String timestamp = (String) dataSnapshot1.child("timeStamp").getValue();
                            String createdBy = (String) dataSnapshot1.child("createdBy").getValue();

                            txtGroupChatTitle.setText(groupTitle);

                            try {

                                Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(groupChatIcon);

                            }catch (Exception e){
                                groupChatIcon.setImageResource(R.drawable.group_blue);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadGroupMessages(){



        groupChatArrayList = new ArrayList<>();

        //Retrieving the group message on the screen with the help of adapter
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("GpMessages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        groupChatArrayList.clear();

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            // NewGroupModelChat has message,sender,timestamp,type;
                            // NewGroupModelChat has message,sender,timestamp,type;

                            model = dataSnapshot1.getValue(NewGroupModelChat.class);
                            //Adding these items to the arraylist
                            groupChatArrayList.add(model);


                        }

                        adapterGroupChat = new AdapterGroupChat(NewGroupChatActivity.this,groupChatArrayList,groupId);
                        groupChatRecyclerView.setAdapter(adapterGroupChat);
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
        menu.findItem(R.id.find_friends).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.logout).setVisible(false);



        if (myGroupRole.equals("creator")|| myGroupRole.equals("admin")){

            menu.findItem(R.id.add_Participant).setVisible(true);
        }else {

            menu.findItem(R.id.add_Participant).setVisible(false );

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.add_Participant){

            Intent intent = new Intent(NewGroupChatActivity.this,NewGroupParticipantsAddActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);

        }else if (id==R.id.group_info){

            Intent moveToGroupInfoActivity = new Intent(NewGroupChatActivity.this,GroupInfoActivity.class);
            moveToGroupInfoActivity.putExtra("groupId",groupId);
            startActivity(moveToGroupInfoActivity);

        }
        return super.onOptionsItemSelected(item);
    }

//    private void seenMessage(){
//
//        databaseReference2 = FirebaseDatabase.getInstance().getReference("Groups");
//
//        seenListener = databaseReference2.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
//
//                    String uid = dataSnapshot1.child("uid").getValue().toString();
//
//                    databaseReference2.child(groupId).child("Participants").child(uid)
//
//                            .addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                    for (DataSnapshot dataSnapshot2:snapshot.getChildren()){
//
//                                        try {
//
//                                            String sender = dataSnapshot2.child("sender").getValue().toString();
//                                            String isseen = dataSnapshot2.child("isseen").getValue().toString();
//                                            String to = dataSnapshot2.child("to").getValue().toString();
//                                            String timeStamp = dataSnapshot2.child("timeStamp").getValue().toString();
//
//                                            // String type = dataSnapshot2.child("type").getValue().toString();
//
//                                            if (to.equals(senderID) && !to.equals(sender)){
//
//                                                Toast.makeText(NewGroupChatActivity.this,to,Toast.LENGTH_LONG).show();
//
//                                                if (isseen.equals("false")){
//
//                                                HashMap<String,Object> hashMap = new HashMap<>();
//                                                hashMap.put("isseen","true");
//
//
//                                                    databaseReference2.child(groupId).child("Participants")
//                                                            .child(uid).child(timeStamp).updateChildren(hashMap);
//
//                                                //dataSnapshot2.getRef().updateChildren(hashMap);
//
//
//
//                                                }
//
//                                            }
//
//                                        }catch (Exception e){
//
//
//                                        }
//
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//
////                                databaseReference2.child(groupId).child("GpMessages").addValueEventListener(new ValueEventListener() {
////                                    @Override
////                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
////
////                                        for (DataSnapshot dataSnapshot2:snapshot.getChildren()){
////
////                                            NewGroupModelChat newGroupModelChat = dataSnapshot2.getValue(NewGroupModelChat.class);
////
////                                            if (uid.equals(currentUserID) && newGroupModelChat.isIsseen().equals("false")){
////
////                                                Toast.makeText(NewGroupChatActivity.this,currentUserID,Toast.LENGTH_LONG).show();
////
////                                                HashMap<String,Object> messageBody = new HashMap<>();
////
////                                                messageBody.put("isseen","true");
////
////                                                dataSnapshot1.child(newGroupModelChat.getTimeStamp()).getRef().updateChildren(messageBody);
////
////
////
////
////                                            }
//////                                            else if (!uid.equals(currentUserID)){
//////
//////                                                HashMap<String,Object> hashMap = new HashMap<>();
//////                                                hashMap.put("isseen",false);
//////                                                databaseReference2.child(groupId).child("Participants").child(uid).child(newGroupModelChat.getTimeStamp()).setValue(hashMap);
//////                                                Toast.makeText(NewGroupChatActivity.this,timeStamp,Toast.LENGTH_LONG).show();
//////
//////                                            }
////                                        }
////
////                                    }
////
////                                    @Override
////                                    public void onCancelled(@NonNull DatabaseError error) {
////
////                                    }
////                                });
//                }
//            }
//
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });

//    }

    private void unseenMessage1(){

        databaseReference2 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("UreadMessage").child(currentUserID);
        try {

        seenListener = databaseReference2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            for (DataSnapshot dataSnapshot1:snapshot.getChildren()){

                                                   String sender = dataSnapshot1.child("sender").getValue().toString();
                                                   String isseen = dataSnapshot1.child("isseen").getValue().toString();
                                                    String to = dataSnapshot1.child("to").getValue().toString();
                                                    String timeStamp = dataSnapshot1.child("timeStamp").getValue().toString();

                                              //  NewGroupModelChat newGroupModelChat = dataSnapshot1.getValue(NewGroupModelChat.class);

//                                                    if (sender.equals(currentUserID)){
//
//                                                        HashMap<String,Object> hashMap = new HashMap<>();
//                                                        hashMap.put("isseen","true");
//
//                                                    //    databaseReference2.child(timeStamp).updateChildren(hashMap);
//                                                             dataSnapshot1.getRef().updateChildren(hashMap);
//
//                                                    }

                                                    HashMap<String,Object> hashMap = new HashMap<>();
                                                    hashMap.put("isseen","true");

                                                    if (to.equals(currentUserID)){
                                                      DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Groups")
                                                        .child(groupId).child("UreadMessage")
                                                        .child(to);
                                                    databaseReference.child(timeStamp).updateChildren(hashMap);

                                                    }
//                                                if (!to.equals(sender))
//                                                    dataSnapshot1.getRef().updateChildren(hashMap);




                                            }

                                        }



                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


        }catch (Exception e){


        }




    }

    private void saff(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("UreadMessage");

        seenListener  = databaseReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                                            for (DataSnapshot dataSnapshot1:snapshot.getChildren()){

                                               String uid = dataSnapshot1.getKey();

                                               databaseReference.child(uid)
                                                       .addValueEventListener(new ValueEventListener() {
                                                           @Override
                                                           public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                               for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                                                                   String timeStamp = dataSnapshot.child("timeStamp").getValue().toString();

                                                                   if (uid.equals(senderID) && !uid.equals(model.getSender())){

                                                                       HashMap hashMap = new HashMap();
                                                                       hashMap.put("isseen",true);


                                                                       databaseReference.child(senderID).child(timeStamp).updateChildren(hashMap);

                                                                   }
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


}

