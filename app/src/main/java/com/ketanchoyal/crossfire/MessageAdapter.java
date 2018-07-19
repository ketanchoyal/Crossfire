package com.ketanchoyal.crossfire;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daasuu.bl.BubbleLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList=mMessageList;
        mAuth=FirebaseAuth.getInstance();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        int layout=0;

        switch (viewType)
        {
            case 0: layout=(R.layout.message_single_layout_right);
                    View v=LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);
                    viewHolder=new MessageViewHolderRight(v);
                    break;

            case 1: layout=(R.layout.message_single_layout_left);
                View w=LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);
                viewHolder=new MessageViewHolderLeft(w);
                break;
            default:
                viewHolder=null;
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        int viewType=holder.getItemViewType();

        switch (viewType)
        {
            case 0:
                MessageViewHolderRight messageViewHolderRight = (MessageViewHolderRight)holder;
                ((MessageViewHolderRight) holder).messagetextright.setText(c.getMessage());
                break;

            case 1:
                MessageViewHolderLeft messageViewHolderLeft = (MessageViewHolderLeft)holder;
                ((MessageViewHolderLeft) holder).messagetextleft.setText(c.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        if(from_user.equals(current_user_id))
        {
            return 0;

        }
        else
        {
            return 1;
        }

    }

    public class MessageViewHolderLeft extends RecyclerView.ViewHolder
    {
        public TextView messagetextleft;
        public CircleImageView profileimageleft;

        public MessageViewHolderLeft(View view) {
            super(view);

            messagetextleft = view.findViewById(R.id.message_text_layout_left);
            profileimageleft = view.findViewById(R.id.message_profile_layout_left);

        }
    }

    public class MessageViewHolderRight extends RecyclerView.ViewHolder
    {
        public TextView messagetextright;
        public CircleImageView profileimageright;

        public MessageViewHolderRight(View view) {
            super(view);

            messagetextright = view.findViewById(R.id.message_text_layout_right);
            profileimageright = view.findViewById(R.id.message_profile_layout_right);

        }
    }
}


