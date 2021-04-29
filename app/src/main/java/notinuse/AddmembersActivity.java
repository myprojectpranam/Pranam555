package notinuse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pranam555.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddmembersActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private DatabaseReference databaseReferenceOfContactsOfCurrentUser,currentUsersFriendInfo,databaseReferenceforAddingUserToDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private RecyclerView listForAddingGroupMembers;
    private  ArrayList<GroupMembersModelss> numberOfUsers = new ArrayList<>();
    String groupName;
    String pushID;
    String currentState;
    DatabaseReference groupDatabaseReference;
    DatabaseReference key;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmembers);

        groupName = getIntent().getExtras().getString("group");
        Toast.makeText(AddmembersActivity.this,groupName,Toast.LENGTH_SHORT).show();
        myToolbar = findViewById(R.id.addmembersToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Add Group Members");


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //database reference for showing contacts to the adding memeber screen
        databaseReferenceOfContactsOfCurrentUser = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        currentUsersFriendInfo = FirebaseDatabase.getInstance().getReference().child("my_users");

     //   databaseReferenceforAddingUserToDatabase = FirebaseDatabase.getInstance().getReference().child("Groups_detail").child(currentUserID);
        databaseReferenceforAddingUserToDatabase = FirebaseDatabase.getInstance().getReference().child("Group_details").child(groupName);
        groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("my_users").child(currentUserID).child("Groups");






        listForAddingGroupMembers = findViewById(R.id.listForAddingGroupMembers);
        listForAddingGroupMembers.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<GroupMembersModelss> options = new FirebaseRecyclerOptions.Builder<GroupMembersModelss>()
                .setQuery(databaseReferenceOfContactsOfCurrentUser, GroupMembersModelss.class)
                .build();

        FirebaseRecyclerAdapter<GroupMembersModelss,groupMembersListViewHolder> adapter =
                new FirebaseRecyclerAdapter<GroupMembersModelss, groupMembersListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull groupMembersListViewHolder holder, int position, @NonNull GroupMembersModelss model) {


                        String currentUsersFriendsUIDS = getRef(position).getKey();

                        currentUsersFriendInfo.child(currentUsersFriendsUIDS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    if (dataSnapshot.hasChild("image")){

                                        String profileImage = dataSnapshot.child("image").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.txtUserProfileUsername.setText(profileName);
                                        holder.txtUserProfileStatus.setText(profileStatus);
                                        Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.imgUserImage);
                                        //holder.btnAddingMembers.setText("Add Members");
                                         holder.btnAddingMembers.setVisibility(View.VISIBLE);




                                    }else {

                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.txtUserProfileUsername.setText(profileName);
                                        holder.txtUserProfileStatus.setText(profileStatus);
                                        holder.btnAddingMembers.setVisibility(View.VISIBLE);



                                    }
                                }

                                holder.btnAddingMembers.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(AddmembersActivity.this,"asfaf",Toast.LENGTH_SHORT).show();
//                                        databaseReferenceforAddingUserToDatabase.child(groupName)
//                                                .child(gettingAllKeyForAddingExactUserAsPerPosition).setValue("Added")
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                                        if (task.isSuccessful()){
//
//                                                            holder.btnAddingMembers.setText("Remove");
//                                                        }
//
//                                                    }
//                                                });
                       //                 String groupId = databaseReferenceforAddingUserToDatabase.push().getKey();



                                        //Getting the users key according to the button
                                    //    String id = groupDatabaseReference.push().getKey();

                                        String gettingAllKeyForAddingExactUserAsPerPosition = getRef(position).getKey();


                                        databaseReferenceforAddingUserToDatabase.child("members").child(gettingAllKeyForAddingExactUserAsPerPosition).setValue("Added")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            currentState = "Added";
                                                        }

                                                    }
                                                });


//                                        Query query = databaseReferenceforAddingUserToDatabase.orderByChild("members");
//                                        ValueEventListener valueEventListener = new ValueEventListener() {                                           @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {//
////                                            Map<String,Object> memberskeys = new HashMap<>();
////                                            memberskeys.put("members",gettingAllKeyForAddingExactUserAsPerPosition);
////
////                                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
////
////                                                key = dataSnapshot1.child("members").getRef();
////                                            }
//
//
//
//
//
//
//                                        }
//
//                                            @Override
//                                            public void onCancelled(@NonNull DatabaseError error) {
//
//                                            }
//                                        };
//
//                                        query.addValueEventListener(valueEventListener);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {


                            }


                        });




                    }

                    @NonNull
                    @Override
                    public groupMembersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_display_layout,parent,false);

                        groupMembersListViewHolder GroupMembersListViewHolder = new groupMembersListViewHolder(view);





                        return GroupMembersListViewHolder;
                    }
                };

        listForAddingGroupMembers.setAdapter(adapter);
        adapter.startListening();



    }

    public static class groupMembersListViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imgUserImage;
        public TextView txtUserProfileStatus,txtUserProfileUsername;
        public Button btnAddingMembers;

        public groupMembersListViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUserImage = itemView.findViewById(R.id.users_profile_image_find_friends);
            txtUserProfileUsername = itemView.findViewById(R.id.txt_user_profile_name);
            txtUserProfileStatus =itemView.findViewById(R.id.txt_user_status);
//            btnAddingMembers =itemView.findViewById(R.id.btnAddingMembers);




        }
    }
    private void addMemberToDatabase(){


    }
}