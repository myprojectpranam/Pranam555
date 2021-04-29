package com.example.pranam555.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pranam555.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NewGroupListFragment extends Fragment {

    private RecyclerView groupRecyclerView;
    private FirebaseAuth mAuth;
    private ArrayList<NewGroupModelChatList>groupModelChatLists;
    private AdapterNewGroupChatList adapterNewGroupChatList;
    private SearchView searchView;
//    NewGroupModelChatList newGroupModelChatList = new NewGroupModelChatList();



    public NewGroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.search).setVisible(true);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGroupChatList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchGroupChatList(newText);

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat_fragments, container, false);

        mAuth = FirebaseAuth.getInstance();

        loadGroupChatList();
        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        return view;
    }

    private void loadGroupChatList(){

        groupModelChatLists = new ArrayList<>();

        //Getting all children in the group node
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupModelChatLists.clear();
                groupModelChatLists.size();

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    //If the user has his id in the group, it will appear the group to the screen

                    if (mAuth.getCurrentUser()!=null && mAuth.getUid()!=null){
                    if (dataSnapshot1.child("Participants").child(mAuth.getUid()).exists()){
                        //Here the NewGroupModelChatList has getter and setter of items which are in the database
                        //Getting that items from database and adding to the groupModelChatLists


                        NewGroupModelChatList modelChatList = dataSnapshot1.getValue(NewGroupModelChatList.class);
                        //Adding items to arrayList
                        groupModelChatLists.add(modelChatList);

                    }


                    }
                }

                //Putting all items in the adapter
                adapterNewGroupChatList = new AdapterNewGroupChatList(getContext(),groupModelChatLists);
                //getting all items and set it to the groupRecyclerView
                groupRecyclerView.setAdapter(adapterNewGroupChatList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void searchGroupChatList(String query){

        groupModelChatLists = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupModelChatLists.size();

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    if (dataSnapshot1.child("Participants").child(mAuth.getUid()).exists()){

                        if (dataSnapshot1.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){


                            NewGroupModelChatList modelChatList = dataSnapshot1.getValue(NewGroupModelChatList.class);
                            groupModelChatLists.add(modelChatList);
                        }

                    }
                }

                adapterNewGroupChatList = new AdapterNewGroupChatList(getContext(),groupModelChatLists);
                groupRecyclerView.setAdapter(adapterNewGroupChatList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}