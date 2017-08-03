package com.example.navanee.letschat;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class ProfileEditFragment extends Fragment {

    private ProfileFragment.OnFragmentInteractionListener mListener;
    User user;
    DatabaseReference mDatabase;
    TextView pFirstName, pLastName, pEmail, genderLabel, changePhoto;
    Button pSaveBtn, pCancelBtn;
    ImageView pImageView;
    RadioGroup genderGroup;
    int gIndex = -1;
    Context mContext;
    private static final int GALLERY_INTENT = 2;
    ProgressDialog pDialog;
    StorageReference mStorage;

    public ProfileEditFragment() {
        // Required empty public constructor
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.OnFragmentInteractionListener) {
            mListener = (ProfileFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        pFirstName = (TextView) getActivity().findViewById(R.id.pFirstName);
        pLastName = (TextView) getActivity().findViewById(R.id.pLastName);
        //pGender = (TextView) getActivity().findViewById(R.id.pGender);
        pEmail = (TextView) getActivity().findViewById(R.id.pEmail);
        pSaveBtn = (Button) getActivity().findViewById(R.id.pSaveBtn);
        pCancelBtn = (Button) getActivity().findViewById(R.id.pCancel);
        genderLabel = (TextView) getActivity().findViewById(R.id.genderLabel);
        pImageView = (ImageView) getActivity().findViewById(R.id.pImage);
        genderGroup = (RadioGroup) getActivity().findViewById(R.id.genderGroup);
        changePhoto = (TextView) getActivity().findViewById(R.id.editPhotoBtn);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        pFirstName.setText(user.getFirstName());
        pLastName.setText(user.getLastName());
        if(user.getGender() != null) {
            if(user.getGender().equals("Male")) {
                gIndex = 0;
            } else if(user.getGender().equals("Female")) {
                gIndex = 1;
            }
            //genderGroup.check(gIndex == 0 ? R.id.radioButton2 : R.id.radioButton);
            RadioButton rButton = (RadioButton) genderGroup.getChildAt(gIndex);
            rButton.setChecked(true);
        }
        pEmail.setText(user.getEmail());
        if(user.getProfileURL() != null && !user.getProfileURL().isEmpty()) {
            Picasso.with(mContext).load(user.getProfileURL()).into(pImageView);
        }

        pSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String firstName = pFirstName.getText().toString();
                final String lastName = pLastName.getText().toString();

                final int genIndex = genderGroup.indexOfChild(getActivity().findViewById(genderGroup.getCheckedRadioButtonId()));
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(mContext, R.string.toast_signup_empty_values, Toast.LENGTH_LONG).show();
                } else if (genIndex == -1) {
                    Toast.makeText(mContext, "Select the gender", Toast.LENGTH_LONG).show();
                } else {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    if (genIndex == 0) {
                        user.setGender("Male");
                    } else {
                        user.setGender("Female");
                    }
                    mDatabase.child("users").child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(getFragmentManager().getBackStackEntryCount() > 0)
                                getFragmentManager().popBackStack();
                        }
                    });
                }
            }
        });

        pCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().getBackStackEntryCount() > 0)
                    getFragmentManager().popBackStack();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == getActivity().RESULT_OK) {
            pDialog = new ProgressDialog(mContext);
            pDialog.setTitle("Uploading");
            pDialog.show();
            Uri uri = data.getData();
            Log.d("uri",uri.toString());
            StorageReference filePath = mStorage.child("photos").child(uri.getLastPathSegment());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.cancel();
                    Toast.makeText(mContext,"Image Uploaded",Toast.LENGTH_LONG).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d("uri",downloadUrl.toString());
                    String imgName = taskSnapshot.getMetadata().getName();
                    Picasso.with(mContext).load(downloadUrl).into(pImageView);
                    mDatabase.child("users").child(user.getId()).child("profileURL").setValue(downloadUrl.toString());
                    user.setProfileURL(downloadUrl.toString());
                }
            });
        }
    }
}
