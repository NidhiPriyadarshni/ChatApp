package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileUpdate extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView imageView;
    EditText name,about,phone;
    //TextView phone;
    Button update;
    ProgressBar progressBar;
    FirebaseAuth mauth;
    FirebaseUser user;
    String userid;
    String profileid;
    final int SELECT_IMAGE =1;
    StorageReference rootref;
    DatabaseReference rootdb;
    String downloadurl;
    String status;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
        initialize();
        insert();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userid.contentEquals(profileid))updateprofile();
                else if(status.contentEquals("new")) sendrequest();
                else if(status.contentEquals("sent")) managesentrequest();
                else if(status.contentEquals("received")) managereceivedrequest();
                else if(status.contentEquals("friends")) block();
                else if(status.contentEquals("blocked"))unblock();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case SELECT_IMAGE:
                    
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(this);
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        Uri resultUri = result.getUri();
                        imageView.setImageURI(resultUri);
                        uploadpic(resultUri);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Toast.makeText(ProfileUpdate.this,result.getError().toString(),Toast.LENGTH_LONG).show();
                    }
            }

    }

    void updateprofile(){
        HashMap<String,Object> map=new HashMap<>();
        final String username=name.getText().toString().trim();
        final String userabout =about.getText().toString().trim();
        if(TextUtils.isEmpty(username)){
            name.setError("Username required");
            name.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(userabout)){
            about.setError("About required");
            about.requestFocus();
            return;
        }

        map.put("Uid",user.getUid());
        map.put("Name",username);
        map.put("Phone",user.getPhoneNumber());
        map.put("About",userabout);



        progressBar.setVisibility(View.VISIBLE);
        rootdb.child("Users").child(user.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                    Toast.makeText(ProfileUpdate.this,"Profile updated successfuly",Toast.LENGTH_SHORT).show();
                    send(MainActivity.class);
                }else {
                    Toast.makeText(ProfileUpdate.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    void uploadpic(Uri uri){
        progressBar.setVisibility(View.VISIBLE);
       rootref.child("ProfilePic").child(user.getUid()+".png").putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               progressBar.setVisibility(View.INVISIBLE);
               if(task.isSuccessful()){
                   rootref.child("ProfilePic/"+userid+".png").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                       @Override
                       public void onComplete(@NonNull Task<Uri> task) {
                          downloadurl=task.getResult().toString();
                           Picasso.get().load(downloadurl).into(imageView);
                           rootdb.child("Users/"+userid+"/"+"Pic").setValue(downloadurl);

                       }
                   });
                  // about.setText(downloadurl);
                  // rootdb.child("Users/"+userid+"/"+"Pic").setValue(downloadurl);
                  // task.getResult().u
                   //downloadurl=task.getResult().getStorage().getDownloadUrl().toString();
                   //downloadurl= rootref.child("ProfilePic").child(user.getUid()+".png").getDownloadUrl().toString();
               }else {
                   Toast.makeText(ProfileUpdate.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
               }
           }
       });
    }

    void initialize(){
        toolbar=findViewById(R.id.app_bar_layout_update);
        imageView=findViewById(R.id.profile_image_update);
        name=findViewById(R.id.name_update);
        about=findViewById(R.id.about_update);
        phone=findViewById(R.id.phone_update);
        phone.setFocusable(false);
        update=findViewById(R.id.button_update);
        progressBar=findViewById(R.id.progress_update);
        status="new";
        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser();
        userid=user.getUid();
        profileid=getIntent().getStringExtra("uid");
        //Toast.makeText(ProfileUpdate.this,userid+"\n"+profileid,Toast.LENGTH_LONG).show();
        rootref= FirebaseStorage.getInstance().getReference();
        rootdb= FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatApp");
        phone.setText(user.getPhoneNumber().toString());
    }

    void selectImage(){
        if(!userid.contentEquals(profileid)){return;}
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,SELECT_IMAGE);

    }

    void send(Class c){
        Intent i=new Intent(ProfileUpdate.this,c);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    void insert(){
        if(!userid.contentEquals(profileid)){
            update.setText("Send request");
            name.setFocusable(false);
            about.setFocusable(false);
            imageView.setFocusable(false);

        }
        rootdb.child("Contacts").child(userid).child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String s=snapshot.getValue().toString();
                    if(s.contentEquals("Friends")){
                        status="friends";
                        update.setText("Block");
                        Toast.makeText(ProfileUpdate.this,"You are friends",Toast.LENGTH_SHORT).show();
                    }else if(s.contentEquals("Blocked")){
                        status="blocked";
                        update.setText("Unblock");
                    }
                }else{
                    rootdb.child("Requests").child(userid).child(profileid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String sts=snapshot.getValue(String.class);
                                if(sts.contentEquals("Sent")){
                                    status="sent";
                                    update.setText("Cancle Request");
                                }else if(sts.contentEquals("Received")){
                                    status="received";
                                    update.setText("Accept/Discard request");

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        rootdb.child("Users").child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("Name"))name.setText(snapshot.child("Name").getValue(String.class));
                    if(snapshot.hasChild("About"))about.setText(snapshot.child("About").getValue(String.class));
                    if(snapshot.hasChild("Phone"))phone.setText(snapshot.child("Phone").getValue(String.class));
                    if(snapshot.hasChild("Pic")){
                        String picuri=snapshot.child("Pic").getValue(String.class);
                        Picasso.get().load(picuri).into(imageView);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void sendrequest(){
        rootdb.child("Requests").child(userid).child(profileid).setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rootdb.child("Requests").child(profileid).child(userid).setValue("Received").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       Toast.makeText(ProfileUpdate.this,"Request sent",Toast.LENGTH_SHORT);
                       status="sent";
                       update.setText("Cancle Request");
                    }
                });
            }
        });
    }


    void managesentrequest(){
        builder=new AlertDialog.Builder(ProfileUpdate.this);
        builder.setMessage("Are you sure want to cancle  request");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                canclerequest();
                status="new";
                update.setText("Send request");
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    void canclerequest(){
        rootdb.child("Requests").child(userid).child(profileid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rootdb.child("Requests").child(profileid).child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!status.contentEquals("friends"))Toast.makeText(ProfileUpdate.this,"Request canceled successfuly",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    void acceptrequest(){
        rootdb.child("Contacts").child(userid).child(profileid).setValue("Friends").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rootdb.child("Contacts").child(profileid).child(userid).setValue("Friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileUpdate.this,"Request accepted successfuly",Toast.LENGTH_SHORT).show();
                        status="friends";
                        update.setText("Block");
                    }
                });
            }
        });
    }


    void managereceivedrequest(){
        builder=new AlertDialog.Builder(ProfileUpdate.this);
        builder.setMessage("You received a request");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                canclerequest();
                acceptrequest();


            }
        }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                canclerequest();
                status="new";
                update.setText("Send request");

            }
        });
        builder.create().show();
    }

    void  block(){
        builder=new AlertDialog.Builder(ProfileUpdate.this);
        builder.setMessage("You want to block the user");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                blockuser();


            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });
        builder.create().show();
    }

    void blockuser(){
        rootdb.child("Contacts").child(userid).child(profileid).setValue("Blocked").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                status="blocked";
                update.setText("Unblock");
            }
        });
    }

    void  unblock(){
        builder=new AlertDialog.Builder(ProfileUpdate.this);
        builder.setMessage("You want to unblock the user");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                unblockuser();


            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });
        builder.create().show();
    }

    void unblockuser(){
        rootdb.child("Contacts").child(userid).child(profileid).setValue("Friends").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                status="friends";
                update.setText("Block");
            }
        });
    }



}