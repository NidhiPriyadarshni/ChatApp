package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    TabLayout tab;
    ViewPager pager;
    FirebaseAuth mauth;
    FirebaseUser muser;
    DatabaseReference rootdb;
    Toolbar toolbar;
    AlertDialog.Builder builder;
    String userid;
    String findid;
    MainPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    void initialize(){
        mauth=FirebaseAuth.getInstance();
        muser=mauth.getCurrentUser();
        if(muser!=null)userid=muser.getUid();
        rootdb=FirebaseDatabase.getInstance().getReference();
        toolbar=findViewById(R.id.app_bar_layout_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatApp");
        tab=findViewById(R.id.tab_bar_main);
        pager=findViewById(R.id.view_pager_main);
        pagerAdapter=new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tab.setupWithViewPager(pager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(muser==null)send(SignInActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mauth.signOut();
                send(SignInActivity.class);
                break;
            case R.id.find_menu:
                findPerson();
                break;
            case R.id.my_profile_menu:
                Intent intntt=new Intent(MainActivity.this,ProfileUpdate.class);
                intntt.putExtra("uid",userid);
                startActivity(intntt);
                break;
        }
        return true;
    }

    void send(Class c){
        Intent i=new Intent(MainActivity.this,c);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
    void findPerson(){
        builder=new AlertDialog.Builder(MainActivity.this);
        final EditText findphone=new EditText(MainActivity.this);
        builder.setTitle("Enter phone no. to search");
        builder.setView(findphone);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String findphoneno=findphone.getText().toString().trim();
                if(TextUtils.isEmpty(findphoneno)){
                    findphone.setError("Phone no. required");
                    findphone.requestFocus();
                }else{
                      rootdb.child("ContactLog").child(findphoneno).addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot snapshot) {
                              if(snapshot.exists()){
                                  findid=snapshot.getValue(String.class);
                                  Intent intnt=new Intent(MainActivity.this,ProfileUpdate.class);
                                  intnt.putExtra("uid",findid);
                                  startActivity(intnt);

                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError error) {

                          }
                      });
                }
            }
        }).setNegativeButton("Discar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }
}