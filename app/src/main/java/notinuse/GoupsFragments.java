package notinuse;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pranam555.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoupsFragments extends Fragment {

    private View groupFragmentsView;
    //Here we put the group data
    private ArrayList<String> groupList = new ArrayList<>();
    //Here we put that data in the adapter
    private ArrayAdapter<String> arrayAdapter;
    //Here we set the adapter with the listview
    private ListView listView;
    FirebaseAuth mAuth;
    String currentUser;

    private DatabaseReference groupDatabaseReference;


    public GoupsFragments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentsView = inflater.inflate(R.layout.fragment_goups_fragments, container, false);

        listView = groupFragmentsView.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,groupList);
        listView.setAdapter(arrayAdapter);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){


        currentUser = mAuth.getCurrentUser().getUid();

        groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("my_users").child(currentUser).child("Groups");


        retrieveAndDisplayGroupsFromTheDatabase();

        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String currentGroupName =  parent.getItemAtPosition(position).toString();
        Intent moveToGroupChatActivity = new Intent(getContext(), GroupChatActivity.class);
        moveToGroupChatActivity.putExtra("groupName",currentGroupName);
        startActivity(moveToGroupChatActivity);

        Toast.makeText(getContext(),currentGroupName,Toast.LENGTH_SHORT).show();


            }
        });

        return groupFragmentsView;
    }

    private void retrieveAndDisplayGroupsFromTheDatabase(){

        groupDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //It will avoid the duplicacy when new group added and avoid repeating the previous group name
                Set<String> set = new HashSet<>();
//                for (DataSnapshot data : dataSnapshot.getChildren()){
//
//                    set.add(data.getKey());
//                }
//                 groupList.clear();
//                groupList.addAll(set);
//                arrayAdapter.notifyDataSetChanged();

                Iterator iterator = dataSnapshot.getChildren().iterator();


                while (iterator.hasNext()){

                    set.add(((DataSnapshot) iterator.next()).getKey());

                }
                groupList.clear();
                groupList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
