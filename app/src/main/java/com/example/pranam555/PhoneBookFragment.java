package com.example.pranam555;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pranam555.ui.AdapterNewGroupChatList;
import com.example.pranam555.ui.NewGroupModelChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PhoneBookFragment extends Fragment {

    private RecyclerView contactsRecyclerView;
    private List<PhoneContactsModel> phoneContactsArrayList = new ArrayList<>();
    private List<PhoneContactsModel> appUsersList = new ArrayList<>();
    private PhoneContacsAdapter adapter ;
    private List<PhoneContactsModel> searchList;
  //  String PhoneName;
    PhoneContactsModel searchUser;
    PhoneContactsModel phoneContactsModelssss;

    private SearchView searchView;

    public PhoneBookFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_phone_book, container, false);

      //  getPermission();

        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView);

        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getContactList();
        adapter = new PhoneContacsAdapter(getContext(),appUsersList);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.search);
//        MenuItem item = menu.findItem(R.id.search);
//        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(item);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

         //       seachUserContact(phoneContactsModelssss,query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {


                if (!TextUtils.isEmpty(query)){
           //     seachUserContact(phoneContactsModelssss,query);

                }
                return true;
            }
        });



    }

    private void getContactList(){

        String IsoPrefix = getCountryIso();

        String [] projection = new String[]{
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };

        String selectionFields =  ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        String[] selectionArgs = new String[]{"com.google"};

        Cursor phones = null;

        try {
            phones = getActivity().getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,null,null,null);
        }catch (Exception e){

            Log.e("getContentResolver()",Log.getStackTraceString(e));
        }

        if (phones!=null){

            while (phones.moveToNext()){

                String PhoneName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            String phoneUri = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                phoneNumber = phoneNumber.replace(" ","");
                phoneNumber = phoneNumber.replace("-","");
                phoneNumber = phoneNumber.replace("(","");
                phoneNumber = phoneNumber.replace(")","");

            if (phoneNumber.length() > 0 && !String.valueOf(phoneNumber.charAt(0)).equals("+")){

                phoneNumber = IsoPrefix + phoneNumber;
            }



            phoneContactsModelssss = new PhoneContactsModel(PhoneName,phoneNumber,null,"");
            phoneContactsArrayList.add(phoneContactsModelssss);

            getUserDetails(phoneContactsModelssss,PhoneName);
        }
        phones.close();

        }
    }

    private void getUserDetails(PhoneContactsModel phoneContactsModelssss,String PName){

        appUsersList.clear();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");
        Query query = databaseReference.orderByChild("phone").equalTo(phoneContactsModelssss.getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String phone = "";
                    String name = "";
                    String image = "";

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){

                        if (childSnapshot.child("phone").getValue()!=null){

                            phone = childSnapshot.child("phone").getValue().toString();
                        }

                        if (childSnapshot.child("name").getValue()!=null){

                            name = childSnapshot.child("name").getValue().toString();
                        }

                        if (childSnapshot.child("image").getValue()!=null){

                            image = childSnapshot.child("image").getValue().toString();
                        }


                        PhoneContactsModel appUsers = new PhoneContactsModel(name,phone,image,childSnapshot.getKey());


                        //Setting the name according to saved name in contact list like whatsapp
                        if (!name.equals(PName)){

                            for (PhoneContactsModel contactIterator:phoneContactsArrayList){

                                if (contactIterator.getPhoneNumber().equals(appUsers.getPhoneNumber())){

                                    appUsers.setName(contactIterator.getName());
                                }
                            }
                        }
                        appUsersList.add(appUsers);
                        contactsRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getCountryIso(){

        String iso = "";
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(getActivity().TELEPHONY_SERVICE);

        if (telephonyManager.getNetworkCountryIso()!=null)

            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))

                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountyToPhonePrefix.getPhone(iso);

    }

//    private void seachUserContact(PhoneContactsModel phoneContactsModel,String qurey){
//
//        searchList = new ArrayList<>();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("my_users");
//
//        Query query1 = databaseReference.orderByChild("phone").equalTo(phoneContactsModel.getPhoneNumber());
//        query1.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (snapshot.exists()){
//
//                    String phone = "";
//                    String name = "";
//                    String image = "";
//
//
//                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
//
//                        name = dataSnapshot.child("name").getValue().toString();
//
//                        searchUser = new PhoneContactsModel(name,"","",dataSnapshot.getKey());
//
//
//                        if (!name.equals(PhoneName)){
//
//                            for (PhoneContactsModel contactIterator:phoneContactsArrayList){
//
//                                if (contactIterator.getPhoneNumber().equals(searchUser.getPhoneNumber())){
//
//                                    searchUser.setName(contactIterator.getName());
//
//                                    if (searchUser.getName().toLowerCase().contains(qurey.toLowerCase())){
//
//                                        searchList.add(searchUser);
//
//                                    }
//
//
//                                }
//                            }
//                        }
//
//                        contactsRecyclerView.setAdapter(adapter);
//                        adapter.notifyDataSetChanged();
//                        return;
//
//
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//
//    }

}