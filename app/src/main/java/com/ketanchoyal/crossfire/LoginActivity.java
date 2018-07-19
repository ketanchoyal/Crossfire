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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextView mLoginheading;

    private TextInputLayout email;
    private TextInputLayout password;

    private Button mLogin;

    private Toolbar mToolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mUserDatabase;

    private ProgressDialog mLoginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ketanchoyal.crossfire.R.layout.activity_login);

        firebaseAuth=FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginheading=findViewById(R.id.main_toolbar_heading);
        mLoginheading.setText("Login");

        email=findViewById(com.ketanchoyal.crossfire.R.id.login_email);
        password=findViewById(com.ketanchoyal.crossfire.R.id.login_pass);
        mLogin=findViewById(com.ketanchoyal.crossfire.R.id.login_btn);

        mLoginProgress=new ProgressDialog(this);

        mToolbar = findViewById(com.ketanchoyal.crossfire.R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail= Objects.requireNonNull(email.getEditText()).getText().toString().trim();
                String pass= Objects.requireNonNull(password.getEditText()).getText().toString().trim();

                if(!TextUtils.isEmpty(mail) || !TextUtils.isEmpty(pass)) {
                    mLoginProgress.setTitle("Logging in User.");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginuser(mail, pass);
                }
            }
        });
    }

    private void loginuser(String mail, String pass) {
        firebaseAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mLoginProgress.dismiss();

                    String current_uid=firebaseAuth.getCurrentUser().getUid();
                    String device_token= FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_uid).child("device_token").setValue(device_token).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
                else
                {
                    mLoginProgress.dismiss();
                    Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
