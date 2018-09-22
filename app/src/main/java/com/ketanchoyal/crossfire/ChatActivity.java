package com.ketanchoyal.crossfire;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUserId;
    private String mFriendName;
    private String mCurrentUserId;

    private TextView mTitleView;
    private TextView mLastseenView;
    private EditText mChatmessageView;

    private RecyclerView mMessageList;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private CircleImageView mProfileImage;
    private SwipeRefreshLayout mRefreshLayout;

    private DatabaseReference mRootRef;

    private Toolbar mChatToolbar;
    private FirebaseAuth firebaseAuth;

    private final List<Messages> messagesList = new ArrayList<>();

    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdaptor;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos=0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar= findViewById(R.id.chat_app_bar);

        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        firebaseAuth= FirebaseAuth.getInstance();
        mRootRef= FirebaseDatabase.getInstance().getReference();

        /* TODO online not working for chatactivity*/


        mChatUserId =getIntent().getStringExtra("user_id");
        mFriendName=getIntent().getStringExtra("user_name");
        mCurrentUserId=firebaseAuth.getCurrentUser().getUid();

        //mRootRef.child("Users").child(mChatUserId).child("online").setValue("true");

        getSupportActionBar().setTitle(null);

        LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action Bar---

        mTitleView = findViewById(R.id.custom_bar_title);
        mLastseenView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        mChatmessageView = findViewById(R.id.chat_type_msg_field);
        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_msg_btn);

        mAdaptor = new MessageAdapter(messagesList);

        mMessageList = findViewById(R.id.message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRefreshLayout = findViewById(R.id.message_swipe_layout);

        mMessageList.setAdapter(mAdaptor);

        loadMessages();

        mTitleView.setText(mFriendName);

        if(mFriendName==null) {

            mRootRef.child("Users").child(mChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mFriendName = dataSnapshot.child("name").getValue().toString();
                    //getSupportActionBar().setTitle(mFriendName);
                    mTitleView.setText(mFriendName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        /* TODO : addListinerForSingleValue*/
        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online.equals("true"))
                {
                    mLastseenView.setText("Online");
                }
                else
                {
                    GetTimeAgo getTimeAgo=new GetTimeAgo();

                    long lastseentime = Long.parseLong(online);

                    String LastSeen =getTimeAgo.getTimeAgo(lastseentime);

                    mLastseenView.setText(LastSeen);
                }

                if(!image.equals("default"))
                    Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.default_profile).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUserId))
                {
                    Map ChatAddMap = new HashMap();
                    ChatAddMap.put("seen",false);
                    ChatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map ChatUserMap = new HashMap();
                    ChatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUserId,ChatAddMap);
                    ChatUserMap.put("Chat/" + mChatUserId + "/" + mCurrentUserId,ChatAddMap);

                    mRootRef.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Log.d("CHAT_LOG", databaseError.getMessage());
                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mChatSendBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               sendMessage();

           }
       });

       mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {
               mCurrentPage++;

               itemPos=0;

               loadMoreMessages();
           }
       });

    }

    private void loadMoreMessages() {

        DatabaseReference messageref = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId);

        Query messageQuery = messageref.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey))
                {
                    messagesList.add(itemPos++,message);
                }
                else
                {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1)
                {
                    mLastKey = messageKey;
                }

                mAdaptor.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(12,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageref = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId);

        Query messageQuery = messageref.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1)
                {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdaptor.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message=mChatmessageView.getText().toString().trim();

        if(!TextUtils.isEmpty(message))
        {
            String current_user_ref = "messages"+ "/" + mCurrentUserId + "/" + mChatUserId;
            String chat_user_ref = "messages"+ "/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_msg_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId).push();

            String push_id= user_msg_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messaseuserMap = new HashMap();
            messaseuserMap.put(current_user_ref + "/" + push_id,messageMap);
            messaseuserMap.put(chat_user_ref + "/" + push_id,messageMap);

            mChatmessageView.setText(null);

            mRootRef.updateChildren(messaseuserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError != null)
                    {
                        Log.d("CHAT_MSG_LOG", databaseError.getMessage());
                    }

                }
            });

        }

    }
}
