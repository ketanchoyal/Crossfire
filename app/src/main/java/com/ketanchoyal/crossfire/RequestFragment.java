package com.ketanchoyal.crossfire;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mRequestDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mFriendRequestDatabase;

    private String mCurrent_user_id;

    private View mMainView;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(com.ketanchoyal.crossfire.R.layout.fragment_request, container, false);

        mRequestList = mMainView.findViewById(R.id.requestlist);

        mRootRef=FirebaseDatabase.getInstance().getReference();

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrent_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRequestList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(mRequestDatabase, Request.class)
                        .build();

        FirebaseRecyclerAdapter<Request, RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {

            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.RequestFragment.RequestViewHoldermessage for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_request_layout, parent,false);

                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final RequestViewHolder requestViewHolder, int position,final Request request) {

                final String Friend_id=getRef(position).getKey();
                final String Request_type = request.getRequest_type();

                //Log.d("LOG","Friend ID: "+Friend_id);
                //Log.d("LOG","Request Type: "+Request_type);

                if(Request_type.equals("received"))
                {
                    mUserDatabase.child(Friend_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String UserName = dataSnapshot.child("name").getValue().toString();
                            String thumb = dataSnapshot.child("thumb_image").getValue().toString();

                            requestViewHolder.displayname.setText(UserName);
                            requestViewHolder.setUserImage(thumb, getContext());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    requestViewHolder.request_accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            requestViewHolder.request_accept.setEnabled(false);
                            final String CurrentDate = DateFormat.getDateTimeInstance().format(new Date());

                            Map friendsMap = new HashMap();
                            friendsMap.put("Friends/" + mCurrent_user_id + "/" + Friend_id + "/date", CurrentDate);
                            friendsMap.put("Friends/" + Friend_id + "/" + mCurrent_user_id + "/date", CurrentDate);

                            friendsMap.put("Friend_req/" + mCurrent_user_id + "/" + Friend_id, null);
                            friendsMap.put("Friend_req/" + Friend_id + "/" + mCurrent_user_id, null);

                            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                    if (databaseError == null) {
                                        //remove data from recyclerview
                                    } else {
                                        requestViewHolder.request_accept.setEnabled(true);
                                        Toast.makeText(getContext(), "Error while accepting request.", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        }
                    });

                    requestViewHolder.request_decline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            requestViewHolder.request_decline.setEnabled(false);

                            mRequestDatabase.child(Friend_id).child("request_type").setValue(null)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                mFriendRequestDatabase.child(Friend_id).child(mCurrent_user_id).child("request_type").setValue("declined")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()) {
                                                                    //Toast.makeText(ProfileActivity.this,"Request Sent.",Toast.LENGTH_LONG).show();
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                requestViewHolder.request_decline.setEnabled(true);
                                            }
                                        }
                                    });

                        }
                    });

                    requestViewHolder.displayimage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent profileintent = new Intent(getContext(), ProfileActivity.class);
                            profileintent.putExtra("user_id", Friend_id);
                            startActivity(profileintent);

                        }
                    });

                }
                else
                {
                    requestViewHolder.request_decline.setEnabled(false);
                    requestViewHolder.request_accept.setEnabled(false);
                }


            }
        };
        mRequestList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView displayname;
        CircleImageView displayimage;
        Button request_accept, request_decline;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            displayname = mView.findViewById(R.id.request_user_name);
            displayimage=mView.findViewById(R.id.request_user_image);
            request_accept = mView.findViewById(R.id.accept_btn);
            request_decline = mView.findViewById(R.id.decline_btn);

        }

        public void setUserImage(String thumb, Context ctx)
        {
            if(!thumb.equals("default"))
                Picasso.with(ctx).load(thumb).placeholder(R.drawable.default_profile).into(displayimage);

        }

    }

}
