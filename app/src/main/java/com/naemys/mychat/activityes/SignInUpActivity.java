package com.naemys.mychat.activityes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.naemys.mychat.R;
import com.naemys.mychat.models.User;

public class SignInUpActivity extends AppCompatActivity {

    private static final String AUTH_TAG = "auth";

    private EditText userNameEditText,
            emailEditText,
            passwordEditText,
            repeatPasswordEditText;
    private Button signButton;
    private TextView toggleSignTextView;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;

    private boolean isSignUp = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_up);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference().child("users");

        userNameEditText = findViewById(R.id.userNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);

        toggleSignTextView = findViewById(R.id.toggleSignTextView);
        toggleSignTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    toggleSignTextView.setText(R.string.click_to_sign_up);

                    userNameEditText.setVisibility(View.GONE);
                    repeatPasswordEditText.setVisibility(View.GONE);
                    signButton.setText(R.string.sign_in);

                    isSignUp = false;
                } else {
                    toggleSignTextView.setText(R.string.click_to_sign_in);

                    userNameEditText.setVisibility(View.VISIBLE);
                    repeatPasswordEditText.setVisibility(View.VISIBLE);
                    signButton.setText(R.string.sign_up);

                    isSignUp = true;
                }
            }
        });

        signButton = findViewById(R.id.signButton);
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    String userName = userNameEditText.getText().toString().trim();
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString();
                    String repeatPassword = repeatPasswordEditText.getText().toString();

                    if (userName.length() < 6) {
                        Toast.makeText(SignInUpActivity.this,
                                "User name can't be less than 6 characters",
                                Toast.LENGTH_LONG).show();
                    } else if (email.isEmpty()) {
                        Toast.makeText(SignInUpActivity.this,
                                "Email can't be empty",
                                Toast.LENGTH_LONG).show();
                    } else if (password.length() < 10) {
                        Toast.makeText(SignInUpActivity.this,
                                "Password can't be less than 10 characters",
                                Toast.LENGTH_LONG).show();
                    } else if (!password.equals(repeatPassword)) {
                        Toast.makeText(SignInUpActivity.this,
                                "Passwords mismatch",
                                Toast.LENGTH_LONG).show();
                    } else {
                        createUser(email, password, userName);
                    }
                } else {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString();

                    signIn(email, password);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(auth.getCurrentUser() != null) {
            goToUsersList();
        }
    }

    private void createUser(String email, String password, final String userName) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(AUTH_TAG, "createUserWithEmail:success");

                            Toast.makeText(SignInUpActivity.this,
                                    "You are successfully sign up",
                                    Toast.LENGTH_LONG).show();

                            FirebaseUser currentUser = auth.getCurrentUser();
                            User user = new User(currentUser.getUid(), userName);

                            goToUsersList();

                            usersReference.child(user.getId()).setValue(user);

                        } else {
                            Log.w(AUTH_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignInUpActivity.this,
                                    "Sign up failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(AUTH_TAG, "signInWithEmail:success");

                            Toast.makeText(SignInUpActivity.this,
                                    "You are successfully sign in",
                                    Toast.LENGTH_LONG).show();

                            goToUsersList();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(AUTH_TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInUpActivity.this,
                                    "Sign in failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToUsersList() {
        Intent usersListIntent = new Intent(SignInUpActivity.this,
                UsersListActivity.class);

        startActivity(usersListIntent);
        finish();
    }
}
