package com.example.pranam555.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pranam555.FullImageActivity;
import com.example.pranam555.MessageAdapter;
import com.example.pranam555.R;
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
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<NewGroupModelChat> groupModelChatsArrayList;
    private FirebaseAuth mAuth;
    String groupId;

    public AdapterGroupChat(Context context, ArrayList<NewGroupModelChat> groupModelChatsArrayList,String groupId) {
        this.context = context;
        this.groupModelChatsArrayList = groupModelChatsArrayList;
        this.groupId = groupId;

        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //If it is sender, it will appear right
        if (viewType==MSG_TYPE_RIGHT){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_groupchat_right,parent,false);
            HolderGroupChat holderGroupChat = new HolderGroupChat(view);
            return holderGroupChat;

        }else {

            //If it is receiver, it will appear left

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_groupchat_left,parent,false);
            HolderGroupChat holderGroupChat = new HolderGroupChat(view);
            return holderGroupChat;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {

        NewGroupModelChat model = groupModelChatsArrayList.get(position);
        String message = model.getMessage();
        String senderUid = model.getSender();
        String timestamp = model.getTimeStamp();
        String messageType = model.getType();
        String isseen = model.isIsseen();
        String to = model.getTo();






        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();


        holder.txtTime.setText(dateTime);


        if (senderUid.equals(mAuth.getCurrentUser().getUid())){

        if (messageType.equals("text")){

            holder.txtGroupMessage.setText(message);
            holder.txtTime.setText(dateTime);
            holder.img_sender_imageview.setVisibility(View.GONE);

        }
        else if (messageType.equals("image")){

            try {

                Picasso.get().load(message).into(holder.img_sender_imageview);
                holder.txtTime.setVisibility(View.GONE);
                holder.txtFileTime.setText(dateTime);
                holder.relative_layout.setVisibility(View.GONE);


            }catch (Exception e){


            }

        }else if (messageType.equals("pdf")|| message.equals("docx")){

            holder.img_sender_imageview.setVisibility(View.VISIBLE);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/pranam555-5dd6d.appspot.com/o/Group%20Image%20File%2Ffile.png?alt=media&token=e9f831f2-e4d4-4531-9b13-ef414e08524e")
                    .into(holder.img_sender_imageview);

            holder.txtTime.setVisibility(View.GONE);
            holder.txtFileTime.setText(dateTime);

            holder.relative_layout.setVisibility(View.GONE);
        }

        }else {

            if (messageType.equals("text")){

                holder.txtGroupMessage.setText(message);
                holder.txtTime.setText(dateTime);
                holder.img_receiver_imageview.setVisibility(View.GONE);

            }
            else if (messageType.equals("image")){

                try {

                    Picasso.get().load(message).into(holder.img_receiver_imageview);
                    holder.txtTime.setVisibility(View.GONE);
                    holder.relativeLayout2.setVisibility(View.GONE);
                    holder.txtFileTime.setText(dateTime);



                }catch (Exception e){


                }

            }else if (messageType.equals("pdf")|| message.equals("docx")){

                holder.img_receiver_imageview.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/pranam555-5dd6d.appspot.com/o/Group%20Image%20File%2Ffile.png?alt=media&token=e9f831f2-e4d4-4531-9b13-ef414e08524e")
                        .into(holder.img_receiver_imageview);

                holder.txtTime.setVisibility(View.GONE);
                holder.relativeLayout2.setVisibility(View.GONE);
                holder.txtFileTime.setText(dateTime);

            }


        }

        if (senderUid.equals(mAuth.getCurrentUser().getUid())){

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Position here is the type of message the user is pressing for delete or any other action
                    //Position knows the type of message because userMessagesList is an array
                    // and it has all the data in sequence according to the database, adapter put the data in userMessagesList
                    if (groupModelChatsArrayList.get(position).getType().equals("pdf")
                            || groupModelChatsArrayList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Download and view this document",
                                "Cancel",
                                "Delete for everyone",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            //Here positions is the Choices in the alert dialog
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    //Here the position items to be deleted which are mentioned in the method, holder is the item on the screen
                                //    deleteSentMessages(position,holder);


                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupModelChatsArrayList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (positions==1){



                                } if (positions==2){


                                    deleteMessagesForEveryOne(position,holder);

                                }
                            }
                        });

                        builder.show();
                    }

                    else  if (groupModelChatsArrayList.get(position).getType().equals("text")){

                        CharSequence[] options = new CharSequence[]{

                                "Delete for everyone",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions==0){

                                    deleteMessagesForEveryOne(position,holder);


                                }else if (positions==2){

                                }
                            }
                        });

                        builder.show();
                    }
                    else if (groupModelChatsArrayList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "View this Image",
                                "Cancel",
                                "Delete for everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    Intent intent = new Intent(holder.itemView.getContext(), FullImageActivity.class);
                                    //Because the url is saved in the message nod in database
                                    intent.putExtra("url",groupModelChatsArrayList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (positions==1){



                                }else if (positions==2){

                                     deleteMessagesForEveryOne(position,holder);

                                }
                            }
                        });

                        builder.show();
                    }
                }
            });
        }       else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (groupModelChatsArrayList.get(position).getType().equals("pdf")
                            || groupModelChatsArrayList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{


                                "Download and view this document",
                                "Cancel",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupModelChatsArrayList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (positions==1){



                                }
                            }
                        });

                        builder.show();
                    }

