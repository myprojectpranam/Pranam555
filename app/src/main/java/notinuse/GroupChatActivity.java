package notinuse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pranam555.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private ImageButton btnSendMessage;
    private EditText edtTypeMessage;
    private ScrollView myScrollView;
    private TextView  txtGroupChat;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;
    private FirebaseAuth mAuth;
    private Button btnAddMembers;

    private DatabaseReference databaseReference,groupNameRef,groupMessageKeyRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        currentGroupName = getIntent().getExtras().getString("groupName");
        setTitle(currentGroupName);
        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        //With the help of UID we can retrieve the username
        currentUserID = mAuth.getCurrentUser().getUid();
        //This database reference is used to get information of the current username and that will display to the group
        databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");
        //This groupNameRef is used to store the information of the target current group in the Groups root
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        btnAddMembers = findViewById(R.id.btnAddMembers);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        edtTypeMessage = findViewById(R.id.edtinput_typemessage);
        myScrollView = findViewById(R.id.myScrolView);
        txtGroupChat = findViewById(R.id.group_chat_display);
        myToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(myToolbar);
//        getSupportActionBar().setTitle("Group Name");


        getUserInformation();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveMessageToDatabase();
                edtTypeMessage.setText("");

                //No need to scroll, it will automatically scroll down for the latest message like whatsapp
                myScrollView.fullScroll(ScrollView.FOCUS_DOWN);


            }
        });

        btnAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addMembersActivity = new Intent(GroupChatActivity.this, AddmembersActivity.class);
                addMembersActivity.putExtra("group",currentGroupName);
                startActivity(addMembersActivity);

            }
        });




    }

    private void getUserInformation(){

        databaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void saveMessageToDatabase(){

        String message = edtTypeMessage.getText().toString();
        //It is generating a unique id of every message of a same user and inside it has information like deepak, date and time, message
        String messageKey = groupNameRef.push().getKey();

        if (message.isEmpty()){

            Toast.makeText(GroupChatActivity.this,"Please write a message",Toast.LENGTH_SHORT).show();
        }else {

            //Date and time of text with format
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            //hh means AM PM clock and mm means minutes and a for AM and PM
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageDetails = new HashMap<>();
            groupNameRef.updateChildren(groupMessageDetails);
          //  It will create a key to every message inside the database
            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            //It is sending all the information to the database
            groupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    @Override
    protected void onStart() {
        super.onStart ();

        //groupNameRef has an access of an active group. basically accessing the each child of a particular group like name date time and messages
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                //If the current group is exist
                if (dataSnapshot.exists()){

                    displayMessages(dataSnapshot);


                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {


                //If the current group is exist
                //Datasnapshot has all the data of chats
                if (dataSnapshot.exists()){

                    displayMessages(dataSnapshot);


                }
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
    }

    private void displayMessages(DataSnapshot dataSnapshot){

        //It is accessing the data line by line and below we are showing how it will appear.
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){

            //Chat date , accessing the first key in the database
            String chatDate =  (String) ((DataSnapshot) iterator.next()).getValue();
            //Chat message , accessing the second key in the database
            String message = (String) ((DataSnapshot) iterator.next()).getValue();
            //Chat name , accessing the third key in the database
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            //Chat time , accessing the fourth key in the database
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            txtGroupChat.append(chatName + " :\n" + message + "\n" + chatTime + "   " + chatDate + "\n\n\n");

            myScrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }

    }
}
