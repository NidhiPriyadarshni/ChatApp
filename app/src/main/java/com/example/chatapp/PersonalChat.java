package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class PersonalChat extends AppCompatActivity {

    final int SELECT_GALLERY=1,SELECT_DOCUMENT=2;
    Toolbar toolbar;
    RecyclerView recyclerView;
    EditText sendtext;
    CircleImageView profileimage;
    ImageView send;
    ImageButton fileselector;
    TextView profilename,profileabout;
    String profileid;
    FirebaseAuth auth;
    String userid,type;
    DatabaseReference chatref;
    DatabaseReference contref;
    ChatAdapter adapter;
    List<Chatmsg> chatlist=new ArrayList<>();
    boolean sendbool,receivebool;
    Uri pictureuri,documenturi;
    StorageReference picref,docref;
    int progress;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chat);
        initialize();
        chatref.child(userid).child(profileid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Chatmsg chatmsg=snapshot.getValue(Chatmsg.class);
                chatlist.add(chatmsg);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        contref.child(userid+"/"+profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    String s=snapshot.getValue().toString();
                    if(s.contentEquals("Friends")){
                        //Toast.makeText(PersonalChat.this,"u r frnds",Toast.LENGTH_SHORT).show();
                        sendbool=true;
                        contref.child(profileid+"/"+userid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String ss;
                                    ss=snapshot.getValue().toString();
                                    if(ss.contentEquals("Friends")){
                                        receivebool=true;

                                    }else receivebool=false;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }else sendbool=receivebool=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsg();
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PersonalChat.this,ProfileUpdate.class);
                intent.putExtra("uid",profileid);
                startActivity(intent);
            }
        });

        fileselector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectfile();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    void sendmsg(){
        final String[] msg = new String[1];
        final Map<String,Object> map=new HashMap<>();
        SimpleDateFormat format=new SimpleDateFormat("MMM dd,yyyy  hh:mm a");
        Date date=new Date();
        String time=format.format(date);
       // map.put("Message",msg);
        map.put("Time",time);
        map.put("Sender",userid);
        map.put("Receiver",profileid);
        map.put("Type",type);
        final String id= chatref.child(userid).child(profileid).push().getKey();
        final String senderpath=userid+"/"+profileid+"/"+id;
        final String receiverpath=profileid+"/"+userid+"/"+id;
        map.put("Uid",id);

        if(sendbool==true){
            if(!type.equals("text")){
                progressDialog.setMessage("Please wait\nwe are uploading the content");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
            if(type.equals("text")){
                msg[0] =sendtext.getText().toString();
                if(TextUtils.isEmpty(msg[0]))return;
                map.put("Message", msg[0]);

                chatref.child(senderpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())chatref.child(receiverpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    sendtext.setText("");
                                    sendtext.clearFocus();
                                }
                            }
                        });
                    }
                });
            }else if(type.equals("picture")){
                type="text";
                map.put("Name",pictureuri.getLastPathSegment());
                 if(pictureuri!=null)picref.child(userid+"_"+profileid+"_"+id+".jpg").putFile(pictureuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if(task.isSuccessful()){
                             picref.child(userid+"_"+profileid+"_"+id+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Uri> task) {
                                     if(task.isSuccessful()){
                                         msg[0] =task.getResult().toString();
                                         map.put("Message",msg[0]);
                                         chatref.child(senderpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if(task.isSuccessful())chatref.child(receiverpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                     @Override
                                                     public void onComplete(@NonNull Task<Void> task) {
                                                         if(task.isSuccessful()){
                                                           pictureuri=null;
                                                           progressDialog.dismiss();
                                                         }
                                                     }
                                                 });
                                             }
                                         });
                                     }
                                 }
                             });
                         }
                     }
                 }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                         progress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                         progressDialog.setMessage(progress+"% uploading..");
                     }
                 });
            }else if(type.equals("document")){
                type="text";
                map.put("Name",documenturi.getLastPathSegment());
                if(documenturi!=null)docref.child(userid+"_"+profileid+"_"+id+".docx").putFile(documenturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            docref.child(userid+"_"+profileid+"_"+id+".docx").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        msg[0] =task.getResult().toString();
                                        map.put("Message",msg[0]);
                                        chatref.child(senderpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())chatref.child(receiverpath).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                           documenturi=null;
                                                           progressDialog.dismiss();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        progress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage(progress+"% uploading..");
                    }
                });
            }
        }else {
            Toast.makeText(PersonalChat.this,"You have blocked the user,\nCan,t send message.",Toast.LENGTH_LONG).show();
            return;
        }

    }

    void initialize(){
        toolbar=findViewById(R.id.personal_chat_bar);
        setSupportActionBar(toolbar);
        recyclerView=findViewById(R.id.personal_chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter=new ChatAdapter(chatlist);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
        sendtext=findViewById(R.id.personal_chat_box);
        send=findViewById(R.id.personal_chat_send);
        fileselector=findViewById(R.id.personal_chat_file);
        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v= inflater.inflate(R.layout.personal_chat_bar, null);
        toolbar.addView(v);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        profileimage=v.findViewById(R.id.personal_bar_img);
        String profileimg=getIntent().getExtras().get("profileimage").toString();
        if(profileimg!=null&&profileimage!=null)Picasso.get().load(profileimg).into(profileimage);
        profilename=v.findViewById(R.id.personal_bar_name);
        String name=getIntent().getExtras().get("profilename").toString();
        if(name!=null&&profilename!=null)profilename.setText(name);
        profileabout=v.findViewById(R.id.personal_bar_about);
        String about=getIntent().getExtras().get("profileabout").toString();
        if(about!=null&&profileabout!=null)profileabout.setText(about);
        profileid=getIntent().getExtras().get("profileid").toString();
        auth=FirebaseAuth.getInstance();
        userid=auth.getCurrentUser().getUid();
        chatref = FirebaseDatabase.getInstance().getReference("Chat");
        contref=FirebaseDatabase.getInstance().getReference("Contacts");
        type="text";
        picref= FirebaseStorage.getInstance().getReference().child("Picturs");
        docref= FirebaseStorage.getInstance().getReference().child("Documents");
        progressDialog= new ProgressDialog(PersonalChat.this);

    }

    void selectfile(){
        final AlertDialog dialog=new AlertDialog.Builder(PersonalChat.this).create();
        View view=LayoutInflater.from(PersonalChat.this).inflate(R.layout.select_media,null);
        LinearLayout selectgallery,selectdocument;
        selectgallery=view.findViewById(R.id.select_gallery);
        selectdocument=view.findViewById(R.id.select_document);
        selectgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGallery();
                dialog.dismiss();
            }
        });
        selectdocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDocument();
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    void selectGallery(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");


        Intent pickintent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        pickintent.setType("image/*");
        pickintent.putExtra("crop",true);
        pickintent.putExtra("scale",true);
        pickintent.putExtra("outputX",256);
        pickintent.putExtra("outputY",256);
        pickintent.putExtra("aspectX",1);
        pickintent.putExtra("aspectY",1);
        pickintent.putExtra("return-data",true);
        Intent chooser=Intent.createChooser(intent,"Select Picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,new Intent[]{pickintent});
        startActivityForResult(pickintent,SELECT_GALLERY);
    }
    void selectDocument(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/officedocument");
        startActivityForResult(Intent.createChooser(intent,"Select Document"),SELECT_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            final Uri selectedImage=data.getData();
            final AlertDialog dialog=new AlertDialog.Builder(PersonalChat.this).create();
            View view=LayoutInflater.from(PersonalChat.this).inflate(R.layout.send_picture_dialog,null);
            ImageView imageView=view.findViewById(R.id.send_picture_dialog_image);
            ImageButton sendbutton=view.findViewById(R.id.send_picture_dialog_button);
            sendbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    type="picture";
                    pictureuri=selectedImage;
                    dialog.dismiss();
                    sendmsg();
                }
            });
            String[] filePathColumn={MediaStore.Images.Media.DATA};
            Cursor cursor=getContentResolver().query(selectedImage,filePathColumn,null,null,null);
            cursor.moveToFirst();
            int columnIndex=cursor.getColumnIndex(filePathColumn[0]);
            String picturepath=cursor.getString(columnIndex);
            cursor.close();


           // Bitmap bitmap= getScaledBitmap(picturepath,800,800);
            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturepath));
            //imageView.setImageBitmap(getScaledBitmap(picturepath,800,800));
            imageView.setImageURI(selectedImage);
            dialog.setView(view);

            dialog.show();

        }else if(requestCode==SELECT_DOCUMENT&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            documenturi=data.getData();
            final AlertDialog.Builder dialog=new AlertDialog.Builder(PersonalChat.this);
            dialog.setTitle("Want to send file "+documenturi.getLastPathSegment());
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    type="document";

                    sendmsg();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            dialog.create().show();
        }
    }

    Bitmap getScaledBitmap(String picturePath,int width,int height){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(picturePath,options);
        int size=calculatesize(options,width,height);
        options.inJustDecodeBounds=false;
        options.inSampleSize=size;
        return BitmapFactory.decodeFile(picturePath,options);
    }
    int calculatesize(BitmapFactory.Options options,int rwidth,int rheight){
        int height=options.outHeight;
        int width=options.outWidth;
        int sizeratio=1;
        if(height>rheight||width>rwidth){
            int hratio=Math.round((float)height/(float)rheight);
            int wratio=Math.round((float)width/(float)rwidth);
            sizeratio=hratio<wratio?hratio:wratio;
        }
        return sizeratio;
    }
}