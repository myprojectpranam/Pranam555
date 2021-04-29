package com.example.pranam555;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullImageActivity extends AppCompatActivity {

    private ImageView fullImageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);


        fullImageView = findViewById(R.id.full_Image);
        imageUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(fullImageView);
    }
}