package notinuse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pranam555.R;
import com.example.pranam555.UserProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView find_friends_recyclerviewList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        find_friends_recyclerviewList = findViewById(R.id.find_friends_recyclerviewList);
        find_friends_recyclerviewList.setLayoutManager(new LinearLayoutManager(FindFriends.this));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("my_users");

        toolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(toolbar);
        //Back button on the toolbar to go back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //By this we are asking the database to give the data of all users info to show it on find friends activity
        FirebaseRecyclerOptions<Contactsusers> options = new FirebaseRecyclerOptions.Builder<Contactsusers>()
                .setQuery(databaseReference,Contactsusers.class)
                .build();

        FirebaseRecyclerAdapter<Contactsusers,findFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contactsusers, findFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findFriendsViewHolder holder, int position, @NonNull Contactsusers model) {

                holder.txtUserProfileName.setText(model.getName());
                holder.txtUserStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.users_profile_image_find_friends);

                //This itemview has name,status and profile pic, we are making them a listener
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id = getRef(position).getKey();

                        Intent moveUserToUserProfileActivity = new Intent(FindFriends.this, UserProfileActivity.class);
                        moveUserToUserProfileActivity.putExtra("visit_user_id",visit_user_id);
                        startActivity(moveUserToUserProfileActivity);

                    }
                });

            }

            //Here we have inflated out layout to the view holder
            @NonNull
            @Override
            public findFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_display_layout,parent,false);

                findFriendsViewHolder viewHolder = new findFriendsViewHolder(view);

                return viewHolder;
            }
        };

        find_friends_recyclerviewList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class findFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView txtUserProfileName,txtUserStatus;
        CircleImageView users_profile_image_find_friends;


        public findFriendsViewHolder(View itemView){

            super(itemView);

            txtUserProfileName = itemView.findViewById(R.id.txt_user_profile_name);
            txtUserStatus =itemView.findViewById(R.id.txt_user_status);
            users_profile_image_find_friends = itemView.findViewById(R.id.users_profile_image_find_friends);
        }
    }
}
