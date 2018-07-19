package com.ketanchoyal.crossfire;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayname;
    private TextInputLayout email;
    private TextInputLayout password;

    private TextView mRegHeading;

    private Toolbar mToolbar;

    private Button mRegisterbtn;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference mDatabase;

    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ketanchoyal.crossfire.R.layout.activity_register);

        mToolbar = findViewById(com.ketanchoyal.crossfire.R.id.reg_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegHeading=findViewById(R.id.main_toolbar_heading);
        mRegHeading.setText("register Account");

        mDisplayname=findViewById(com.ketanchoyal.crossfire.R.id.reg_display_name);
        email=findViewById(com.ketanchoyal.crossfire.R.id.reg_email_id);
        password=findViewById(com.ketanchoyal.crossfire.R.id.reg_pass);

        mRegProgress=new ProgressDialog(this);

        mRegisterbtn=findViewById(com.ketanchoyal.crossfire.R.id.reg_btn);

        firebaseAuth=FirebaseAuth.getInstance();

        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name=mDisplayname.getEditText().getText().toString().trim();
                String Email=email.getEditText().getText().toString().trim();
                String pass=password.getEditText().getText().toString().trim();

                if(!TextUtils.isEmpty(Name) || !TextUtils.isEmpty(Email) || !TextUtils.isEmpty(pass))
                {
                    mRegProgress.setTitle("Registering User.");
                    mRegProgress.setMessage("Please wait, your account is being created.");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registeruser(Name,Email,pass);
                }
            }
        });
    }

    private void registeruser(final String name, String email, String pass) {

        firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser current_user=firebaseAuth.getCurrentUser();
                    String uid=current_user.getUid();
                    String device_token= FirebaseInstanceId.getInstance().getToken();

                    mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> usermap=new HashMap<>();
                    usermap.put("name",name);
                    usermap.put("status","Just Started using 2018 App.");
                    usermap.put("image","default");
                    usermap.put("thumb_image","default");
                    usermap.put("device_token",device_token);

                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mRegProgress.dismiss();
                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }
                else
                {
                    mRegProgress.dismiss();
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
