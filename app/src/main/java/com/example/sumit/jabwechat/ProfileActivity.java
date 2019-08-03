package com.example.sumit.jabwechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.StringPrepParseException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private TextView mProfileName;
    private ImageView mProfileImageView;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendRequestbtn;
    private Button mProfileDeclineRequestbtn;

    private DatabaseReference mUserDatabase;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mNotiicationDatabase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mFriendDatabase;

    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    //private String user_id;

    private String mCurrent_state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mProfileName = (TextView) findViewById(R.id.user_profile_name);
        mProfileStatus = (TextView) findViewById(R.id.user_profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.user_profile_friends_counter);
        mProfileImageView = (ImageView) findViewById(R.id.user_profile_image);
        mProfileSendRequestbtn = (Button) findViewById(R.id.send_friend_request_btn);
        mProfileDeclineRequestbtn = (Button) findViewById(R.id.decline_friend_request_btn);

        mCurrent_state = "not_friends";

        mAuth = FirebaseAuth.getInstance();
        //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotiicationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = getIntent().getStringExtra("user_id");
        if(mCurrent_user.getUid().equals(user_id)){
            mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
            mProfileDeclineRequestbtn.setEnabled(false);
            mProfileSendRequestbtn.setVisibility(View.INVISIBLE);
            mProfileSendRequestbtn.setEnabled(false);
        }
        mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
        mProfileDeclineRequestbtn.setEnabled(false);
        //Toast.makeText(ProfileActivity.this,user_id+"",Toast.LENGTH_SHORT).show();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user Data");
        mProgressDialog.setMessage("Please Wait.....");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String displayStatus = dataSnapshot.child("status").getValue().toString();
                final String displayImage = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(displayStatus);
               Picasso.with(ProfileActivity.this).load(displayImage).networkPolicy(NetworkPolicy.OFFLINE)
                       .placeholder(R.drawable.default_avatar).into(mProfileImageView, new Callback() {
                   @Override
                   public void onSuccess() {

                   }

                   @Override
                   public void onError() {
                       Picasso.with(ProfileActivity.this).load(displayImage).placeholder(R.drawable.default_avatar).into(mProfileImageView);
                   }
               });
                //Friend List------------------------------------
                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equalsIgnoreCase("received")){
                                mCurrent_state = "req_received";
                                mProfileSendRequestbtn.setText("Accept Friend Request");

                                mProfileDeclineRequestbtn.setVisibility(View.VISIBLE);
                                mProfileDeclineRequestbtn.setEnabled(true);

                            }
                            else if(req_type.equalsIgnoreCase("sent")){
                                mCurrent_state = "req_sent";
                                mProfileSendRequestbtn.setText("Cancel Friend Request");

                                mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestbtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendRequestbtn.setText("UnFriend This Person");
                                        mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRequestbtn.setEnabled(false);

                                        mProgressDialog.dismiss();


                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });

                            mProgressDialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendRequestbtn.setEnabled(false);
                //SEND FRIENDREQUEST ----------------------------------------------------
                if(mCurrent_state.equalsIgnoreCase("not_friends")){
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String,String> notifiactionData = new HashMap<String, String>();
                                        notifiactionData.put("from",mCurrent_user.getUid());
                                        notifiactionData.put("type","request");

                                        mNotiicationDatabase.child(user_id).push().setValue(notifiactionData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mCurrent_state = "req_sent";
                                                mProfileSendRequestbtn.setText("CANCEL FRIEND REQUEST");

                                                mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineRequestbtn.setEnabled(false);
                                            }
                                        });

                                        mProfileSendRequestbtn.setEnabled(true);

                                        //Toast.makeText(ProfileActivity.this,"Request Sent",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{

                                Toast.makeText(ProfileActivity.this,"Failed Sending Request",Toast.LENGTH_SHORT).show();
                                mProfileSendRequestbtn.setEnabled(true);
                            }
                        }
                    });
                }

                //CANCEL REQUEST-----------------------
                if(mCurrent_state.equalsIgnoreCase("req_sent")){
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendRequestbtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mProfileSendRequestbtn.setText("SEND FRIEND REQUEST");

                                                    mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineRequestbtn.setEnabled(false);
                                                    //Toast.makeText(ProfileActivity.this,"Friend Request canceled",Toast.LENGTH_SHORT).show();
                                                }
                                            });

                        }
                    });
                }

                //--------------------Request Received-----------------------
                if(mCurrent_state.equalsIgnoreCase("req_received")){
                    Date dNow = new Date( );
                    SimpleDateFormat ft =
                            new SimpleDateFormat ("yyyy.MM.dd 'at' hh:mm:ss a");

                    final String current_date = ft.format(dNow);
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(current_date).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                             mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(current_date)
                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                     mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                         @Override
                                                         public void onSuccess(Void aVoid) {
                                                             mProfileSendRequestbtn.setEnabled(true);
                                                             mCurrent_state = "friends";
                                                             mProfileSendRequestbtn.setText("UnFriend This Person");

                                                             mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                                             mProfileDeclineRequestbtn.setEnabled(false);
                                                             //Toast.makeText(ProfileActivity.this,"Friend Request canceled",Toast.LENGTH_SHORT).show();
                                                         }
                                                     });

                                         }
                                     });
                                 }
                             });
                        }
                    });
                }

                //---------------------unFriend------------------------------------
                if(mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequestbtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendRequestbtn.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRequestbtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

            }
        });

        mProfileDeclineRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mCurrent_state = "not_friends";
                                mProfileSendRequestbtn.setText("SEND FRIEND REQUEST");
                                mProfileDeclineRequestbtn.setVisibility(View.GONE);
                                mProfileDeclineRequestbtn.setEnabled(false);
                            }
                        });
                    }
                });
            }
        });

    }

    //online activity here..............................
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mUser.getUid());
            mUserRef.child("online").setValue("true");
        }
        else{
            //Toast.makeText(this,"current user is null",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser!=null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

}
