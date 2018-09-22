package com.ketanchoyal.crossfire;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabse;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(com.ketanchoyal.crossfire.R.layout.fragment_friends, container, false);

        mFriendsList= mMainView.findViewById(R.id.friendslist);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabse= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabse.keepSynced(true);
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriendsList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsDatabse, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent,false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int position, final Friends friends) {


                final String Friend_id=getRef(position).getKey();
                mUserDatabase.child(Friend_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String UserName=dataSnapshot.child("name").getValue().toString();
                        String thumb=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String onlinestatus = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnlineStatus(onlinestatus);
                        }

                        friendsViewHolder.setFriendName(UserName);
                        friendsViewHolder.setUserImage(thumb,getContext());
                        friendsViewHolder.setDate(friends.getdate());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence option[]=new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        switch (which) {
                                            case 0 :
                                            {
                                                Intent profileintent = new Intent(getContext(), ProfileActivity.class);
                                                profileintent.putExtra("user_id", Friend_id);
                                                startActivity(profileintent);
                                            }break;
                                            case 1:
                                            {
                                                Intent messageintent = new Intent(getContext(), ChatActivity.class);
                                                messageintent.putExtra("user_name", UserName);
                                                messageintent.putExtra("user_id", Friend_id);
                                                startActivity(messageintent);
                                            }break;
                                        }

                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setDate(String date)
        {
            TextView displayfriends_since=mView.findViewById(R.id.user_single_status);
            displayfriends_since.setText(date);
        }

        public void setFriendName(String UserName)
        {
            TextView displayname=mView.findViewById(R.id.user_single_name);
            displayname.setText(UserName);
        }

        public void setUserImage(String thumb, Context ctx)
        {
            CircleImageView displayimage=mView.findViewById(R.id.user_single_image);
            if(!thumb.equals("default"))
                Picasso.with(ctx).load(thumb).placeholder(R.drawable.default_profile).into(displayimage);

        }

        public void setUserOnlineStatus(String onlinestatus)
        {
            ImageView online_status=mView.findViewById(R.id.online_status);
            if(onlinestatus.equals("true"))
            {
                online_status.setVisibility(View.VISIBLE);
            }
            else
            {
                online_status.setVisibility(View.INVISIBLE);
            }
        }
    }
}
