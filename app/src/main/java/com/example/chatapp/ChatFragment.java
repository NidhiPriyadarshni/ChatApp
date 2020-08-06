package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ChatFragment extends Fragment {

    private View v;
    RecyclerView recyclerView;

    FirebaseAuth auth;
    String userid;
    DatabaseReference db;
    DatabaseReference userref;
    DatabaseReference contref;
    FirebaseRecyclerAdapter adapter;


    public ChatFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth=FirebaseAuth.getInstance();
        userid=auth.getCurrentUser().getUid();
        db=FirebaseDatabase.getInstance().getReference();
        userref= db.child("Users");
        contref=db.child("Contacts").child(userid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String s[]={"ram","shyam","gij","kil"};
        v= inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView=v.findViewById(R.id.contacts_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
       // recyclerView.setAdapter(new ArrayAdapter<String>());
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<String> options=new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(contref,String.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<String,ViewHolderr>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolderr holder, int position, @NonNull String model) {
                //Toast.makeText(getContext(),"hii",Toast.LENGTH_SHORT).show();
                String uid=getRef(position).getKey();

                userref.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        final String profilepic=snapshot.child("Pic").getValue().toString();
                        if(profilepic!=null)Picasso.get().load(profilepic).into(holder.pic);
                        final String profilename=snapshot.child("Name").getValue().toString();
                        //Toast.makeText(getContext(),"profilename",Toast.LENGTH_LONG).show();
                        final String profileabout=snapshot.child("About").getValue().toString();
                        final String profileid=snapshot.child("Uid").getValue().toString();
                        holder.name.setText(profilename);
                        holder.desc.setText(profileabout);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(getContext(),PersonalChat.class);
                                intent.putExtra("profilename",profilename);
                                intent.putExtra("profileabout",profileabout);
                                intent.putExtra("profileimage",profilepic);
                                intent.putExtra("profileid",profileid);
                                startActivity(intent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }


            @NonNull
            @Override
            public ViewHolderr onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item,parent,false);
                ViewHolderr holder= new ViewHolderr(v);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening();
    }

    public class ViewHolderr extends RecyclerView.ViewHolder {

        public ImageView pic;
        public ImageView online;
        public TextView name;
        public TextView desc;
        public ViewHolderr(@NonNull View itemView) {
            super(itemView);
            pic=itemView.findViewById(R.id.profile_image_contacts);
            online=itemView.findViewById(R.id.online_contacts);
            name=itemView.findViewById(R.id.name_contacts);
            desc=itemView.findViewById(R.id.last_msg_contacts);
        }
    }
}