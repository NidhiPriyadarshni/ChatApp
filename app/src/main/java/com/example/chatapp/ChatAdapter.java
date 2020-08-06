package com.example.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Viewholder> {
    FirebaseAuth auth;
    String userid;
    List<Chatmsg> chatlist;
    public ChatAdapter(List<Chatmsg> chatlist){
        this.chatlist=chatlist;
        auth=FirebaseAuth.getInstance();
        userid=auth.getCurrentUser().getUid();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        RelativeLayout receivebox,sendbox,imgreceivebox,imgsendbox,docreceivebox,docsendbox;
        TextView receivemsg,receivemsgtime,sendmsg,sendmsgtime,imgreceivemsgtime,imgsendmsgtime,docreceivemsg,docreceivemsgtime,docsendmsg,docsendmsgtime;
        ImageView imgreceive,imgsend;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            receivebox=itemView.findViewById(R.id.chat_box_receive);
            sendbox=itemView.findViewById(R.id.chat_box_send);
            receivemsg=itemView.findViewById(R.id.chat_box_receive_msg);
            receivemsgtime=itemView.findViewById(R.id.chat_box_receive_msg_time);
            sendmsg=itemView.findViewById(R.id.chat_box_send_msg);
            sendmsgtime=itemView.findViewById(R.id.chat_box_send_msg_time);
            imgreceivebox=itemView.findViewById(R.id.image_box_receive);
            imgsendbox=itemView.findViewById(R.id.image_box_send);
            imgreceive=itemView.findViewById(R.id.image_box_receive_msg);
            imgreceivemsgtime=itemView.findViewById(R.id.image_box_receive_msg_time);
            imgsend=itemView.findViewById(R.id.image_box_send_msg);
            imgsendmsgtime=itemView.findViewById(R.id.image_box_send_msg_time);
            docreceivebox=itemView.findViewById(R.id.doc_box_receive);
            docsendbox=itemView.findViewById(R.id.doc_box_send);
            docreceivemsg=itemView.findViewById(R.id.doc_box_receive_msg);
            docreceivemsgtime=itemView.findViewById(R.id.doc_box_receive_msg_time);
            docsendmsg=itemView.findViewById(R.id.doc_box_send_msg);
            docsendmsgtime=itemView.findViewById(R.id.doc_box_send_msg_time);

        }
    }

    @NonNull
    @Override
    public ChatAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_box,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.Viewholder holder, int position) {
            final Chatmsg chatmsg=chatlist.get(position);
            holder.receivebox.setVisibility(View.GONE);
            holder.sendbox.setVisibility(View.GONE);
            holder.imgreceivebox.setVisibility(View.GONE);
            holder.imgsendbox.setVisibility(View.GONE);
            holder.docreceivebox.setVisibility(View.GONE);
            holder.docsendbox.setVisibility(View.GONE);
            String type=chatmsg.getType();
            if(chatmsg.getSender().contentEquals(userid)){

                if(type.equals("text")||type.equals("")){
                    holder.sendbox.setVisibility(View.VISIBLE);
                    holder.sendmsg.setText(chatmsg.getMessage());
                    holder.sendmsgtime.setText(chatmsg.getTime());
                }else if(type.equals("picture")){
                    holder.imgsendbox.setVisibility(View.VISIBLE);
                    Picasso.get().load(chatmsg.getMessage()).into(holder.imgsend);
                    holder.imgsendmsgtime.setText(chatmsg.getTime());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chatmsg.getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }else {
                    holder.docsendbox.setVisibility(View.VISIBLE);
                    holder.docsendmsg.setText(chatmsg.getName());
                    holder.docsendmsgtime.setText(chatmsg.getTime());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chatmsg.getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }
            }else {
                if(type.equals("text")||type.equals("")){
                    holder.receivebox.setVisibility(View.VISIBLE);
                    holder.receivemsg.setText(chatmsg.getMessage());
                    holder.receivemsgtime.setText(chatmsg.getTime());
                }else if(type.equals("picture")){
                    holder.imgreceivebox.setVisibility(View.VISIBLE);
                    Picasso.get().load(chatmsg.getMessage()).into(holder.imgreceive);
                    holder.imgreceivemsgtime.setText(chatmsg.getTime());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chatmsg.getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }else{
                    holder.docreceivebox.setVisibility(View.VISIBLE);
                    holder.docreceivemsg.setText(chatmsg.getName());
                    holder.docreceivemsgtime.setText(chatmsg.getTime());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chatmsg.getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                }
            }
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }
}
