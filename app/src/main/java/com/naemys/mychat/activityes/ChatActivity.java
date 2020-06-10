package com.naemys.mychat.activityes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.naemys.mychat.R;
import com.naemys.mychat.adapters.MessageAdapter;
import com.naemys.mychat.models.Message;
import com.naemys.mychat.models.User;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final int CHOOSE_IMAGE_RC = 0;

    private String userName;
    private String recipientId;
    private String recipientUserName;

    private ImageButton sendMessageImageButton,
            sendPhotoImageButton;
    private EditText messageEditText;

    private RecyclerView messagesRecyclerView;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private RecyclerView.LayoutManager messageLayoutManager;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference messageReference,
            usersReference;
    private ChildEventListener messagesEventListener,
            usersEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        if (intent != null) {
            recipientId = intent.getStringExtra("recipientId");
            recipientUserName = intent.getStringExtra("recipientUserName");
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        messageReference = database.getReference().child("messages");
        usersReference = database.getReference().child("users");

        storage = FirebaseStorage.getInstance();
        chatImagesReference = storage.getReference().child("chat_images");

        sendMessageImageButton = findViewById(R.id.sendMessageImageButton);
        sendPhotoImageButton = findViewById(R.id.sendPhotoImageButton);
        messageEditText = findViewById(R.id.messageEditText);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        messages = new ArrayList<>();

        messageAdapter = new MessageAdapter(this, messages);
        messageAdapter.setHasStableIds(false);

        messageLayoutManager = new LinearLayoutManager(this);

        messagesRecyclerView.setLayoutManager(messageLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    sendMessageImageButton.setEnabled(false);
                    sendMessageImageButton.setBackgroundResource(R.drawable.ic_send_dark_black_24dp);
                } else {
                    sendMessageImageButton.setEnabled(true);
                    sendMessageImageButton.setBackgroundResource(R.drawable.ic_send_blue_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendMessageImageButton.isEnabled()) {
                    Message message = new Message(messageEditText.getText().toString().trim(),
                            null,
                            userName,
                            auth.getCurrentUser().getUid(),
                            recipientId);

                    messageReference.push().setValue(message);
                }

                messageEditText.setText("");
            }
        });

        sendPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
                chooseImage.setType("image/*");
                chooseImage.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                startActivityForResult(Intent.createChooser(chooseImage, "Choose an image")
                        , CHOOSE_IMAGE_RC);
            }
        });

        messagesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);

                if (message.getSenderId().equals(auth.getCurrentUser().getUid())
                        && message.getRecipientId().equals(recipientId)
                        || (message.getSenderId().equals(recipientId)
                        && message.getRecipientId().equals(auth.getCurrentUser().getUid()))) {
                    messages.add(message);
                    messageAdapter.notifyDataSetChanged();

                    messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message removedMessage = dataSnapshot.getValue(Message.class);
                messages.remove(removedMessage);

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        usersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);

                if (user.getId().equals(auth.getCurrentUser().getUid())) {
                    userName = user.getUserName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        messageReference.addChildEventListener(messagesEventListener);
        usersReference.addChildEventListener(usersEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOutMenuItem) {
            auth.signOut();

            Intent signIntent = new Intent(ChatActivity.this,
                    SignInUpActivity.class);

            startActivity(signIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE_RC && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference imageStorageReference =
                    chatImagesReference.child(imageUri.getLastPathSegment());

            UploadTask uploadTask = imageStorageReference.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return imageStorageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Message message = new Message(null,
                                downloadUri.toString(),
                                userName,
                                auth.getCurrentUser().getUid(),
                                recipientId);

                        messageReference.push().setValue(message);
                    } else {
                        Toast.makeText(ChatActivity.this,
                                "An error occurred while sending the image",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
