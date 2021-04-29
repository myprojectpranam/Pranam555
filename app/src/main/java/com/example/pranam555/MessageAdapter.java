package com.example.pranam555;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,databaseReference1;
    String messageSenderID,messageReceiverID;
    private Context context;


    public MessageAdapter (List<Messages> userMessagesList,String messageReceiverID,Context context){

        this.userMessagesList = userMessagesList;
        this.messageReceiverID = messageReceiverID;
        this.context = context;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessageText,receiverMessageText,txtTime,txtSeen;
        CircleImageView receiverProfileImage;
        ImageView msgSenderPicture,msgReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.img_message_profile_image);
            msgSenderPicture = itemView.findViewById(R.id.img_sender_imageview);
            msgReceiverPicture = itemView.findViewById(R.id.img_receiver_imageview);
            txtSeen = itemView.findViewById(R.id.txtSeen);


        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();

        MessageViewHolder messageViewHolder = new MessageViewHolder(view);

        return messageViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        //Here deepak is sender, we need to get the current user id first

        if (mAuth.getCurrentUser()!=null){

            messageSenderID = mAuth.getCurrentUser().getUid();

        }
        //Position defines the position of messages according to database
        Messages messages = userMessagesList.get(position);
        //Here from is also deepak who is sender here
        String fromUserID = messages.getFrom();
        //getType means message is text or something else
        String fromMessageType = messages.getType();



        //Accessing the data by my_users nod of sender user so that we can get the data and show that to the other user
        //If deepak is sender, vishal can see my pic with msg and if vishal is sender deepak can see my pic with msg
        databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users").child(fromUserID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){

                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






        //Basically it is invisible the textview of receiver because deepak is sending a message,he is a sender
        //When someone send deepak a message we will make them visible and show that tot the textview below
        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.msgSenderPicture.setVisibility(View.GONE);
        holder.msgReceiverPicture.setVisibility(View.GONE);




        //If the msg type is text, text refers to the remark in the database
        if (fromMessageType.equals("text")){

            //If this is sender
            if (fromUserID.equals(messageSenderID)){

                //Deepak is sending message, it will set to senderMessageText in green color
                //This for green background for the sender like whatsapp, putiing sender msg in green.
                //getMessage refers to message coloumn in the database
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messager_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

                //When deepak receive a message, sender textview green  will be invisible because we
                // will show the received message in white color to deepak with the profile image of received message.
            }else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            }

        }else if (fromMessageType.equals("image")){

            if (fromUserID.equals(messageSenderID)){

                holder.msgSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.msgSenderPicture);

            }else {

                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.msgReceiverPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(holder.msgReceiverPicture);



            }
        }else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")){

            if (fromUserID.equals(messageSenderID)){

                holder.msgSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/pranam555-5dd6d.appspot.com/o/Group%20Image%20File%2Ffile.png?alt=media&token=e9f831f2-e4d4-4531-9b13-ef414e08524e")
                        .into(holder.msgSenderPicture);

            }else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.msgReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/pranam555-5dd6d.appspot.com/o/Group%20Image%20File%2Ffile.png?alt=media&token=e9f831f2-e4d4-4531-9b13-ef414e08524e")
                        .into(holder.msgReceiverPicture);
            }
        }




        if (fromUserID.equals(messageSenderID)){


            databaseReference1 = FirebaseDatabase.getInstance().getReference("Messages").child(messageReceiverID).child(messageSenderID);

            databaseReference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                        Messages messages1 = dataSnapshot.getValue(Messages.class);

                        //Checking the last message
                        if (position==userMessagesList.size()-1){

                            if (messages1.isIsseen()){

                                holder.txtSeen.setVisibility(View.VISIBLE);
                                holder.txtSeen.setText("Seen");

                            }else {

                                holder.txtSeen.setVisibility(View.VISIBLE);
                                holder.txtSeen.setText("Delivered");
                            }

                        }else {

                            holder.txtSeen.setVisibility(View.GONE);
                        }

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }



        if (fromUserID.equals(messageSenderID)){

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Position here is the type of message the user is pressing for delete or any other action
                    //Position knows the type of message because userMessagesList is an array
                    // and it has all the data in sequence according to the database, adapter put the data in userMessagesList
                    if (userMessagesList.get(position).getType().equals("pdf")
                     || userMessagesList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
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
                                    deleteSentMessages(position,holder);

                                }else if (positions==1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);


                                } if (positions==3){


                                    deleteMessagesForEveryOne(position,holder);

                                }
                            }
                        });

                        builder.show();
                    }

                 else  if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence[] options = new CharSequence[]{


                                "Delete for me",
                                "Cancel",
                                "Delete for everyone",
//                                "Forward",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions==0){

                                    deleteSentMessages(position,holder);


                                }else if (positions==2){

                                    deleteMessagesForEveryOne(position,holder);

                                }

//                                else if (positions==3){
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("forwardImage",userMessagesList.get(position).getMessage());
//                                ChatsFragment chatsFragment = new ChatsFragment();
//                                chatsFragment.setArguments(bundle);
//
//
//                                Toast.makeText(context,userMessagesList.get(position).getMessage(),Toast.LENGTH_LONG).show();
//
//
//                                }
                            }
                        });

                        builder.show();
                    }
                   else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View this Image",
                                "Cancel",
                                "Delete for everyone",
                                "Forward",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    deleteSentMessages(position,holder);

                                }else if (positions==1){

                                    Intent intent = new Intent(holder.itemView.getContext(),FullImageActivity.class);
                                    //Because the url is saved in the message nod in database
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);



                                }else if (positions==3){

                                    deleteMessagesForEveryOne(position,holder);

                                }else if (positions==4){

//                                    Intent intent = new Intent(holder.itemView.getContext(),ChatsFragment.class);
//                                    intent.putExtra("forwardImage",userMessagesList.get(position).getMessage());
//                                    holder.itemView.getContext().startActivity(intent);


                                }
                            }
                        });

                        builder.show();
                    }
                }
            });
        }
        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf")
                            || userMessagesList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Download and view this document",
                                "Cancel",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    deleteReceivedMessages(position,holder);

                                }else if (positions==1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });

                        builder.show();
                    }

                    else  if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Cancel",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    deleteReceivedMessages(position,holder);

                                }
                            }
                        });

                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View this Image",
                                "Cancel",
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int positions) {

                                if (positions == 0){

                                    deleteReceivedMessages(position,holder);


                                }else if (positions==1){

                                    Intent intent = new Intent(holder.itemView.getContext(),FullImageActivity.class);
                                    //Because the url is saved in the message nod in database
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);



                                }
                            }
                        });

                        builder.show();
                    }
                }
            });

        }




    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessages(final int position,final MessageViewHolder holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if (task.isSuccessful()){

                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,userMessagesList.size());

                    Toast.makeText(holder.itemView.getContext(),"Deleted",Toast.LENGTH_SHORT).show();

                }else {

                    Toast.makeText(holder.itemView.getContext(),"Error occurred",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    private void deleteReceivedMessages(final int position,final MessageViewHolder holder){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){


                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,userMessagesList.size());

                    Toast.makeText(holder.itemView.getContext(),"Deleted",Toast.LENGTH_SHORT).show();

                }else {

                    Toast.makeText(holder.itemView.getContext(),"Error occurred",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteMessagesForEveryOne(final int position,final MessageViewHolder holder){

       final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    databaseReference.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){


                                userMessagesList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position,userMessagesList.size());
                                Toast.makeText(holder.itemView.getContext(),"Deleted",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else {

                    Toast.makeText(holder.itemView.getContext(),"Error occurred",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }



}
