package com.example.pranam555.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import notinuse.Contactsusers;
import com.example.pranam555.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPartcipantsAddForGroup extends RecyclerView.Adapter<AdapterPartcipantsAddForGroup.HolderParticipantsAdd> {

    private Context context;
    private ArrayList<Contactsusers> usersArrayList;
    private String groupId,myGroupRole;//creator/admin/participants


    public AdapterPartcipantsAddForGroup(Context context, ArrayList<Contactsusers> usersArrayList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_participants_add_activity,parent,false);
        HolderParticipantsAdd holderParticipantsAdd = new HolderParticipantsAdd(view);
        return holderParticipantsAdd;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, int position) {

        Contactsusers users = usersArrayList.get(position);
        String name = users.getName();
        String status = users.getStatus();
        String image = users.getImage();
        String uid = users.getUid();
        String phone = users.getPhoneNumber();

        holder.txt_user_profile_name.setText(name);
        holder.txt_user_status.setText(status);

        try {

            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.users_profile_image_for_add_group);

        }catch (Exception e){


        }

        checkIfAlreadyExists(users,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if user already exist
                //if added:show remove participants/make-admin/remove-admin option (admin not able to change the role of creator)
                //if not added,show add participants option

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                databaseReference.child(groupId).child("Participants").child(users.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    //user Exists

                                    String hisPreviousRole = dataSnapshot.child("role").getValue().toString();

                                    String[] options;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){

                                            options = new String[]{"Remove admin","Remove Participant"};

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int position) {

                                                    if (position==0){

                                                        removeAdmin(users);

                                                    }else {

                                                        removeParticipant(users);

                                                    }

                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){

                                            options = new String[]{"Make admin","Remove Participant"};

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int position) {

                                                    if (position==0){

                                                        makeAdmin(users);

                                                    }else {

                                                        removeParticipant(users);

                                                    }

                                                }
                                            }).show();
                                        }
                                    }else if (myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){

                                            Toast.makeText(context,"Creator of group..",Toast.LENGTH_SHORT).show();
                                        }else if (hisPreviousRole.equals("admin")){

                                            options = new String[]{"Remove admin","Remove Participant"};

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int position) {

                                                    if (position==0){

                                                        removeAdmin(users);

                                                    }else {

                                                        removeParticipant(users);

                                                    }

                                                }
                                            }).show();


                                        }else if (hisPreviousRole.equals("participant")){

                                            options = new String[]{"Make admin","Remove participant"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int position) {

                                                    if (position==0){

                                                        makeAdmin(users);

                                                    }else {

                                                        removeParticipant(users);

                                                    }

                                                }
                                            }).show();
                                        }
                                    }


                                }else {

                                    //User doesn't exist/not participant:add

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    addParticipant(users);

                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();

                                        }
                                    }).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });


    }


    private void removeAdmin(Contactsusers user){



        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").child(user.getUid())
                .updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(context,"The user is no longer admin",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });



    }

    private void removeParticipant(Contactsusers user){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").child(user.getUid())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



    }

    private void makeAdmin(Contactsusers user){


        //Change role
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        //Update role
        databaseReference.child(groupId).child("Participants").child(user.getUid())
                .updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(context,"The user is now admin",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void addParticipant (Contactsusers user){

        String timeStamp =""+System.currentTimeMillis();
        HashMap<String,String> addParticipantBody = new HashMap<>();
        addParticipantBody.put("uid",user.getUid());
        addParticipantBody.put("role","participant");
        addParticipantBody.put("timestamp",timeStamp);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").child(user.getUid())
                .setValue(addParticipantBody)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent intent = new Intent(context,NewGroupChatActivity.class);
                        intent.putExtra("uid",user.getUid());
                        context.startActivity(intent);
                        Toast.makeText(context,"Added Successfully",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });



    }

    private void checkIfAlreadyExists(Contactsusers users,HolderParticipantsAdd holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("Participants").child(users.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            //If already exists,define his role creator/admin or participant
                            String hisRole = dataSnapshot.child("role").getValue().toString();
                            holder.txt_user_status.setText(hisRole);

                        }else {




                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class HolderParticipantsAdd extends RecyclerView.ViewHolder {

        private CircleImageView users_profile_image_for_add_group;
        private TextView txt_user_profile_name,txt_user_status;


        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);

            users_profile_image_for_add_group = itemView.findViewById(R.id.users_profile_image_find_friends);
            txt_user_profile_name = itemView.findViewById(R.id.txt_user_profile_name);
            txt_user_status = itemView.findViewById(R.id.txt_user_status);
        }
    }

}
