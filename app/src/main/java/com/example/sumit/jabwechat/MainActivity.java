package com.example.sumit.jabwechat;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
   private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DatabaseReference mUserRef;
    private FirebaseUser current_user;
    private static final int PERMISSION_REQUEST_CODE = 123;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Jab We Chat");

       //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
      //  if (mAuth.getCurrentUser() != null)
        //        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        //current_user = mAuth.getCurrentUser();

        mViewPager = (ViewPager) findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                   // Toast.makeText(MainActivity.this,"onAuthStateChanged:signed_in:"+ user.getUid(), Toast.LENGTH_SHORT).show();
                    mUserRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(mAuth.getCurrentUser().getUid());
                    current_user = mAuth.getCurrentUser();


                } else {
                    // User is signed out
                    //Toast.makeText(MainActivity.this,"error", Toast.LENGTH_SHORT).show();
                    sendToStart();
                }
            }
        };
        if(hasPermission()){

        }
        else{
            requestPermissions();
        }

    }
    private boolean hasPermission(){
        int res = 0;
        String[] permissions = new String[]{Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

        for(String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if(!(res == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(){
        String[] permissions = new String[]{Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)){
            requestPermissions(permissions,PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch(requestCode){
            case PERMISSION_REQUEST_CODE:
                for(int res : grantResults){
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                allowed = false;
                break;
        }
        if(!allowed){
            Toast.makeText(MainActivity.this,"Permission not Granted... Please grant the permissions",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

       // FirebaseUser current_user = mAuth.getCurrentUser();


        //------correct
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mUser.getUid());
            mUserRef.child("online").setValue("true");
        }
        else{
            //Toast.makeText(this,"current user is null",Toast.LENGTH_SHORT).show();
        }

        //-----try
       /* if(current_user!=null){
            if (mAuth.getCurrentUser() != null) {
                //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                mUserRef.child("online").setValue(false);
            }
            else{
                Toast.makeText(this,"mauth is null",Toast.LENGTH_SHORT).show();
            }
        }

        else{
             Toast.makeText(this,"current user is null",Toast.LENGTH_SHORT).show();
        }*/


        //FirebaseUser current_user = mAuth.getCurrentUser();

        //if(current_user == null){
          //  sendToStart();
        //}
        //else{
          //  mUserRef.child("online").setValue(true);
        //}

    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //mUserRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
        //FirebaseUser current_user = mAuth.getCurrentUser();
       // Toast.makeText(MainActivity.this,"onAuthStateChanged:signed_in:"+ current_user.getUid(), Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(current_user!=null){
            if (mAuth.getCurrentUser() != null) {
                //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
            }
            else{
                //Toast.makeText(this,"mauth is null",Toast.LENGTH_SHORT).show();
            }
        }

        else{
            // Toast.makeText(this,"current user is null",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_btn){
            FirebaseUser current_user = mAuth.getCurrentUser();
            if(current_user!=null)
              mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();

            sendToStart();
        }
        if(item.getItemId()==R.id.main_settings_btn){
            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId()==R.id.main_users_btn){
            Intent usersIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(usersIntent);
        }

        return  true;
    }
    public void sendToStart(){
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

}
