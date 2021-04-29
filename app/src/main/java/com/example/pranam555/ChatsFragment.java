package com.example.pranam555;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import notinuse.Contactsusers;


public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatListRecyclerView;
    private DatabaseReference chatReference,allUsersReference,databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserId,calledBy="";
    private FirebaseUser fUser;
    String currentUserFriendsKeys;
    String forwardImage;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
//        forwardImage = this.getArguments().getString("forwardImage");


        if (mAuth.getCurrentUser()!=null){

            currentUserId = mAuth.getCurrentUser().getUid();

        //As we want to show the chat list of current user, we need to access the contacts saved in the database and retrieve them in the app
        //Because in Contact nod, all the key are saved or attached of accepted friends of the current user.
       chatReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId);



        }

        allUsersReference = FirebaseDatabase.getInstance().getReference("my_users");
        chatListRecyclerView = privateChatsView.findViewById(R.id.chat_list_recyclerview);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));




        return privateChatsView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.add_Participant).setVisible(false );
        menu.findItem(R.id.group_info).setVisible(false);

        menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //Called when user presses search button from keyboard
            @Override
            public boolean onQueryTextSubmit(String text) {

                if (!text.isEmpty()){
                    //Search text contain text



                }else {

                    getAllUserList();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {

                //Called when user presses any single letter
                if (!text.isEmpty()){

                    //Search text contain text


                }else {

                    getAllUserList();


                }
                return false;
            }
        });
    }



    @Override
    public void onStart() {

        getAllUserList();
        super.onStart();


    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgProfileImage;
        TextView txtUserName,txtUserMessage,txtMessageTime,txtPhotoRemark1,txtPdfRemark1,txtUnreadMessage;

        ImageView imgPhotoMessage1,imgPdfMessage1,imgDoubleTick;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfileImage = itemView.findViewById(R.id.users_profile_image_find_friends);
            txtUserName = itemView.findViewById(R.id.txt_user_profile_name);
            txtUserMessage = itemView.findViewById(R.id.txt_last_message);
            txtMessageTime = itemView.findViewById(R.id.txtMessageTime);
            imgPhotoMessage1 = itemView.findViewById(R.id.imgPhotoMessage1);
            txtPhotoRemark1 = itemView.findViewById(R.id.txtPhotoRemark1);
            imgPdfMessage1 = itemView.findViewById(R.id.imgPdfMessage1);
            txtPdfRemark1 = itemView.findViewById(R.id.txtPdfRemark1);
            imgDoubleTick = itemView.findViewById(R.id.imgDoubleTick);
            txtUnreadMessage = itemView.findViewById(R.id.txtUnreadMessage);
        }
    }

    private void getAllUserList(){

        if (mAuth.getCurrentUser()!=null){

            checkForReceivingAudioCall();
            checkForReceivingCall();

        FirebaseRecyclerOptions<Contactsusers> options =
                new FirebaseRecyclerOptions.Builder<Contactsusers>()
                        .setQuery(chatReference,Contactsusers.class)
                        .build();

        FirebaseRecyclerAdapter<Contactsusers,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contactsusers, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contactsusers model) {

                        //Because in the chatReference above we are accessing the current user uid from the contacts nod
                        //where all the saved or friends key are available
                        currentUserFriendsKeys = getRef(position).getKey();
                        //Because of multiple pics in database it uses array to pick the correct index
                        final String[] retrieveProfileImage = {"default_image"};


                        allUsersReference.child(currentUserFriendsKeys).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    if (dataSnapshot.hasChild("image")){

                                        retrieveProfileImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retrieveProfileImage[0]).placeholder(R.drawable.profile_image).into(holder.imgProfileImage);

                                    }


                                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.txtUserName.setText(retrieveUserName);


                                    chatReference.child(currentUserFriendsKeys).limitToLast(1).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            for (DataSnapshot dataSnapshot1:snapshot.getChildren()){

                                                unseenMessagesCount(holder);

                                                String message = dataSnapshot1.child("message").getValue().toString();
                                                String time = dataSnapshot1.child("time").getValue().toString();
                                                String messageType = dataSnapshot1.child("type").getValue().toString();
                                                String from = dataSnapshot1.child("from").getValue().toString();



                                                if (from.equals(mAuth.getCurrentUser().getUid())){

                                                if (messageType.equals("text")){

                                                    holder.txtUserMessage.setVisibility(View.VISIBLE);
                                                    holder.txtMessageTime.setVisibility(View.VISIBLE);
                                                    holder.txtMessageTime.setText(time);
                                                    holder.txtUserMessage.setText(message);
                                                    holder.imgDoubleTick.setVisibility(View.GONE);





                                                }else if (messageType.equals("image")){

                                                    holder.imgPhotoMessage1.setVisibility(View.VISIBLE);
                                                    holder.txtPhotoRemark1.setVisibility(View.VISIBLE);
                                                    holder.txtMessageTime.setVisibility(View.VISIBLE);

                                                    holder.txtMessageTime.setText(time);
                                                    holder.imgDoubleTick.setVisibility(View.GONE);


                                                }else if (messageType.equals("pdf")){

                                                    holder.imgPdfMessage1.setVisibility(View.VISIBLE);
                                                    holder.txtPdfRemark1.setVisibility(View.VISIBLE);
                                                    holder.txtMessageTime.setVisibility(View.VISIBLE);

                                                    holder.txtMessageTime.setText(time);
                                                    holder.imgDoubleTick.setVisibility(View.GONE);



                                                }

                                                }else {

                                                    if (messageType.equals("text")){

                                                        holder.txtUserMessage.setVisibility(View.VISIBLE);
                                                        holder.txtMessageTime.setVisibility(View.VISIBLE);

                                                        holder.txtMessageTime.setText(time);

                                                        holder.txtUserMessage.setText(message);
                                                        holder.imgDoubleTick.setVisibility(View.VISIBLE);

                                                    }else if (messageType.equals("image")){

                                                        holder.imgPhotoMessage1.setVisibility(View.VISIBLE);
                                                        holder.txtPhotoRemark1.setVisibility(View.VISIBLE);
                                                        holder.txtMessageTime.setVisibility(View.VISIBLE);

                                                        holder.txtMessageTime.setText(time);
                                                        holder.imgDoubleTick.setVisibility(View.VISIBLE);


                                                    }else if (messageType.equals("pdf")){

                                                        holder.imgPdfMessage1.setVisibility(View.VISIBLE);
                                                        holder.txtPdfRemark1.setVisibility(View.VISIBLE);
                                                        holder.txtMessageTime.setVisibility(View.VISIBLE);

                                                        holder.txtMessageTime.setText(time);
                                                        holder.imgDoubleTick.setVisibility(View.VISIBLE);



                                                    }

                                                }


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });




                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent chatIntent = new Intent(getActivity(),ChatActivity.class);
                                            chatIntent.putExtra("uid",currentUserFriendsKeys);
                                            chatIntent.putExtra("name",retrieveUserName);
                                            chatIntent.putExtra("photo", retrieveProfileImage[0]);
                                            startActivity(chatIntent);


                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_display_layout,parent,false);
                        ChatsViewHolder chatsViewHolder = new ChatsViewHolder(view);
                        return chatsViewHolder;

                    }
                };

        chatListRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    }

    private void checkForReceivingCall() {

        //if this id is receiving the call from someone
        //It this receiver uid has ringing child, it will send to calling activity
        allUsersReference.child(currentUserId)
                .child("Ringing")
                //It will continously listen if any update in the database
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("ringing")){

                            calledBy = dataSnapshot.child("ringing").getValue().toString();
                            Intent callingIntent = new Intent(getActivity(),CallingActivty.class);
                            callingIntent.putExtra("visit_user_id",calledBy);

                            startActivity(callingIntent);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkForReceivingAudioCall(){

        allUsersReference.child(currentUserId).child("AudioCallRinging")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("ringing")){

                            calledBy = dataSnapshot.child("ringing").getValue().toString();
                            Intent audioCallIntent = new Intent(getActivity(),AudioCall.class);
                            audioCallIntent.putExtra("visit_user_id_forAudioCall",currentUserId);
                            audioCallIntent.putExtra("visit_user_id_forAudioCall",calledBy);
                            startActivity(audioCallIntent);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void unseenMessagesCount(ChatsViewHolder holder){

      DatabaseReference  databaseReference2 = FirebaseDatabase.getInstance().getReference("Messages").child(currentUserId).child(currentUserFriendsKeys);
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int countUnread = 0;

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if (messages.getTo().equals(currentUserId) && !messages.isIsseen()){

                        countUnread = countUnread + 1;
                    }
                }
                if (countUnread == 0){

                   holder.txtUnreadMessage.setVisibility(View.GONE);

                }else {

                   holder.txtUnreadMessage.setVisibility(View.VISIBLE);
                    holder.txtUnreadMessage.setText(Integer.toString(countUnread));

                    holder.txtUserMessage.setTypeface(null, Typeface.BOLD);



                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}






