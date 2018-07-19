package com.ketanchoyal.crossfire;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mUserRef;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ketanchoyal.crossfire.R.layout.activity_main);

        mToolbar=findViewById(com.ketanchoyal.crossfire.R.id.mainpage_toolbar);
        setSupportActionBar(mToolbar);

        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid());
        }

        mViewPager=findViewById(com.ketanchoyal.crossfire.R.id.main_tabpager);

        mSectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout=findViewById(com.ketanchoyal.crossfire.R.id.main_tab_bottom);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sendtostart();
    }

    private void sendtostart() {
        if(firebaseAuth.getCurrentUser() == null)
        {
            Intent intent = new Intent(this,StartActivity.class);
            startActivity(intent);
            finish();

        }
        else
        {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(firebaseAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(com.ketanchoyal.crossfire.R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() ==  R.id.main_logout_btn)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            firebaseAuth.getInstance().signOut();
            sendtostart();
        }

        if(item.getItemId() == R.id.main_setting_btn)
        {
            Intent settingsintend = new Intent(this,SettingsActivity.class);
            startActivity(settingsintend);
        }
        if(item.getItemId() == R.id.main_all_users)
        {
            Intent alluserintend = new Intent(this, UsersActivity.class);
            startActivity(alluserintend);
        }

        return true;
    }
}
