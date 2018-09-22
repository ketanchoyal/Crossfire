package com.ketanchoyal.crossfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mUserToolbar;
    private TextView mUserHeading;

    private RecyclerView mUserList;

    private DatabaseReference mUsersDatabase;

    //private Query query;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUserHeading=findViewById(R.id.main_toolbar_heading);
        mUserHeading.setText("All Users");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //query=mUsersDatabase.limitToLast(100);

        mUserToolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(mUserToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mUserList=findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

    }

    // Users class is the model for database (get and set data)
    // Layout file to show the data
    // View to hold the view's data (set data into view)
    // DatabaseRef to store reference of the database
    // Adapter acts like a controller

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUsersDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent,false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UsersViewHolder usersViewHolder, int position, Users users) {
                // Bind the Chat object to the ChatHolder
                // ...


                //usersViewHolder.displayname.setText(users.getName());
                //usersViewHolder.displaystatus.setText(users.getStatus());

                usersViewHolder.displayname.setText(users.getName());;
                usersViewHolder.setUserStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getThumb_image(),getApplicationContext());

                final String user_id=getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileintent =new Intent(UsersActivity.this,ProfileActivity.class);
                        profileintent.putExtra("user_id",user_id);
                        startActivity(profileintent);

                    }
                });
            }
        };
        mUserList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    // User view holder is used to get data from FirebaseRecyclerAdapter and put into View (R.layout.single_user_layout)
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView displayname,displaystatus;
        CircleImageView displayimage;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            displaystatus=mView.findViewById(R.id.user_single_status);
            displayname = mView.findViewById(R.id.user_single_name);
            displayimage=mView.findViewById(R.id.user_single_image);

        }
        public  void setDisplayname(String name)
        {
            //TextView displayname = mView.findViewById(R.id.user_single_name);
            displayname.setText(name);
        }

        public void setUserStatus(String status)
        {
            //TextView displaystatus=mView.findViewById(R.id.user_single_status);
            displaystatus.setText(status);
        }

        public  void setUserImage(String thumb_image, Context ctx)
        {
            if(!thumb_image.equals("default"))
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_profile).into(displayimage);

        }
    }
}
