package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.Session;
import javax.microedition.khronos.opengles.GL;

import notifications.APIService;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;



public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener  {

    private static final String  API_KEY="47142264";
    private static final String SESSION_ID = "1_MX40NzE0MjI2NH5-MTYxNDU5MDYyNzM1MX5DbUJRSUhNTytsdm9mR3VBL21JSlNHN2h-fg";
    private static final String TOKEN = "T1==cGFydG5lcl9pZD00NzE0MjI2NCZzaWc9OWEzNTFiMGM3ZTU4MTQxYmZiZGNjMTI5Y2ZlMDkzNzZjYTFkMTk4NDpzZXNzaW9uX2lkPTFfTVg0ME56RTBNakkyTkg1LU1UWXhORFU1TURZeU56TTFNWDVEYlVKUlNVaE5UeXRzZG05bVIzVkJMMjFKU2xOSE4yaC1mZyZjcmVhdGVfdGltZT0xNjE0NTkwNjM1Jm5vbmNlPTAuNzQ3NzQ0MjAzMzk2NjIxNSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjE3MTc5MDM1JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    private ImageView closeVideoChatBtn;
    private com.opentok.android.Session mSession;
    private DatabaseReference databaseReference;
    private String userID = "";
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null){

            userID = mAuth.getCurrentUser().getUid();

        }
        databaseReference = FirebaseDatabase.getInstance().getReference("my_users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);


        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(userID).hasChild("Ringing")){

                            databaseReference.child(userID).child("Ringing").removeValue();

                            if (mPublisher!=null){

                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){

                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }

                        if (dataSnapshot.child(userID).hasChild("Calling")){

                            databaseReference.child(userID).child("Calling").removeValue();

                            if (mPublisher!=null){

                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){

                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this,ChatActivity.class));
                            finish();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){

        String[] perm = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this,perm)){

            mPublisherViewController = findViewById(R.id.publisherContainer);
            mSubscriberViewController = findViewById(R.id.subscriberContainer);

            //Initialize and connect to the session
            mSession = new Session.Builder(this,API_KEY,SESSION_ID).build();

            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else {

            EasyPermissions.requestPermissions(this,"Permission required",RC_VIDEO_APP_PERM,perm);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {


    }

    //Publish stream to the session
    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG,"Session connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        //this will show my image in small screen in my mobile
        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){

            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

        Log.i(LOG_TAG,"Stream Disconnected");


    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        //Subscribing to the streams

        Log.i(LOG_TAG,"Stream Received");
        if (mSubscriber == null){

            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Dropped");

        if (mSubscriber!=null){

            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }


    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.i(LOG_TAG,"Stream Error");


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}