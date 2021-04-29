package com.example.pranam555.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pranam555.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupEditActivity extends AppCompatActivity {

    String groupId;

    private static final int CAMERA_REQUEST_CODE = 1000;
    private static final int STORAGE_REQUEST_CODE = 2000;

    private static  final int IMAGE_PICK_CAMERA_CODE = 3000;
    private static  final int IMAGE_PICK_GALLERY_CODE = 4000;

    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri image_uri = null;

    private FirebaseAuth mAuth;
    private CircleImageView imgGroupIcon;
    private EditText edtGroupTitle,edtGroupDescription;
    private FloatingActionButton btnUpdateGroup;
    private ProgressDialog progressDialog;
    private String currentUser;
    private Toolbar toolbar;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        groupId = getIntent().getExtras().getString("groupId");

        imgGroupIcon = findViewById(R.id.groupIcon);
        edtGroupTitle = findViewById(R.id.edtGroupTitle);
        edtGroupDescription = findViewById(R.id.groupDescription);
        btnUpdateGroup = findViewById(R.id.btnUpdateGroup);

        toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("Create Group");

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};




        mAuth = FirebaseAuth.getInstance();

        loadGroupInfo();


        if (mAuth.getCurrentUser()!=null){

            currentUser = mAuth.getCurrentUser().getPhoneNumber();

            actionBar.setSubtitle(currentUser);
        }

        imgGroupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImagePickDialog();

            }
        });
        btnUpdateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startUpdatingGroup();


            }
        });


    }

    private void startUpdatingGroup(){

        String groupTitle = edtGroupTitle.getText().toString().trim();
        String groupDescription = edtGroupDescription.getText().toString().trim();

        if (groupTitle.isEmpty()){

            Toast.makeText(GroupEditActivity.this,"Group title is required",Toast.LENGTH_SHORT).show();
            return;
        }

        //update group without icon
        progressDialog.setMessage("Updating Group Info...");
        progressDialog.show();

        if (image_uri==null){


            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle",groupTitle);
            hashMap.put("groupDescription",groupDescription);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
            databaseReference.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //Updated...
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this,"Group info updated",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });

        }
        else {

            String timestamp = ""+System.currentTimeMillis();
            String filePathAndName = "Group_Imgs/"+"image"+"_"+timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();

                            while (!p_uriTask.isSuccessful());

                            Uri p_downloadUrl = p_uriTask.getResult();

                            if (p_uriTask.isSuccessful()){


                                HashMap<String,Object> hashMap = new HashMap<>();

                                hashMap.put("groupTitle",groupTitle);
                                hashMap.put("groupDescription",groupDescription);
                                hashMap.put("groupIcon",p_downloadUrl.toString());

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                                databaseReference.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                //Updated...
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this,"Group info updated",Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressDialog.dismiss();
                                        Toast.makeText(GroupEditActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                                    }
                                });



                            }
                            }


                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGroupInfo(){


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                            String groupId = dataSnapshot1.child("groupId").getValue().toString();
                            String groupTitle = dataSnapshot1.child("groupTitle").getValue().toString();
                            String groupDescription = dataSnapshot1.child("groupDescription").getValue().toString();
                            String groupIcon = dataSnapshot1.child("groupIcon").getValue().toString();
                            String createdBy = dataSnapshot1.child("createdBy").getValue().toString();
                            String timestamp = dataSnapshot1.child("timestamp").getValue().toString();

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            edtGroupTitle.setText(groupTitle);
                            edtGroupDescription.setText(groupDescription);

                            try {

                                Picasso.get().load(groupIcon).placeholder(R.drawable.profile_image).into(imgGroupIcon);

                            }catch (Exception e){


                            }

                        }

                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void pickFromGalley(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(this
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return result && result1;

    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void showImagePickDialog(){

        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {

                        if (position==0){

                            if (!checkCameraPermission()){

                                requestCameraPermission();

                            }else {



                                pickFromCamera();


                            }


                        }else {

                            if (!checkStoragePermission()){

                                requestStoragePermission();
                            }else {

                                pickFromGalley();


                            }


                        }
                    }
                });
        builder.show();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case CAMERA_REQUEST_CODE:{

                if (grantResults.length>0){

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){


                        pickFromCamera();
                    }else {

                        requestCameraPermission();
                        Toast.makeText(this,"Camera && storage permission are required",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{

                if (grantResults.length>0){

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){

                        pickFromGalley();
                    }else {

                        Toast.makeText(this,"Storage permission are required",Toast.LENGTH_SHORT).show();



                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK){

            if (data!=null){
            image_uri = data.getData();
            imgGroupIcon.setImageURI(image_uri);
            }

        }else if (requestCode == IMAGE_PICK_CAMERA_CODE && resultCode == RESULT_OK){

            imgGroupIcon.setImageURI(image_uri);


        }
    }
}