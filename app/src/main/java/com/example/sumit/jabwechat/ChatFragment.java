package com.example.sumit.jabwechat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 *
 */
public class ChatFragment extends Fragment {
    private RecyclerView mChatList;

    private DatabaseReference mChatDatabase;
    private DatabaseReference mUsersReference;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chat, container, false);

        mChatList = (RecyclerView) mMainView.findViewById(R.id.chat_list);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() !=null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
            mChatDatabase.keepSynced(true);
        }



        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersReference.keepSynced(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null){
            FirebaseRecyclerAdapter<Friends,ChatViewHolder> firebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Friends, ChatViewHolder>(
                    Friends.class,
                    R.layout.users_single_layout,
                    ChatViewHolder.class,
                    mChatDatabase
            ) {
                @Override
                protected void populateViewHolder(final ChatViewHolder viewHolder, Friends model, int position) {
                    final String list_user_id = getRef(position).getKey();
                    mUsersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                            //  if(dataSnapshot.hasChild("online")){
                            //    String userOnline = dataSnapshot.child("online").getValue().toString();
                            //    viewHolder.setUserOnline(userOnline);
                            // }

                            viewHolder.setName(userName);
                            viewHolder.setStatus(status);
                            viewHolder.setuserPic(userThumbImage,getContext());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("user_id",list_user_id);
                                    chatIntent.putExtra("user_name",userName);
                                    startActivity(chatIntent);
                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };

            mChatList.setAdapter(firebaseRecyclerAdapter);
        }

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ChatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setStatus(String status){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }
        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setuserPic(final String thumb_image, final Context ctx){
            final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_avatar).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar)
                            .into(userImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar)
                                            .into(userImageView);
                                }
                            });
                }
            });
        }
        /*public void setUserOnline(String online_icon){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.show_online_image);
            if(online_icon.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }*/
    }
}
