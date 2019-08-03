package com.example.sumit.jabwechat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//import static com.example.sumit.jabwechat.R.color.colorPrimary;

/**
 * Created by Sumit on 8/27/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private Context ctx;
    //private RelativeLayout mRelativeLayout;
    public MessageAdapter(List<Messages> mMessageList,Context ctx){
        this.mMessageList = mMessageList;
        this.ctx = ctx;
    }
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
       mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        String mCurrentUserId = mAuth.getCurrentUser().getUid();



        Messages c = mMessageList.get(position);
        String from_user = c.getFrom();


        if(from_user.equals(mCurrentUserId)){
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);

            //Right alingment
            holder.mRelativeLayout.setGravity(Gravity.RIGHT);

            mUserDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userThumbimage = dataSnapshot.child("thumb_image").getValue().toString();
                    if(userThumbimage!=null){
                        Picasso.with(ctx).load(userThumbimage).into(holder.profileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
        else{

            holder.mRelativeLayout.setGravity(Gravity.LEFT);
            mUserDatabase.child(from_user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String senderThumbimage = dataSnapshot.child("thumb_image").getValue().toString();
                    if(senderThumbimage!=null){
                        Picasso.with(ctx).load(senderThumbimage).into(holder.profileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.messageText.setBackgroundResource(R.drawable.messge_text_receive_background);
            holder.messageText.setTextColor(Color.BLACK);


            //holder.profileImage.setLayoutParams(params);
        }
        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public RelativeLayout mRelativeLayout;
        public MessageViewHolder(View itemView) {

            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.message_single_layout);

        }
    }
}
