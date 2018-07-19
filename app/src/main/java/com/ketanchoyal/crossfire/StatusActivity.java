package com.ketanchoyal.crossfire;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private TextView appbarheading;
    private TextInputLayout mStatusInput;

    private Toolbar toolbar;

    private Button mStatusChangebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        toolbar =findViewById(R.id.status_appbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appbarheading=findViewById(R.id.main_toolbar_heading);
        appbarheading.setText("Status");

        currentuser= FirebaseAuth.getInstance().getCurrentUser();
        String Uid=currentuser.getUid();
        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);

        mStatusChangebtn=findViewById(R.id.status_change_btn);
        mStatusInput=findViewById(R.id.status_input);

        String Status_value=getIntent().getStringExtra("Status_value");
        mStatusInput.getEditText().setText(Status_value);

        mStatusChangebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Status=mStatusInput.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(Status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(StatusActivity.this,"Status Updated Successfully.",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });

    }
}
