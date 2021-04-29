package com.example.pranam555;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import notinuse.Contactsusers;


/**
 * A simple {@link Fragment} subclass.
 */
public class Contacts extends Fragment {

    private View contacts_list_view;
    private RecyclerView myContactsRecyclerViewList;
    private DatabaseReference contactsDatabaseReference,currentUsersFriendsReference;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public Contacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contacts_list_view = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsRecyclerViewList = contacts_list_view.findViewById(R.id.contact_list);
        myContactsRecyclerViewList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //This will show the current user contacts who is logged in , each current user has their own contacts, because in the database
        // each user uid is attached to the friends user id
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        currentUsersFriendsReference = FirebaseDatabase.getInstance().getReference().child("my_users");


        return contacts_list_view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Basically we want all the friends uid by using contactsDatabaseReference, because each currentuser uid it has all the keys uid of added friends
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contactsusers>()
                .setQuery(contactsDatabaseReference,Contactsusers.class)
                .build();

        FirebaseRecyclerAdapter<Contactsusers,contactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contactsusers, contactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull contactsViewHolder holder, int position, @NonNull Contactsusers model) {

                        //Getting all friends keys from the option(FirebaseRecyclerOptions) from the contacts of database of the current users
                        //Because options has all the keys of current user of friends
                        String currentUserFriendsUIDS = getRef(position).getKey();
                        //First accessing the all users by currentUsersFriendsReference the accessing the friends UID
                        //which are added as a friend to the current user and then accessing name, status and profile image
                        currentUsersFriendsReference.child(currentUserFriendsUIDS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    if (dataSnapshot.child("userState").hasChild("state")){

                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){

                                            holder.onlineIconStatus.setVisibility(View.VISIBLE);

                                        }if (state.equals("offline")){

                                            holder.onlineIconStatus.setVisibility(View.INVISIBLE);

                                        }
                                    }

                                    else {


                                        holder.onlineIconStatus.setVisibility(View.INVISIBLE);

                                    }


                                    if (dataSnapshot.hasChild("image")){

                                        String profileImage = dataSnapshot.child("image").getValue().toString();
                                        String userName = dataSnapshot.child("name").getValue().toString();
                                        String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.txtuserName.setText(userName);
                                        holder.txtuserStatus.setText(userStatus);
                                        Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.imgprofileImage);

                                    }else {

                                        String userName = dataSnapshot.child("name").getValue().toString();
                                        String userStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.txtuserName.setText(userName);
                                        holder.txtuserStatus.setText(userStatus);


                                    }

                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_display_layout,parent,false);

                        contactsViewHolder viewHolder = new contactsViewHolder(view);
                        return viewHolder;

                    }
                };

        myContactsRecyclerViewList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contactsViewHolder extends RecyclerView.ViewHolder {

        TextView txtuserName,txtuserStatus;
        CircleImageView imgprofileImage;
        ImageView onlineIconStatus;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);

            txtuserName = itemView.findViewById(R.id.txt_user_profile_name);
            txtuserStatus = itemView.findViewById(R.id.txt_user_status);
            imgprofileImage = itemView.findViewById(R.id.users_profile_image_find_friends);
            onlineIconStatus = itemView.findViewById(R.id.img_user_online_status);
        }
    }
    {


    }
}
