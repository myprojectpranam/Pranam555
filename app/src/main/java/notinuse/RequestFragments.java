package notinuse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import de.hdodenhof.circleimageview.CircleImageView;
import notinuse.Contactsusers;


public class RequestFragments extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRefrence, my_usersRef,contactsReference;
    private FirebaseAuth mAuth;
    private String currentUserID;



    public RequestFragments() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_request_fragments, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatRequestRefrence = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        my_usersRef = FirebaseDatabase.getInstance().getReference().child("my_users");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList = requestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contactsusers> options = new FirebaseRecyclerOptions.Builder<Contactsusers>()
                //Basically the current user who logged in, we will show the requests by request key in the database
                .setQuery(chatRequestRefrence.child(currentUserID),Contactsusers.class)
                .build();

        FirebaseRecyclerAdapter<Contactsusers,RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contactsusers, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contactsusers model) {

//                holder.itemView.findViewById(R.id.btnAcceptRequest).setVisibility(View.VISIBLE);
//                holder.itemView.findViewById(R.id.btnCancelRequest).setVisibility(View.VISIBLE);

                // Getting all request keys which are inside the current user uid
                String chatRequestUserListKeys = getRef(position).getKey();

                //Because here we are working on received request type, whom sent me the request
                //We are accessing all the received request type from the database
                DatabaseReference getRequestType = getRef(position).child("Request Type").getRef();

                getRequestType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("received")){

                                my_usersRef.child(chatRequestUserListKeys).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){


                                            String requestUserProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserProfileImage).placeholder(R.drawable.profile_image).into(holder.imgUserProfileImage);

                                        }

                                            String requestUserName = dataSnapshot.child("name").getValue().toString();
                                            String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.txtUserName.setText(requestUserName);
                                            holder.txtUserStatus.setText("Wants to connect with you");




                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[] = new CharSequence[2];

                                                options[0] = "Accept";
                                                options[1] = "Cancel";

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName + " Chat Request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {

                                                        //If friend request accepted
                                                        if (position == 0){
                                                            //If request is accepted,Both uids in contacts nod with saved remark in database
                                                            contactsReference.child(currentUserID).child(chatRequestUserListKeys).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        contactsReference.child(chatRequestUserListKeys).child(currentUserID)
                                                                                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    //After request accepted, that request will be removed from request list of app
                                                                                    //by removing the request from chat request which is in database
                                                                                    chatRequestRefrence.child(currentUserID).child(chatRequestUserListKeys)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if (task.isSuccessful()){

                                                                                                chatRequestRefrence.child(chatRequestUserListKeys).child(currentUserID)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(),"New Contact Added",Toast.LENGTH_SHORT).show();
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

                                                                }
                                                            });

                                                        }if (position == 1){

                                                            chatRequestRefrence.child(currentUserID).child(chatRequestUserListKeys)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        chatRequestRefrence.child(chatRequestUserListKeys).child(currentUserID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });


                                                        }

                                                    }

                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else if (type.equals("Sent")){


//                                Button request_sent_btn = holder.itemView.findViewById(R.id.btnAcceptRequest);
//                                request_sent_btn.setText("Request sent");

                         //       holder.itemView.findViewById(R.id.btnCancelRequest).setVisibility(View.INVISIBLE);


                                my_usersRef.child(chatRequestUserListKeys).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){


                                            String requestUserProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestUserProfileImage).placeholder(R.drawable.profile_image).into(holder.imgUserProfileImage);

                                        }

                                        String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.txtUserName.setText(requestUserName);
                                        holder.txtUserStatus.setText("Request sent to " + requestUserName);




                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence options[] = new CharSequence[1];


                                                options[0] = "Cancel sent chat request";

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already sent request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int position) {

                                                        //If friend request accepted
                                                 if (position == 0){

                                                            chatRequestRefrence.child(currentUserID).child(chatRequestUserListKeys)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        chatRequestRefrence.child(chatRequestUserListKeys).child(currentUserID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){

                                                                                    Toast.makeText(getContext(),"Sent request deleted successfully",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });


                                                        }

                                                    }

                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

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
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_display_layout,parent,false);
                RequestsViewHolder requestsViewHolder = new RequestsViewHolder(view);
                return requestsViewHolder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{


        TextView txtUserName,txtUserStatus;
        CircleImageView imgUserProfileImage;
        Button btnAcceptRequest,btnCancelRequest;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txt_user_profile_name);
            txtUserStatus = itemView.findViewById(R.id.txt_user_status);
            imgUserProfileImage = itemView.findViewById(R.id.users_profile_image_find_friends);
//            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
//            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
        }
    }
}
