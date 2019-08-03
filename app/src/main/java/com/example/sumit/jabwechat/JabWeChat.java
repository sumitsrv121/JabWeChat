package com.example.sumit.jabwechat;

import android.app.Application;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Sumit on 8/25/2017.
 */

public class JabWeChat extends Application {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference mDatabaseUsers;
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /*picasso*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth mAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    // add auth state - user is not-null
                    // launch login activity
                    startActivity(new Intent(JabWeChat.this, LoginActivity.class));

                } else {

                    mDatabaseUsers = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(mAuth.getCurrentUser().getUid());

                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot != null){

                                mDatabaseUsers.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                                //mDatabaseUsers.child("lastSeen").setValue(ServerValue.TIMESTAMP);


                            } else {

                                Toast.makeText(JabWeChat.this, "Failed!", Toast.LENGTH_LONG).show();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        };

    }
}