package com.example.navanee.letschat;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    OnFragmentInteractionListener mListener;
    Context mContext;
    String userID, loggedInUserID;
    DatabaseReference mDatabase;
    User cUser;
    TextView pFirstName, pLastName, pGender, pEmail, genderLabel;
    Button pEditBtn, pCancelBtn;
    ImageView pImageView;
    FirebaseAuth mAuth;
    String source = "";

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        pFirstName = (TextView) getActivity().findViewById(R.id.pFirstName);
        pLastName = (TextView) getActivity().findViewById(R.id.pLastName);
        pGender = (TextView) getActivity().findViewById(R.id.pGender);
        pEmail = (TextView) getActivity().findViewById(R.id.pEmail);
        pEditBtn = (Button) getActivity().findViewById(R.id.pEditBtn);
        pCancelBtn = (Button) getActivity().findViewById(R.id.pCancel);
        genderLabel = (TextView) getActivity().findViewById(R.id.genderLabel);
        pImageView = (ImageView) getActivity().findViewById(R.id.pImage);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        loggedInUserID = mAuth.getCurrentUser().getUid();
        if(loggedInUserID.equals(userID)) {
            pEditBtn.setVisibility(View.VISIBLE);
        } else {
            pEditBtn.setVisibility(View.GONE);
        }
        if(source.equals("details")) {
            pCancelBtn.setVisibility(View.VISIBLE);
        } else {
            pCancelBtn.setVisibility(View.GONE);
        }
        pCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener = (OnFragmentInteractionListener) mContext;
                mListener.goBackToMain();
            }
        });
        mDatabase.child("users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cUser = dataSnapshot.getValue(User.class);
                setUpUserData(cUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setUpUserData(User user) {
        pFirstName.setText(user.getFirstName());
        pLastName.setText(user.getLastName());
        if(user.getGender() != null) {
            pGender.setText(user.getGender());
        } else {
            pGender.setVisibility(View.GONE);
            genderLabel.setVisibility(View.GONE);
        }
        pEmail.setText(user.getEmail());
        pEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener = (OnFragmentInteractionListener) mContext;
                mListener.editSelectedUserProfile(cUser);
            }
        });
        if(user.getProfileURL() != null && !user.getProfileURL().isEmpty()) {
            Picasso.with(mContext).load(user.getProfileURL()).into(pImageView);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        ArrayList<User> onProfileFragmentInteraction();
        void editSelectedUserProfile(User cUser);
        void goBackToMain();
    }

}
