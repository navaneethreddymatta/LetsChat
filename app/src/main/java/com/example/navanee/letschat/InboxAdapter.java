package com.example.navanee.letschat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by navanee on 18-11-2016.
 */

public class InboxAdapter extends ArrayAdapter<InboxSummary>{

    Context mContext;
    int mResource;
    List<InboxSummary> mObjects;
    DatabaseReference mDatabase;

    public InboxAdapter(Context context, int resource, List<InboxSummary> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final InboxViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
            holder = new InboxViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImageField);
            holder.userNameView = (TextView) convertView.findViewById(R.id.userNameField);
            holder.userMessageView = (TextView) convertView.findViewById(R.id.userMessageField);
            holder.unreadMessageView = (TextView) convertView.findViewById(R.id.unreadCount);
            convertView.setTag(holder);
        }
        else {
            holder = (InboxViewHolder) convertView.getTag();
        }
        InboxSummary ibSum = mObjects.get(position);
        holder.userMessageView.setText(ibSum.getLastMessage());
        if(ibSum.getNumUnreadMessages() == 0) {
            holder.unreadMessageView.setVisibility(View.GONE);
        } else {
            holder.unreadMessageView.setVisibility(View.VISIBLE);
            holder.unreadMessageView.setText(String.valueOf(ibSum.getNumUnreadMessages()));
        }
        mDatabase.child("users").child(ibSum.getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User cUser = dataSnapshot.getValue(User.class);
                holder.userNameView.setText(cUser.getFirstName() + " " + cUser.getLastName());
                if(cUser.getProfileURL() != null && !cUser.getProfileURL().isEmpty()) {
                    Picasso.with(mContext).load(cUser.getProfileURL()).into(holder.userImageView);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return convertView;
    }
}

class InboxViewHolder {
    TextView userNameView, userMessageView, unreadMessageView;
    ImageView userImageView;
}