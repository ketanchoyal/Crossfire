package com.ketanchoyal.crossfire;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button regbtn;
    private Button loginbtn;

    private Apppermissions apppermissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        regbtn=findViewById(R.id.change_image);
        loginbtn=findViewById(R.id.change_status_btn);

        apppermissions = new Apppermissions(this);

        if (!apppermissions.checkPermissionForExternalStorage()) {
            apppermissions.requestPermissionForExternalStorage(Apppermissions.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE_BY_GALLERY);
        }

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(login_intent);
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Apppermissions.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE_BY_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (!apppermissions.checkPermissionForCamera())
                    {
                        apppermissions.requestPermissionForCamera();
                    }

                } else {
                    if (!apppermissions.checkPermissionForCamera())
                    {
                        apppermissions.requestPermissionForCamera();
                    }
                }
                break;
            case Apppermissions.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE_BY_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission granted successfully

                } else {

                    //permission denied

                }
                break;
        }

    }
}
