package com.example.pranam555;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhoneContacsAdapter extends RecyclerView.Adapter<PhoneContacsAdapter.PhoneContactViewHolder> implements Filterable {

    Context mContext;
    List<PhoneContactsModel> phoneContactsList;
    List <PhoneContactsModel> phoneContactsFilteredList;


    public PhoneContacsAdapter(Context mContext, List<PhoneContactsModel> phoneContactsList) {
        this.mContext = mContext;
        this.phoneContactsList = phoneContactsList;
        this.phoneContactsFilteredList = phoneContactsList;
    }

    @NonNull
    @Override
    public PhoneContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_contacts,parent,false);
        PhoneContactViewHolder phoneContactViewHolder = new PhoneContactViewHolder(view);
        return phoneContactViewHolder;


    }


    @Override
    public void onBindViewHolder(@NonNull PhoneContactViewHolder holder, int position) {

        PhoneContactsModel model = phoneContactsList.get(position);

        String name = model.getName();
        String phoneNumber = model.getPhoneNumber();
        String image = model.getPhoto();
        String uid = model.getUid();

        holder.nameContact.setText(name);
        holder.phoneNumber.setText(phoneNumber);


        try {

            Picasso.get().load(image).into(holder.img_contact);
        }catch (Exception e){

            holder.img_contact.setImageResource(R.drawable.profile_image);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent moveToChatActivity = new Intent(mContext,ChatActivity.class);
                moveToChatActivity.putExtra("uid",uid);
                moveToChatActivity.putExtra("name",name);
                moveToChatActivity.putExtra("photo",image);
                holder.itemView.getContext().startActivity(moveToChatActivity);




            }
        });

    }

    @Override
    public int getItemCount() {
        return phoneContactsFilteredList.size();
    }

    public static class PhoneContactViewHolder extends RecyclerView.ViewHolder {

        TextView nameContact,phoneNumber;
        CircleImageView img_contact;


        public PhoneContactViewHolder(@NonNull View itemView) {
            super(itemView);

            nameContact = itemView.findViewById(R.id.txtnameContact);
            phoneNumber = itemView.findViewById(R.id.txtPhoneContact);
            img_contact = itemView.findViewById(R.id.imgContact);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    phoneContactsFilteredList = phoneContactsList;
                } else {

                    List<PhoneContactsModel> filteredList = new ArrayList<>();
                    for (PhoneContactsModel row : phoneContactsList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhoneNumber().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    phoneContactsFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = phoneContactsFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                phoneContactsFilteredList = (ArrayList<PhoneContactsModel>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }
//    @Override
//    public Filter getFilter(){
//        return  new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence charSequence) {
//
//                String charString = charSequence.toString();
//                if (charString.isEmpty()){
//
//                    phoneContactsFilteredList = phoneContactsList;
//
//
//                }else {
//
//                    List<PhoneContactsModel> filteredList = new ArrayList<>();
//                    for (PhoneContactsModel row: phoneContactsList){
//
//                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhoneNumber().contains(charSequence)){
//
//                            filteredList.add(row);
//                        }
//                    }
//
//                    phoneContactsFilteredList = filteredList;
//                }
//
//                FilterResults filterResults = new FilterResults();
//                filterResults.values = phoneContactsFilteredList;
//                return filterResults;
//
//
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
//
//                phoneContactsFilteredList = (ArrayList<PhoneContactsModel>) filterResults.values;
//                notifyDataSetChanged();
//
//            }
//        };
//    }
}
