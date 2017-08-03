package com.example.navanee.letschat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by navanee on 18-11-2016.
 */

public class UserAdapter extends ArrayAdapter<User> {

    Context mContext;
    int mResource;
    List<User> mObjects;

    public UserAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
            holder = new ViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImage);
            holder.userNameView = (TextView) convertView.findViewById(R.id.userName);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        User usr = mObjects.get(position);
        holder.userNameView.setText(usr.getFirstName() + " " + usr.getLastName());
        if(usr.getProfileURL() != null && !usr.getProfileURL().isEmpty()) {
            Picasso.with(mContext).load(usr.getProfileURL()).into(holder.userImageView);
        }
        return convertView;
    }
}

class ViewHolder {
    TextView userNameView;
    ImageView userImageView;
}