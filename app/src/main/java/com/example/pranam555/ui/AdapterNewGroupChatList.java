package com.example.pranam555.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pranam555.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNewGroupChatList extends RecyclerView.Adapter<AdapterNewGroupChatList.HolderNewGroupChatList> {

    private Context context;
    private ArrayList<NewGroupModelChatList> groupModelChatLists;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();



    public AdapterNewGroupChatList(Context context, ArrayList<NewGroupModelChatList> groupModelChatLists) {
        this.context = context;
        this.groupModelChatLists = groupModelChatLists;
    }

    @NonNull
    @Override
    public HolderNewGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_row_groupchat_list,parent,false);
        HolderNewGroupChatList holderNewGroupChatList = new HolderNewGroupChatList(view);
        return holderNewGroupChatList;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNewGroupChatList holder, int position) {

        holder.txtName.setText("");
        holder.txtMessage.setText("");
        holder.txtTime.setText("");

        NewGroupModelChatList model = groupModelChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();


        loadLastMessage(model,holder);

        unreadMessageCount(model,holder);

        holder.txtGroupTitle.setText(groupTitle);

        try {

            Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(holder.imgGroupIcon);

        }catch (Exception e){

            holder.imgGroupIcon.setImageResource(R.drawable.group_blue);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent moveInsideTheGroupChatActivity = new Intent(context,NewGroupChatActivity.class);
                moveInsideTheGroupChatActivity.putExtra("groupId",groupId);
                moveInsideTheGroupChatActivity.putExtra("current",currentUser);
                context.startActivity(moveInsideTheGroupChatActivity);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");



            }
        });

    }

    @Override
    public int getItemCount() {
        return groupModelChatLists.size();
    }




    public static class HolderNewGroupChatList extends RecyclerView.ViewHolder {

        private CircleImageView imgGroupIcon;
        private TextView txtGroupTitle,txtName,txtMessage,txtTime,txtPhotoRemark,txtPdfRemark,txtUnreadMessage;
        private ImageView ImgImageMessage,ImgPdfMessage;

        public HolderNewGroupChatList(@NonNull View itemView) {
            super(itemView);

            imgGroupIcon = itemView.findViewById(R.id.imgGroupIcon);
            txtGroupTitle = itemView.findViewById(R.id.txtGroupTitle);
            txtName = itemView.findViewById(R.id.txtName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            ImgImageMessage = itemView.findViewById(R.id.ImgImageMessage);
            txtPhotoRemark = itemView.findViewById(R.id.txtPhotoRemark);
            ImgPdfMessage = itemView.findViewById(R.id.ImgPdfMessage);
            txtPdfRemark = itemView.findViewById(R.id.txtPdfRemark);
            txtUnreadMessage = itemView.findViewById(R.id.txtUnreadMessage);

        }
    }

    private void loadLastMessage(NewGroupModelChatList model,HolderNewGroupChatList holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(model.groupId).child("GpMessages").limitToLast(1)//get Last item message from the child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                            String lastMessage = dataSnapshot1.child("message").getValue().toString();
                            String timeStamp = dataSnapshot1.child("timeStamp").getValue().toString();
                            String sender = dataSnapshot1.child("sender").getValue().toString();
                            String messageType = dataSnapshot1.child("type").getValue().toString();



                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timeStamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();


                            if (messageType.equals("text")){

                            holder.txtMessage.setVisibility(View.VISIBLE);
                            holder.txtMessage.setText(lastMessage);
                            holder.txtTime.setText(dateTime);

                            }else if (messageType.equals("image")){

                                holder.ImgImageMessage.setVisibility(View.VISIBLE);
                                holder.txtTime.setText(dateTime);
                                holder.txtPhotoRemark.setVisibility(View.VISIBLE);


                            }else if (messageType.equals("pdf")){

                                holder.ImgPdfMessage.setVisibility(View.VISIBLE);
                                holder.txtTime.setText(dateTime);
                                holder.txtPdfRemark.setVisibility(View.VISIBLE);
                            }
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("my_users");
                            databaseReference1.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){

                                                String name = dataSnapshot2.child("name").getValue().toString();
                                                holder.txtName.setText(name);
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



    private void unreadMessageCount(NewGroupModelChatList model,HolderNewGroupChatList holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        databaseReference.child(model.getGroupId()).child("UreadMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                    Log.e("id",model.getGroupId());

                 //   String uid = dataSnapshot.child("uid").getValue().toString();

                    String key = dataSnapshot.getKey();


                    databaseReference.child(model.getGroupId()).child("UreadMessage").child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {

                            int countUnread = 0;
                            for (DataSnapshot dataSnapshot1:dataSnapshot2.getChildren()){

                            try {
                                NewGroupModelChat newGroupModelChat = dataSnapshot1.getValue(NewGroupModelChat.class);


                                assert newGroupModelChat != null;
                                if (newGroupModelChat.isIsseen().equals("false") &&  !newGroupModelChat.getSender().equals(key)){

                                    if (key.equals(currentUser)){

                                        countUnread = countUnread + 1;

                                    }

                                    if (countUnread==0){

                                        holder.txtUnreadMessage.setVisibility(View.GONE);
                                        Log.e("ssdd",Integer.toString(countUnread));

                                    }else {

                                        holder.txtUnreadMessage.setVisibility(View.VISIBLE);
                                        holder.txtUnreadMessage.setText(Integer.toString(countUnread));
                                        holder.txtMessage.setTypeface(null,Typeface.BOLD);
                                        Log.e("ddss",Integer.toString(countUnread));

                                    }
                                }


                            }catch (Exception e){


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









