package com.example.navanee.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class UserMessagesActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    ImageView sendMessageBtn, sendImageBtn;
    TextView editMsgText, receiverNameView;
    ListView messagesListView;
    DatabaseReference mDatabase;
    String receiverID, senderID;
    ArrayList<ChatMessage> messagesList = new ArrayList<ChatMessage>();
    FirebaseUser lUser;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final int GALLERY_INTENT = 3;
    ProgressDialog pDialog;
    StorageReference mStorage;
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_messages);
        getSupportActionBar().setLogo(R.drawable.chatsmall);
        loadViews();
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        getMessagesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_signout) {
            /*Log.d("demo",lUser.getProviderData().toString());
            Log.d("demo",lUser.getProviderId());
            Log.d("demo",lUser.getProviders().toString());*/
            mAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {

                }
            });
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public void loadViews() {
        sendMessageBtn = (ImageView) findViewById(R.id.sendMessageBtn);
        sendImageBtn = (ImageView) findViewById(R.id.sendImageBtn);
        editMsgText = (TextView) findViewById(R.id.editMessageText);
        receiverNameView = (TextView) findViewById(R.id.receiverName);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        receiverID = getIntent().getExtras().get("userID").toString();
        lUser = FirebaseAuth.getInstance().getCurrentUser();
        senderID = lUser.getUid();
        mDatabase.child("users").child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User recUser = dataSnapshot.getValue(User.class);
                receiverNameView.setText(recUser.getFirstName() + " " + recUser.getLastName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageContent = editMsgText.getText().toString();
                if(messageContent.isEmpty()) {
                    Toast.makeText(UserMessagesActivity.this,R.string.toast_enter_message,Toast.LENGTH_LONG).show();
                } else {
                    ChatMessage chtMsg = new ChatMessage();
                    chtMsg.setContent(messageContent);
                    chtMsg.setSenderID(senderID);
                    chtMsg.setReceiverID(receiverID);
                    chtMsg.setDate(dateFormat.format(new Date()));
                    chtMsg.setRead(true);
                    String key = mDatabase.child("users").child(senderID).child("messages").push().getKey();
                    chtMsg.setId(key);
                    mDatabase.child("users").child(senderID).child("messages").child(key).setValue(chtMsg);
                    chtMsg.setRead(false);
                    key = mDatabase.child("users").child(receiverID).child("messages").push().getKey();
                    chtMsg.setId(key);
                    mDatabase.child("users").child(receiverID).child("messages").child(key).setValue(chtMsg);
                    editMsgText.setText("");
                }
            }
        });
        sendImageBtn.setOnClickListener(new View.OnClickListener() {
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
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            pDialog = new ProgressDialog(UserMessagesActivity.this);
            pDialog.setTitle("Uploading");
            pDialog.show();
            Uri uri = data.getData();
            StorageReference filePath = mStorage.child("photos").child(uri.getLastPathSegment());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.cancel();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    ChatMessage chtMsg = new ChatMessage();
                    chtMsg.setImageURL(downloadUrl.toString());
                    chtMsg.setSenderID(senderID);
                    chtMsg.setReceiverID(receiverID);
                    chtMsg.setDate(dateFormat.format(new Date()));
                    chtMsg.setRead(true);
                    String key = mDatabase.child("users").child(senderID).child("messages").push().getKey();
                    chtMsg.setId(key);
                    mDatabase.child("users").child(senderID).child("messages").child(key).setValue(chtMsg);
                    chtMsg.setRead(false);
                    key = mDatabase.child("users").child(receiverID).child("messages").push().getKey();
                    chtMsg.setId(key);
                    mDatabase.child("users").child(receiverID).child("messages").child(key).setValue(chtMsg);
                }
            });
        }
    }

    public void getMessagesList(){
        mDatabase.child("users").child(senderID).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    String key = "";
                    if(msg.getReceiverID().equals(receiverID) || msg.getSenderID().equals(receiverID)) {
                        messagesList.add(msg);
                    }
                    if(messagesList.size() > 0)
                        generateInboxTable();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void generateInboxTable() {
        Collections.sort(messagesList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage msg1, ChatMessage msg2) {
                try {
                    if(dateFormat.parse(msg1.getDate()).before(dateFormat.parse(msg2.getDate())))
                        return -1;
                    else if(dateFormat.parse(msg1.getDate()).after(dateFormat.parse(msg2.getDate())))
                        return 1;
                    else
                        return 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        UserMessagesAdapter adapter = new UserMessagesAdapter(UserMessagesActivity.this, R.layout.message_row_layout, messagesList);
        messagesListView.setAdapter(adapter);
        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatMessage msg = messagesList.get(i);
                mDatabase.child("users").child(lUser.getUid()).child("messages").child(msg.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserMessagesActivity.this, "Message deleted", Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
