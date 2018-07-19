package com.ketanchoyal.crossfire;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private DatabaseReference mRootRef;

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

        mRootRef=FirebaseDatabase.getInstance().getReference();
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
                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(Friend_id).push();
                    String mNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationdata= new HashMap<>();
                    notificationdata.put("from",mCurrent_user.getUid());
                    notificationdata.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req" + "/" + mCurrent_user.getUid() + "/" + Friend_id + "/" + "request_type","sent");
                    requestMap.put("Friend_req" + "/" + Friend_id + "/" + mCurrent_user.getUid() + "/" + "request_type","received");
                    requestMap.put("notifications" + "/" + Friend_id + "/" + mNotificationId,notificationdata);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            mrequestsendbtn.setEnabled(true);

                            if(databaseError == null)
                            {
                                mCurrent_state="req_sent";
                                mrequestsendbtn.setText("Cancel Friend Request");
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Failed sending request..",Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }
                //cancel sent request  state
                if(mCurrent_state.equals("req_sent"))
                {
                    mrequestsendbtn.setEnabled(true);
                    mFriendrequestDatabase.child(mCurrent_user.getUid()).child(Friend_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendrequestDatabase.child(Friend_id).child(mCurrent_user.getUid()).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            mCurrent_state="not_friends";
                                            mrequestsendbtn.setText("Send Friend Request");
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
                                Toast.makeText(ProfileActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //request received state
                if(mCurrent_state.equals("req_received"))
                {
                    final String CurrentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+ mCurrent_user.getUid() + "/" + Friend_id + "/date",CurrentDate);
                    friendsMap.put("Friends/"+ Friend_id + "/" + mCurrent_user.getUid() +"/date",CurrentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + Friend_id,null);
                    friendsMap.put("Friend_req/" + Friend_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            mrequestsendbtn.setEnabled(true);

                            if(databaseError == null)
                            {
                                mCurrent_state="friends";
                                mrequestsendbtn.setText("UnFriend");
                                mrequestdeclinebtn.setVisibility(View.GONE);
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Error while accepting request.",Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }
                //Friends State
                if(mCurrent_state.equals("friends"))
                {
                    Map UnFriendMap = new HashMap();
                    UnFriendMap.put("Friends/"+ mCurrent_user.getUid() + "/" + Friend_id,null);
                    UnFriendMap.put("Friends/"+ Friend_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(UnFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            mrequestsendbtn.setEnabled(true);
                            if(databaseError == null)
                            {
                                mCurrent_state="not_friends";
                                mrequestsendbtn.setText("Send Friend Request");
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Error while UnFriending.",Toast.LENGTH_LONG).show();
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
