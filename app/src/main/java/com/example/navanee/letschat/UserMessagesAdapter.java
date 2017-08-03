package com.example.navanee.letschat;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by navanee on 18-11-2016.
 */

public class UserMessagesAdapter extends ArrayAdapter<ChatMessage> {

    Context mContext;
    int mResource;
    List<ChatMessage> mObjects;
    DatabaseReference mDatabase;
    String loggedInUserID;

    public UserMessagesAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        loggedInUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MessageViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
            holder = new MessageViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.messageImageView);
            holder.unreadCheckView = (RelativeLayout) convertView.findViewById(R.id.unreadCheckView);
            holder.messageLanding = (RelativeLayout) convertView.findViewById(R.id.messageLanding);
            holder.userMessageView = (TextView) convertView.findViewById(R.id.messageTextView);
            convertView.setTag(holder);
        }
        else {
            holder = (MessageViewHolder) convertView.getTag();
        }
        final ChatMessage chtMsg = mObjects.get(position);
        if(chtMsg.getContent() != null && !chtMsg.getContent().isEmpty()) {
            holder.userMessageView.setVisibility(View.VISIBLE);
            holder.userImageView.setVisibility(View.GONE);
            holder.userMessageView.setText(chtMsg.getContent());
            if(loggedInUserID.equals(chtMsg.getSenderID())) {
                holder.userMessageView.setGravity(Gravity.RIGHT);
            }
        } else if(chtMsg.getImageURL() != null) {
            holder.userMessageView.setVisibility(View.GONE);
            holder.userImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(chtMsg.getImageURL()).into(holder.userImageView);
        }

        if(chtMsg.isRead())
            holder.unreadCheckView.setVisibility(View.INVISIBLE);
        else
            holder.unreadCheckView.setVisibility(View.VISIBLE);
       /* RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams)holder.messageLanding.getLayoutParams();
        if(chtMsg.getReceiverID().equals(loggedInUserID)) {
            relativeParams.setMargins(0, 0, 100, 0);  // left, top, right, bottom
        } else {
            relativeParams.setMargins(100, 0, 0, 0);  // left, top, right, bottom
        }
        holder.messageLanding.setLayoutParams(relativeParams);*/

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDatabase.child("users").child(loggedInUserID).child("messages").child(chtMsg.getId()).child("read").setValue(true);
                holder.unreadCheckView.setVisibility(View.INVISIBLE);
            }
        }, 4000);
        return convertView;
    }
}

class MessageViewHolder {
    TextView userMessageView;
    ImageView userImageView;
    RelativeLayout unreadCheckView, messageLanding;
}