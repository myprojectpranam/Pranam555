package com.example.pranam555;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class fullprofileImageView extends AppCompatActivity {

    private ImageView fullProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullprofile_image_view);


        fullProfileImageView = findViewById(R.id.fullProfileImageView);

        String image = getIntent().getExtras().getString("fullImage");

        Picasso.get().load(image).into(fullProfileImageView);
    }
}