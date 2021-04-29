package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText setting_edtUsername,settings_edtProfileStatus;
    private Button btnUpdateSettings;
    private CircleImageView settings_imgProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private static final int GALLERY_PIC = 1000;
    //In this storage firebase we are putting image of user profile image
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
//    private String photoUrl = "";

    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setting_edtUsername = findViewById(R.id.settings_edtUsername);
        settings_edtProfileStatus = findViewById(R.id.settings_edtProfileStatus);
        btnUpdateSettings = findViewById(R.id.btnUpdateSettings);
        settings_imgProfileImage = findViewById(R.id.settings_imgProfileImage);
        settingsToolbar = findViewById(R.id.settings_Toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");


        //This will be the folder name of profile image of the user. basically adding the profile image folder/
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        settings_imgProfileImage.setOnClickListener(SettingsActivity.this);

        setting_edtUsername.setVisibility(View.INVISIBLE);

        btnUpdateSettings.setOnClickListener(SettingsActivity.this);

        loadingBar = new ProgressDialog(this);

       //Getting the current user(Deepak) UID
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //Getting access to the database to put deepak's information to store inside the database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        retrieveUserInformation();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btnUpdateSettings:
                updatedSettings();
                break;

            case R.id.settings_imgProfileImage:

                //This will send the user to the gallery of phone
                Intent galleryPic = new Intent();
                galleryPic.setAction(Intent.ACTION_GET_CONTENT);
                galleryPic.setType("image/*");
                startActivityForResult(galleryPic,GALLERY_PIC);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PIC && resultCode == RESULT_OK && data!=null){

            //This is the normal pic that selected, later we will crop it


            Uri imageUri = data.getData();
//            String downloadUrl = imageUri.toString();
//            photoUrl = downloadUrl;


            //Start cropping the image
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("Image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                //This resultUri Contain the crop URI (URL in the database)

                Uri resultUri = result.getUri();

                //It will store in profile image folder with uid of the current user, in storage database
                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //    Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();

                                filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {

                                        if (task.isSuccessful()){

                                            String downloadUrl = task.getResult().toString();

                                            databaseReference.child("my_users").child(currentUserID).child("image")
                                                    .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        Toast.makeText(SettingsActivity.this,"Image saved in realtime database",Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();

                                                    }else {

                                                        String errorMessage = task.getException().toString();

                                                        Toast.makeText(SettingsActivity.this,errorMessage,Toast.LENGTH_SHORT).show();

                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });

                                        }

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                        if (task.isSuccessful()){
//
//                            Uri downloadUrl = result.getUri();
//
//                            Toast.makeText(SettingsActivity.this,"Uploaded successfully",Toast.LENGTH_SHORT).show();
//                            //We will get the link of the profile image from the firebase storage database
//
//                            //Adding a name of image in the database and it will contain the image which is in storage database
//                            databaseReference.child("my_users").child(currentUserID).child("image").setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                    if (task.isSuccessful()){
//
//                                        Toast.makeText(SettingsActivity.this,"Image saved in realtime database",Toast.LENGTH_SHORT).show();
//                                        loadingBar.dismiss();
//
//                                    }else {
//
//                                        String errorMessage = task.getException().toString();
//                                        Toast.makeText(SettingsActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
//                                        loadingBar.dismiss();
//                                    }
//
//                                }
//                            });
//
//                        }else {
//
//                            String errorMessage = task.getException().toString();
//
//                            Toast.makeText(SettingsActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
//
//                            loadingBar.dismiss();
//
//                        }
//
//                    }
//                });
//
            }


        }

    }

    private void updatedSettings() {

        String setUserName = setting_edtUsername.getText().toString();
        String setProfileStatus = settings_edtProfileStatus.getText().toString();

        if (setUserName == null){

            Toast.makeText(SettingsActivity.this,"Please provide username",Toast.LENGTH_SHORT).show();
        }if (setProfileStatus == null){

            Toast.makeText(SettingsActivity.this,"Please provide Status",Toast.LENGTH_SHORT).show();

        }else {

            //Putting the user settings information to the database
            HashMap<String,Object> profileInformationMap = new HashMap<>();
            profileInformationMap.put("uid",currentUserID);
            profileInformationMap.put("name", setUserName);
            profileInformationMap.put("status",setProfileStatus);
//            if (TextUtils.isEmpty(photoUrl)){
//                profileInformationMap.put("image",photoUrl);
//            }

            databaseReference.child("my_users").child(currentUserID).updateChildren(profileInformationMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this,"Profile Updated",Toast.LENGTH_SHORT).show();

                            }else {

                                String errorMessage = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,errorMessage, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });


        }
    }

    private void sendUserToMainActivity(){

        Intent moveUserToTheMainActivity = new Intent(SettingsActivity.this,MainActivity.class);
        //This means back button will not applicable in the activity, it will not go back to the log in activity, has to press logout
        moveUserToTheMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(moveUserToTheMainActivity);
        finish();
    }

    //Retrieving the old information of the deepak user, it will display to the settings activity
    private void retrieveUserInformation(){

        databaseReference.child("my_users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if the user is old user and has below all 3 items in his UID, showing the owner user
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && ((dataSnapshot.hasChild("image"))))){

                    String retrievingUserName = dataSnapshot.child("name").getValue().toString();
                    String retrievingProfileStatus = dataSnapshot.child("status").getValue().toString();
                    //getting the image from the image key in the database to update profile pic
                    String retrievingProfileImage = dataSnapshot.child("image").getValue().toString();

                    setting_edtUsername.setText(retrievingUserName);
                    settings_edtProfileStatus.setText(retrievingProfileStatus);
                    Picasso.get().load(retrievingProfileImage).into(settings_imgProfileImage);


                    //If the user has only update the name but not his profile pic
                }else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")){

                    String retrievingUserName = dataSnapshot.child("name").getValue().toString();
                    String retrievingProfileStatus = dataSnapshot.child("status").getValue().toString();

                    setting_edtUsername.setText(retrievingUserName);
                    settings_edtProfileStatus.setText(retrievingProfileStatus);


                    //If none of above
                }else {

                    setting_edtUsername.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this,"Please update your profile information",Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
