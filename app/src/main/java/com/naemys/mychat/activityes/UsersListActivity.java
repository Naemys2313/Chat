package com.naemys.mychat.activityes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.naemys.mychat.R;
import com.naemys.mychat.adapters.UserAdapter;
import com.naemys.mychat.models.Message;
import com.naemys.mychat.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private String userName;
    private boolean isEditUserName;

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference messagesReference;
    private ChildEventListener usersEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference().child("users");
        messagesReference = database.getReference().child("messages");

        users = new ArrayList<>();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        layoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(UsersListActivity.this, users);
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                User recipientUser = users.get(position);

                Intent chatIntent = new Intent(UsersListActivity.this,
                        ChatActivity.class);
                chatIntent.putExtra("recipientId", recipientUser.getId());
                chatIntent.putExtra("recipientUserName", recipientUser.getUserName());

                startActivity(chatIntent);
            }
        });

        userAdapter.setHasStableIds(false);
        usersRecyclerView.setAdapter(userAdapter);

        usersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);

                if(!user.getId().equals(auth.getCurrentUser().getUid())) {
                    users.add(user);

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User changedUser = dataSnapshot.getValue(User.class);

                for(int i = 0; i < users.size(); i++) {
                    if(users.get(i).getId().equals(changedUser.getId())) {
                        users.set(i, changedUser);

                        userAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                Log.d("onChildChanged: ", "Changed");

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

        usersReference.addChildEventListener(usersEventListener);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOutMenuItem:
                auth.signOut();

                startActivity(new Intent(UsersListActivity.this,
                        SignInUpActivity.class));

                finish();
                return true;

            case R.id.setUserName:
                final View userNameDialogView = LayoutInflater.from(UsersListActivity.this)
                        .inflate(R.layout.user_name_dialog, null);

                AlertDialog.Builder userNameDialogBuilder = new AlertDialog
                        .Builder(UsersListActivity.this);
                userNameDialogBuilder.setView(userNameDialogView);

                userNameDialogBuilder.setPositiveButton("Save user name", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText userNameEditText = userNameDialogView
                                .findViewById(R.id.userNameEditText);

                        userName = userNameEditText.getText().toString().trim();


                        isEditUserName = true;
                        usersReference.child(auth.getCurrentUser().getUid())
                                .child("userName")
                                .setValue(userName);

                        messagesReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(isEditUserName) {
                                    for(DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                        Message message = messageSnapshot.getValue(Message.class);
                                        if(message.getSenderId().equals(auth.getCurrentUser().getUid())) {
                                            messagesReference.child(messageSnapshot.getKey())
                                                    .child("userName").setValue(userName);
                                        }
                                    }
                                    Log.d("onDataChanged: ", "here");
                                }

                                isEditUserName = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog userNameDialog = userNameDialogBuilder.create();
                userNameDialog.show();

                return true;

            case R.id.setAvatar:

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }
}
