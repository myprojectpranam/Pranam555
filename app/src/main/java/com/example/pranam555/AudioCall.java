package com.example.pranam555;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AudioCall extends AppCompatActivity {

    private SinchClient sinchClient;
    private Call call;
    private ImageView hangUpCall;
    private TextView txtCalling;
    private DatabaseReference databaseReference;
    private String checker="";

    private String callReceiverId;
    private String callMakerId;
    private int INITIAL_REQUEST = 12345;
    private static final String APP_KEY = "2c2ef9c5-803b-4929-8842-d8d042b03747";
    private static final String APP_SECRET = "9l5woHvey0+9IOj4oN7tvQ==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";
    private FirebaseAuth mAuth;
    private String currentUser;
    private TextView name_Contact;
    private ImageView profile_image_calling;
    private String receiverUserName,receiverUserImage;
    private String senderUserName,senderUserImage;
    private Timer timer;
    private ImageView makeCalls;
    private long mCallStart = 0;
    private TextView mCallingDuration;
    private UpdateCallDurationTask mDurationTask;
    private FirebaseUser fUser;
    private String callingID,ringingID;



    private class UpdateCallDurationTask extends TimerTask{


        @Override
        public void run() {

            AudioCall.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    updateCallDuration();
                }
            });

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!canAccessLocation()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }


        hangUpCall = findViewById(R.id.hangUpCall);
        makeCalls = findViewById(R.id.makeCalls);
        callReceiverId = getIntent().getExtras().getString("visit_user_id_forAudioCall");
       // callMakerId = getIntent().getExtras().getString("visit_user_id_whoMakeCall");
        txtCalling = findViewById(R.id.txtCalling);
        databaseReference = FirebaseDatabase.getInstance().getReference("my_users");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        name_Contact = findViewById(R.id.name_Contact);
        profile_image_calling = findViewById(R.id.profile_image_calling);
        mCallingDuration = findViewById(R.id.mCallDuration);
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        getAndSetUserProfileInfo();



        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(currentUser)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();

        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        makeCalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call == null) {
                    call = sinchClient.getCallClient().callUser(callReceiverId);
                    call.addCallListener(new SinchCallListener());

                }

//                else {
//                    call.hangup();
//
//                }
            }
        });

        hangUpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelCall();
                endCall();
                checker = "clicked";

            }
        });

        mCallStart = System.currentTimeMillis();


    }
    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            txtCalling.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mDurationTask.cancel();
            timer.cancel();
            endCall();

        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            txtCalling.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            timer = new Timer();
            mDurationTask = new UpdateCallDurationTask();
            timer.schedule(mDurationTask,0,500);
            mCallStart = System.currentTimeMillis();

        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            txtCalling.setText("ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }


    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            Toast.makeText(AudioCall.this, "incoming call", Toast.LENGTH_SHORT).show();
            call.answer();
            call.addCallListener(new SinchCallListener());

        }
    }

    private static final String[] INITIAL_PERMS={
            Manifest.permission.RECORD_AUDIO};

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.RECORD_AUDIO));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.child(callReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!checker.equals("clicked") && !dataSnapshot.hasChild("AudioCalling") && !dataSnapshot.hasChild("AudioCallRinging")){

                    final HashMap<String,Object> audioCallInfo = new HashMap<>();
                    audioCallInfo.put("calling" ,callReceiverId);

                    databaseReference.child(currentUser).child("AudioCalling").updateChildren(audioCallInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        final HashMap<String,Object> audioRingingInfo = new HashMap<>();
                                        audioRingingInfo.put("ringing",currentUser);
                                        databaseReference.child(callReceiverId).child("AudioCallRinging").updateChildren(audioRingingInfo);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getAndSetUserProfileInfo() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(callReceiverId).exists()){

                    receiverUserName = dataSnapshot.child(callReceiverId).child("name").getValue().toString();
                    name_Contact.setText(receiverUserName);

                    try {

                        receiverUserImage = dataSnapshot.child(callReceiverId).child("image").getValue().toString();

                        Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profile_image_calling);

                    }catch (Exception e){

                    }

                }if (dataSnapshot.child(currentUser).exists()){

                    senderUserName = dataSnapshot.child(currentUser).child("name").getValue().toString();

                    try {

                        senderUserImage = dataSnapshot.child(currentUser).child("image").getValue().toString();

                    }catch (Exception e){


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private String formatTimespan(long timespan){

        long totalSeconds = timespan/1000;
        long minutes = totalSeconds/60;
        long seconds = totalSeconds & 60;
        return String.format(Locale.US, "%02d:%02d",minutes,seconds);

    }

    private void updateCallDuration(){

        if (mCallStart>0){

            mCallingDuration.setText(formatTimespan(System.currentTimeMillis() - mCallStart));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//
//        timer = new Timer();
//        mDurationTask = new UpdateCallDurationTask();
//        timer.schedule(mDurationTask,0,500);

    }

    @Override
    protected void onPause() {
        super.onPause();
    //    mDurationTask.cancel();
//        timer.cancel();
    }



    private void cancelCall(){

        databaseReference.child(currentUser).child("AudioCalling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")){

                            callingID = dataSnapshot.child("calling").getValue().toString();

                            databaseReference.child(callingID).child("AudioCallRinging")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        databaseReference.child(currentUser).child("AudioCalling")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                Intent intent = new Intent(AudioCall.this,ChatActivity.class);
                                                intent.putExtra("uid",callingID);
                                                intent.putExtra("name",receiverUserName);
                                                intent.putExtra("photo",receiverUserImage);
                                                startActivity(intent);
                                            }
                                        });

                                    }
                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//        databaseReference.child(currentUser).child("AudioCallRinging")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")){
//
//                            ringingID = dataSnapshot.child("ringing").getValue().toString();
//                            databaseReference.child(ringingID).child("AudioCalling")
//                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                    if (task.isSuccessful()){
//
//                                        databaseReference.child(currentUser)
//                                                .child("AudioCallRinging")
//                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                startActivity(new Intent(AudioCall.this,MainActivity.class));
//                                                finish();
//
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
    }

    private void endCall(){

        if (call != null) {
            call.hangup();

        }
        finish();
    }

}

