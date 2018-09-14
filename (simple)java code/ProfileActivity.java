package com.ketanchoyal.crossfire;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView mprofiledisplayname;
    private TextView mprofilelstatustext;
    private TextView mfriendscount;

    private ImageView mprofileimage;
    private ProgressDialog mProgressDialog;

    private Button mrequestsendbtn;
    private Button mrequestdeclinebtn;

    private String mCurrent_state;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendrequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String Friend_id=getIntent().getStringExtra("user_id");

        mCurrent_state="not_friends";

        mprofiledisplayname=findViewById(R.id.profiledisplayname);
        mprofilelstatustext=findViewById(R.id.profilestatustext);
        mfriendscount=findViewById(R.id.profilefriendscount);
        mprofileimage=findViewById(R.id.user_profile_image);
        mrequestsendbtn=findViewById(R.id.request_send_btn);
        mrequestdeclinebtn=findViewById(R.id.request_decline_btn);

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(Friend_id);
        mFriendrequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user=FirebaseAuth.getInstance().getCurrentUser();

        mProgressDialog.show();
        mrequestdeclinebtn.setVisibility(View.GONE);
        mrequestdeclinebtn.setEnabled(false);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                mprofiledisplayname.setText(dataSnapshot.child("name").getValue().toString());
                mprofilelstatustext.setText(dataSnapshot.child("status").getValue().toString());

                Picasso.with(ProfileActivity.this).load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.default_profile).into(mprofileimage);

                //Friend list/request list
                mFriendrequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(Friend_id))
                        {
                            String req_type=dataSnapshot.child(Friend_id).child("request_type").getValue().toString();

                            if(req_type.equals("received"))
                            {
                                mCurrent_state="req_received";
                                mrequestsendbtn.setText("Accept Friend Request");
                                mrequestdeclinebtn.setVisibility(View.VISIBLE);
                                mrequestdeclinebtn.setEnabled(true);
                            }
                            if(req_type.equals("sent"))
                            {
                                mCurrent_state="req_sent";
                                mrequestsendbtn.setText("Cancel Friend Request");
                            }
                            if(req_type.equals("declined"))
                            {
                                //TODO:1 Display alert message if request is declined last time
                                Toast.makeText(ProfileActivity.this,"This user Declined your Request Last time",Toast.LENGTH_LONG).show();
                                mCurrent_state="not_friends";
                                mrequestsendbtn.setText("Send Friend Request");
                            }
                            mProgressDialog.dismiss();
                        }
                        else
                        {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(Friend_id))
                                    {
                                        mCurrent_state="friends";
                                        mrequestsendbtn.setText("UnFriend");
                                    }
                                    else
                                    {
                                        mCurrent_state="not_friends";
                                        mrequestsendbtn.setText("Send Friend Request");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mProgressDialog.dismiss();

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mrequestsendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mrequestsendbtn.setEnabled(false);
                //not friend state
                if(mCurrent_state.equals("not_friends"))
                {
                    mFriendrequestDatabase.child(mCurrent_user.getUid()).child(Friend_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                mFriendrequestDatabase.child(Friend_id).child(mCurrent_user.getUid()).child("request_type").setValue("received")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {

                                            HashMap<String, String> notificationdata= new HashMap<>();
                                            notificationdata.put("from",mCurrent_user.getUid());
                                            notificationdata.put("type","request");

                                            mNotificationDatabase.child(Friend_id).push().setValue(notificationdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful())
                                                    {
                                                        mCurrent_state="req_sent";
                                                        mrequestsendbtn.setText("Cancel Friend Request");
                                                    }
                                                    else
                                                    {
                                                        mrequestsendbtn.setEnabled(true);
                                                        Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            });
                                            //Toast.makeText(ProfileActivity.this,"Request Sent.",Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            mrequestsendbtn.setEnabled(true);
                                            Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Failed sending request..",Toast.LENGTH_LONG).show();
                            }
                            mrequestsendbtn.setEnabled(true);
                        }
                    });
                    //cancel sent request  state
                }
                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendrequestDatabase.child(mCurrent_user.getUid()).child(Friend_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendrequestDatabase.child(Friend_id).child(mCurrent_user.getUid()).child("request_type").removeValue();
                                mrequestsendbtn.setEnabled(true);
                                mCurrent_state="not_friends";
                                mrequestsendbtn.setText("Send Friend Request");
                            }
                            else
                            {
                                mrequestsendbtn.setEnabled(true);
                                Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //request received state
                if(mCurrent_state.equals("req_received"))
                {
                    final String CurrentDate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(Friend_id).setValue(CurrentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                mFriendDatabase.child(Friend_id).child(mCurrent_user.getUid()).setValue(CurrentDate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            mFriendrequestDatabase.child(mCurrent_user.getUid()).child(Friend_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        mFriendrequestDatabase.child(Friend_id).child(mCurrent_user.getUid()).child("request_type").removeValue();
                                                        mrequestsendbtn.setEnabled(true);
                                                        mCurrent_state="friends";
                                                        mrequestsendbtn.setText("UnFriend");
                                                        mrequestdeclinebtn.setVisibility(View.GONE);
                                                    }
                                                    else
                                                    {
                                                        mrequestsendbtn.setEnabled(true);
                                                        Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }

                                    }
                                });
                            }
                            else
                            {
                                mrequestsendbtn.setEnabled(true);
                                Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }
                if(mCurrent_state=="friends")
                {
                    mFriendDatabase.child(mCurrent_user.getUid()).child(Friend_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendDatabase.child(Friend_id).child(mCurrent_user.getUid()).removeValue();
                                mrequestsendbtn.setEnabled(true);
                                mCurrent_state="not_friends";
                                mrequestsendbtn.setText("Send Friend Request");
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                mrequestsendbtn.setEnabled(true);
                            }
                        }
                    });
                }

            }
        });

        //Decline Feature
        mrequestdeclinebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendrequestDatabase.child(mCurrent_user.getUid()).child(Friend_id).child("request_type").setValue("declined")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    mFriendrequestDatabase.child(Friend_id).child(mCurrent_user.getUid()).child("request_type").setValue("declined")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful())
                                                    {
                                                        mrequestdeclinebtn.setVisibility(View.GONE);
                                                        mrequestdeclinebtn.setEnabled(false);
                                                        mCurrent_state="not_friends";
                                                        mrequestsendbtn.setText("Send Friend Request");
                                                        //Toast.makeText(ProfileActivity.this,"Request Sent.",Toast.LENGTH_LONG).show();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                                else
                                {

                                }
                            }
                        });

            }
        });

    }
}

/* dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:27.0.2'
    implementation 'com.android.support:design:27.0.2'
    implementation 'com.android.support.constraint:constraint-layout:1.0.2'
    implementation 'com.google.firebase:firebase-auth:11.8.0'
    implementation 'com.google.firebase:firebase-database:11.8.0'
    implementation 'com.google.firebase:firebase-storage:11.8.0'
    implementation 'com.google.firebase:firebase-messaging:11.8.0'
    testImplementation 'junit:junit:4.12'
    compile 'de.hdodenhof:circleimageview:2.2.0'
    androidTestImplementation 'com.android.support.test:runner:1.0.1'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
    compile 'com.theartofdev.edmodo:android-image-cropper:2.6.+'
    implementation 'com.firebaseui:firebase-ui-database:3.2.1'
    compile 'id.zelory:compressor:2.1.0'
    implementation 'com.squareup.picasso:picasso:2.5.2'
    compile 'com.squareup.okhttp:okhttp:2.5.0'
    compile 'com.daasuu:BubbleLayout:1.2.0'

} */
