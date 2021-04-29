
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pranam555.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private ActionBar actionBar;
    String currentUser;
    private FirebaseAuth mAuth;
    private CircleImageView groupIcon;
    private EditText edtGroupTitle,edtGroupDescription;
    private FloatingActionButton btnCreateGroup;
    private static final int CAMERA_REQUEST_CODE = 1000;
    private static final int STORAGE_REQUEST_CODE = 2000;

    private static  final int IMAGE_PICK_CAMERA_CODE = 3000;
    private static  final int IMAGE_PICK_GALLERY_CODE = 4000;

    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri image_uri = null;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);


        toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("Create Group");


        groupIcon = findViewById(R.id.groupIcon);
        edtGroupTitle = findViewById(R.id.edtGroupTitle);
        edtGroupDescription = findViewById(R.id.groupDescription);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};




        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser()!=null){

            currentUser = mAuth.getCurrentUser().getPhoneNumber();

            actionBar.setSubtitle(currentUser);
        }

        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImagePickDialog();

            }
        });
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startCreatingGroup();


            }
        });
    }

    private void startCreatingGroup(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Group");

        String groupTitle = edtGroupTitle.getText().toString().trim();
        String groupDescription = edtGroupDescription.getText().toString().trim();

        if (groupTitle.isEmpty()){

            Toast.makeText(this,"Please enter group title",Toast.LENGTH_SHORT).show();
        }
        progressDialog.show();


        String timestamp = ""+System.currentTimeMillis();
        if (image_uri==null){

            createGroup(""+timestamp,""+groupTitle,""+groupDescription,"");


        }else {

            String fileNameAndPath = "Group_Imgs/" + "image" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()){


                                if (uriTask.isSuccessful()){

                                    Uri downloadUrl = uriTask.getResult();


                                    createGroup(""+timestamp,""+groupTitle,""+groupDescription,""+downloadUrl);


                                }
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(NewGroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();


                }
            });


        }

    }

    private void createGroup(String timestamp,String groupTitle,String groupDescription,String groupIcon){

        HashMap<String,String> groupDetails = new HashMap<>();

        groupDetails.put("groupId",timestamp);
        groupDetails.put("groupTitle",groupTitle);
        groupDetails.put("groupDescription",groupDescription);
        groupDetails.put("groupIcon",groupIcon);
        groupDetails.put("timestamp",timestamp);
        groupDetails.put("createdBy",mAuth.getCurrentUser().getUid());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        databaseReference.child(timestamp).setValue(groupDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                HashMap<String,String> adminDetails = new HashMap<>();
                adminDetails.put("uid",mAuth.getCurrentUser().getUid());
                adminDetails.put("role","creator");
                adminDetails.put("timestamp",timestamp);

                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Groups");
                databaseReference1.child(timestamp).child("Participants").child(mAuth.getCurrentUser().getUid())
                        .setValue(adminDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressDialog.dismiss();

                        Toast.makeText(NewGroupActivity.this,"Group created",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();

                        Toast.makeText(NewGroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();


                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(NewGroupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });



    }




    @Override
    protected void onStart() {

        super.onStart();
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

        if (requestCode==IMAGE_PICK_GALLERY_CODE){

            image_uri = data.getData();
            groupIcon.setImageURI(image_uri);

        }else if (requestCode == IMAGE_PICK_CAMERA_CODE){

            groupIcon.setImageURI(image_uri);
        }
    }
}