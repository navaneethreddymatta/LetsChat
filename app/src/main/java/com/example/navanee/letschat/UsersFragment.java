package com.example.navanee.letschat;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    DatabaseReference mRef;
    ArrayList<User> users = new ArrayList<User>();
    ListView usersListView;
    Context mContext;
    String reason, loggedUserID;


    public UsersFragment() {
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        loggedUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersListView = (ListView) getActivity().findViewById(R.id.usersListView);
        Log.d("demo","users page start");
        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if(!(reason.equals("userMessages") && user.getId().equals(loggedUserID))) {
                        users.add(user);
                    }
                }
                showUsersTable();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void showUsersTable() {
        UserAdapter adapter = new UserAdapter(mContext,R.layout.user_row_layout,users);
        usersListView.setAdapter(adapter);
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(reason.equals("userProfile")) {
                    Intent intent = new Intent(mContext, UserDetailsActivity.class);
                    intent.putExtra("uid", users.get(position).getId());
                    startActivity(intent);
                } else if(reason.equals("userMessages")) {
                    Intent intent = new Intent(mContext, UserMessagesActivity.class);
                    intent.putExtra("userID", users.get(position).getId());
                    startActivity(intent);
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        ArrayList<User> onUsersFragmentInteraction();
    }
}