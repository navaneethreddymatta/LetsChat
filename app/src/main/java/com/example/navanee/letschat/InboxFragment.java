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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InboxFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    Context mContext;
    FirebaseUser lUser;
    DatabaseReference mDatabase, mRef;
    FirebaseAuth mAuth;
    ListView inboxMessages;
    RelativeLayout hintMessageView;
    ArrayList<ChatMessage> messagesList = new ArrayList<ChatMessage>();
    ArrayList<InboxSummary> inboxList = new ArrayList<InboxSummary>();
    ImageView newMsgBtn;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    //HashMap<String,ArrayList<ChatMessage>> messagesHashMap = new HashMap<String,ArrayList<ChatMessage>> ();

    public InboxFragment() {
        Log.d("test","inbox fragment created");
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
        return inflater.inflate(R.layout.fragment_inbox, container, false);
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        lUser = mAuth.getCurrentUser();
        inboxMessages = (ListView) getActivity().findViewById(R.id.inboxListView);
        hintMessageView = (RelativeLayout) getActivity().findViewById(R.id.noMessagesView);
        newMsgBtn = (ImageView) getActivity().findViewById(R.id.newMsgBtn);
        mRef = mDatabase.child("users").child(lUser.getUid()).child("messages");
        getMessagesList();
        mListener = (OnFragmentInteractionListener) mContext;
        newMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addNewMessage();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void getMessagesList(){
        if(mRef == null) {
            inboxMessages.setVisibility(View.GONE);
            hintMessageView.setVisibility(View.VISIBLE);
        } else {
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    messagesList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ChatMessage msg = ds.getValue(ChatMessage.class);
                        messagesList.add(msg);
                    }
                    Log.d("test", "messages-------" + String.valueOf(messagesList.size()));
                    if(messagesList.size() == 0) {
                        inboxMessages.setVisibility(View.GONE);
                        hintMessageView.setVisibility(View.VISIBLE);
                    } else {
                        hintMessageView.setVisibility(View.GONE);
                        inboxMessages.setVisibility(View.VISIBLE);
                        generateInboxArray();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void generateInboxArray() {
        inboxList.clear();
        for(ChatMessage cmsg : messagesList) {
            String key = "";
            Log.d("demo","---"+cmsg.toString());
            Log.d("demo",cmsg.getReceiverID());
            Log.d("demo","========"+lUser.getUid());
            if(!cmsg.getReceiverID().equals(lUser.getUid())) {
                key = cmsg.getReceiverID();
            } else {
                key = cmsg.getSenderID();
            }
            int msgIndex = getMessageIndex(key);
            if(msgIndex == -1) {
                InboxSummary is = new InboxSummary();
                is.setUserID(key);
                is.setpDate(cmsg.getDate());
                String content = cmsg.getContent() != null ? cmsg.getContent() : "Image";
                is.setLastMessage(content);
                int numMsgs = cmsg.isRead() ? 0 : 1;
                is.setNumUnreadMessages(numMsgs);
                inboxList.add(is);
            } else {
                InboxSummary is = inboxList.get(msgIndex);
                String content = cmsg.getContent() != null ? cmsg.getContent() : "Image";
                is.setpDate(cmsg.getDate());
                is.setLastMessage(content);
                int numMsgs = is.getNumUnreadMessages();
                if(!cmsg.isRead())
                    is.setNumUnreadMessages(++numMsgs);
            }
        }
        Log.d("test","inbox-------" + String.valueOf(inboxList.size()));
        generateInboxTable();
    }

    public int getMessageIndex(String key) {
        int index = -1;
        for(int i = 0; i < inboxList.size(); i++) {
            InboxSummary chtMsg = inboxList.get(i);
            if(chtMsg.getUserID().equals(key)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void generateInboxTable() {
        Collections.sort(inboxList, new Comparator<InboxSummary>() {
            @Override
            public int compare(InboxSummary msg1, InboxSummary msg2) {
                try {
                    if(dateFormat.parse(msg1.getpDate()).before(dateFormat.parse(msg2.getpDate())))
                        return -1;
                    else if(dateFormat.parse(msg1.getpDate()).after(dateFormat.parse(msg2.getpDate())))
                        return 1;
                    else
                        return 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        InboxAdapter adapter = new InboxAdapter(mContext, R.layout.inbox_row_layout,inboxList);
        inboxMessages.setAdapter(adapter);
        inboxMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InboxSummary ibSummary = inboxList.get(i);
                Intent intent = new Intent(mContext,UserMessagesActivity.class);
                intent.putExtra("userID", ibSummary.getUserID());
                startActivity(intent);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        ArrayList<User> onInboxFragmentInteraction();
        void addNewMessage();
    }
}

