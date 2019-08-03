package com.example.sumit.jabwechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    //private DatabaseReference mUserRef;
    //private FirebaseAuth mAuth;

    private Button mImagebtn;
    private Button mStatusbtn;
    private byte[] thumb_byte;
    private ProgressDialog mProgress;
    private StorageReference mStorageRef;
    private static final int GALLERY_PICK = 1;


    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = (CircleImageView) findViewById(R.id.profile_image);
        mName = (TextView) findViewById(R.id.default_name);
        mStatus = (TextView) findViewById(R.id.default_status);
        mImagebtn = (Button) findViewById(R.id.image_change_btn);
        mStatusbtn = (Button) findViewById(R.id.status_change_btn);


        mStorageRef = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String image = dataSnapshot.child("image").getValue(String.class);
                String name = dataSnapshot.child("name").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                String thumb_image = dataSnapshot.child("thumb_image").getValue(String.class);


                mName.setText(name);
                mStatus.setText(status);
                if(!image.equalsIgnoreCase("default")){
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar)
                                    .into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusValue = mStatus.getText().toString();
                startActivity(new Intent(SettingsActivity.this,StatusActivity.class).putExtra("statusValue",statusValue));
            }
        });

        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
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















    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_filepath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(SettingsActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading Images...");
                mProgress.setMessage("Please wait while we upload and process the image");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                String current_user_id = mCurrentUser.getUid();
                final StorageReference filepath = mStorageRef.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thumbs_filepath = mStorageRef.child("profile_images").child("thumb").child(current_user_id+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(SettingsActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    UploadTask uploadTask = thumbs_filepath.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            String thumb_downloadURL = task.getResult().getDownloadUrl().toString();
                                                if(task.isSuccessful()){
                                                    Map update_Hashmap = new HashMap<String, String>();
                                                    update_Hashmap.put("image",downloadUrl);
                                                    update_Hashmap.put("thumb_image",thumb_downloadURL);
                                                    mUserDatabase.updateChildren(update_Hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                mProgress.dismiss();
                                                                Toast.makeText(SettingsActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                                            }
                                                            else{

                                                            }
                                                        }
                                                    });
                                                }
                                                else{
                                                    Toast.makeText(SettingsActivity.this,"Error.. In Uploading",Toast.LENGTH_SHORT).show();

                                                }

                                        }
                                    });
                                    /*mUserDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mProgress.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                            }
                                            else{

                                            }
                                        }
                                    });*/
                                }
                            });
                        }
                        else{
                            Toast.makeText(SettingsActivity.this,"Error.. In Uploading",Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