//                    else  if (groupModelChatsArrayList.get(position).getType().equals("text")){
//
//                        CharSequence options[] = new CharSequence[]{
//
//
//                        };
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
//                        builder.setTitle("Delete?");
//
//                        builder.setItems(options, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int positions) {
//
//
//                            }
//                        });
//
//                        builder.show();
//                    }
                    else if (groupModelChatsArrayList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "View this Image",
                                "Cancel",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){


                                    Intent intent = new Intent(holder.itemView.getContext(),FullImageActivity.class);
                                    //Because the url is saved in the message nod in database
                                    intent.putExtra("url",groupModelChatsArrayList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (positions==1){




                                }
                            }
                        });

                        builder.show();
                    }
                }
            });

        }

        setUserName(model,holder);





    }

    private void setUserName(NewGroupModelChat model,HolderGroupChat holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("my_users");
        databaseReference.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                            String senderName = (String) dataSnapshot1.child("name").getValue();

                            holder.txtName.setText(senderName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return groupModelChatsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {


        //If it is sender, it will appear right

        if (groupModelChatsArrayList.get(position).getSender().equals(mAuth.getCurrentUser().getUid())){

            return MSG_TYPE_RIGHT;
        }else {

            //If it is receiver, it will appear left

            return MSG_TYPE_LEFT;
        }


    }

    public static class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView txtName,txtGroupMessage,txtTime,txtFileTime;
        ImageView img_sender_imageview,img_receiver_imageview;
        RelativeLayout relative_layout,relativeLayout2;



        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtGroupMessage = itemView.findViewById(R.id.txtGroupMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            img_sender_imageview = itemView.findViewById(R.id.img_sender_imageview);
            relative_layout = itemView.findViewById(R.id.relative_layout);
            img_receiver_imageview = itemView.findViewById(R.id.img_receiver_imageview);
            relativeLayout2 = itemView.findViewById(R.id.relative_layout2);
            txtFileTime = itemView.findViewById(R.id.txtTFileTime);


    }
}
    private void deleteReceivedMessages(final int position,final HolderGroupChat holder){


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("GpMessages")
                .child(groupModelChatsArrayList.get(position).getMessage())
                .child(groupModelChatsArrayList.get(position).getSender())
                .child(groupModelChatsArrayList.get(position).getTimeStamp())
                .child(groupModelChatsArrayList.get(position).getType())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Deleted",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(holder.itemView.getContext(),"Error occurred",Toast.LENGTH_SHORT).show();

                }
            }


        });


        groupModelChatsArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,groupModelChatsArrayList.size());
    }

    private void deleteMessagesForEveryOne(final int position,final HolderGroupChat holder){


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("GpMessages")
                .child(groupModelChatsArrayList.get(position).getTimeStamp())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){




                }else {

                    Toast.makeText(holder.itemView.getContext(),"Error occurred",Toast.LENGTH_SHORT).show();

                }
            }


        });

        groupModelChatsArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,groupModelChatsArrayList.size());


    }

}

