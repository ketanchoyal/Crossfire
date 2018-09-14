package com.ketanchoyal.crossfire;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;

    private TextView mName;
    private TextView mStatus;
    private CircleImageView mDisplayimage;

    private Button mChangeStatus;
    private Button mChangeImage;
    private ProgressDialog mProgressDialog;

    //firebase
    private DatabaseReference mUserDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser current_user;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mChangeImage=findViewById(R.id.change_image_btn);
        mChangeStatus=findViewById(R.id.change_status_btn);
        mDisplayimage=findViewById(R.id.user_profile_image);
        mName=findViewById(R.id.displayname);
        mStatus=findViewById(R.id.statustext);

        firebaseAuth=FirebaseAuth.getInstance();
        current_user=firebaseAuth.getCurrentUser();

        String uid=current_user.getUid();

        mImageStorage= FirebaseStorage.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();
                final String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!thumb_image.equals("default"))
                {
                    Picasso.with(SettingsActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile).into(mDisplayimage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.default_profile).into(mDisplayimage);

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Status_value=mStatus.getText().toString();
                Intent statusIntent =new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("Status_value",Status_value);
                startActivity(statusIntent);
            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ImageIntent =new Intent();
                ImageIntent.setType("image/*");
                ImageIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(ImageIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri ImageUri=data.getData();
            CropImage.activity(ImageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgressDialog=new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading....");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String uid=current_user.getUid();

                StorageReference imagefilepath=mImageStorage.child("profile_images").child(uid+".jpg");
                final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(uid+".jpg");

                Uri resultUri = result.getUri();

                File imagefile =new File(resultUri.getPath());

                Bitmap profile_bitmap=null;
                try {
                    profile_bitmap = new Compressor(this)
                            .setQuality(100)
                            .compressToBitmap(imagefile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream profilebaos = new ByteArrayOutputStream();
                assert profile_bitmap != null;
                profile_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, profilebaos);
                final byte[] profile_byte = profilebaos.toByteArray();

                Bitmap thumb_bitmap=null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(300)
                            .setMaxHeight(300)
                            .setQuality(75)
                            .compressToBitmap(imagefile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream thumbbaos = new ByteArrayOutputStream();
                assert thumb_bitmap != null;
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbbaos);
                final byte[] thumb_byte = thumbbaos.toByteArray();

                UploadTask uploadTask_profile = imagefilepath.putBytes(profile_byte);
                uploadTask_profile.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            final String download_url=task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                            UploadTask uploadTask_thumb = thumb_filepath.putBytes(thumb_byte);
                            uploadTask_thumb.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    if(thumb_task.isSuccessful())
                                    {
                                        String thumb_download_url=thumb_task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                                        Map<String, Object> update_hashmap=new HashMap<>();
                                        update_hashmap.put("image",download_url);
                                        update_hashmap.put("thumb_image",thumb_download_url);

                                        mUserDatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mProgressDialog.dismiss();
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingsActivity.this,"Profile Changed Succesfully.",Toast.LENGTH_LONG).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(SettingsActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }

                                }
                            });

                        }
                        else
                        {
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

}
