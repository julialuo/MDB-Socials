package com.juliazluo.www.mdbsocials;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String CLASS_NAME = "DetailsActivity";
    private DatabaseReference detailsRef, socialsListRef;
    private StorageReference storageRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private TextView name, email, date, description;
    private Button numInterestedBtn, interestedBtn;
    private ImageView imageView;
    private String id, displayName;
    private Uri profileUri;
    private long numInterested;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        detailsRef = FirebaseDatabase.getInstance().getReference("/socialDetails");
        socialsListRef = FirebaseDatabase.getInstance().getReference("/socialsList");
        storageRef = FirebaseStorage.getInstance().getReference();
        name = (TextView) findViewById(R.id.name_detail);
        email = (TextView) findViewById(R.id.email_detail);
        date = (TextView) findViewById(R.id.date_detail);
        description = (TextView) findViewById(R.id.description_detail);
        numInterestedBtn = (Button) findViewById(R.id.num_interested_btn);
        interestedBtn = (Button) findViewById(R.id.interested_btn);
        imageView = (ImageView) findViewById(R.id.image_detail);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    firebaseUser = user;
                    displayName = user.getDisplayName();
                    profileUri = user.getPhotoUrl();

                    for (UserInfo userInfo : user.getProviderData()) {
                        if (displayName == null && userInfo.getDisplayName() != null) {
                            displayName = userInfo.getDisplayName();
                        }
                        if (profileUri == null && userInfo.getPhotoUrl() != null) {
                            profileUri = userInfo.getPhotoUrl();
                        }
                    }
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };

        numInterestedBtn.setOnClickListener(this);
        interestedBtn.setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        firebaseUser = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Intent intent = getIntent();
        id = intent.getStringExtra("SOCIAL_ID");
        String imageName = intent.getStringExtra("IMAGE_NAME");
        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child(id).child("name").getValue(String.class));
                email.setText("Host: " + dataSnapshot.child(id).child("email").getValue(String.class));
                description.setText(dataSnapshot.child(id).child("description").getValue(String.class));
                date.setText("Date: " + dataSnapshot.child(id).child("date").getValue(String.class));
                numInterested = dataSnapshot.child(id).child("numRSVP").getValue(Long.class);
                numInterestedBtn.setText(numInterested + " Interested");

                if (!dataSnapshot.child(id).child("usersRSVP").hasChild(firebaseUser.getUid())) {
                    interestedBtn.setText("Interested");
                } else {
                    interestedBtn.setText("Not interested");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(CLASS_NAME, "Failed to read value.", error.toException());
            }
        });

        storageRef.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri)
                        .override(300, 300)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(CLASS_NAME, "Couldn't find file");
            }
        });
    }

    private void incrementInterested() {
        if (firebaseUser != null) {
            detailsRef.child(id).child("usersRSVP").child(firebaseUser.getUid()).child("userName").setValue(displayName);
            detailsRef.child(id).child("usersRSVP").child(firebaseUser.getUid()).child("userImage").setValue(profileUri.toString());
        }
        numInterested += 1;
        detailsRef.child(id).child("numRSVP").setValue(numInterested);
        socialsListRef.child(id).child("numRSVP").setValue(numInterested);
        numInterestedBtn.setText(numInterested + " Interested");
        interestedBtn.setText("Not interested");
    }

    private void decrementInterested() {
        if (firebaseUser != null) {
            detailsRef.child(id).child("usersRSVP").child(firebaseUser.getUid()).removeValue();
        }
        numInterested -= 1;
        detailsRef.child(id).child("numRSVP").setValue(numInterested);
        socialsListRef.child(id).child("numRSVP").setValue(numInterested);
        numInterestedBtn.setText(numInterested + " Interested");
        interestedBtn.setText("Interested");
    }

    private void showPopup() {
        final View popupView = LayoutInflater.from(this).inflate(R.layout.interested_popup, null);
        popupWindow = new PopupWindow(popupView, 1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.interested_recycler);
        final ArrayList<User> users = new ArrayList<>();
        final PopupAdapter adapter = new PopupAdapter(getApplicationContext(), users);

        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView textView = (TextView) popupView.findViewById(R.id.popup_text);
                if (dataSnapshot.child(id).child("usersRSVP").hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.child(id).child("usersRSVP").getChildren()) {
                        String userName = snapshot.child("userName").getValue(String.class);
                        String imageURI = snapshot.child("userImage").getValue(String.class);
                        User newUser = new User(userName, imageURI);
                        users.add(newUser);
                    }
                    adapter.notifyDataSetChanged();
                    textView.setText("");
                    textView.setHeight(0);
                } else {
                    ((TextView) popupView.findViewById(R.id.popup_text)).setText("No users interested yet");
                    ((TextView) popupView.findViewById(R.id.popup_text)).setHeight(120);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(CLASS_NAME , "Failed to read value.", error.toException());
            }
        });

        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        ((Button) popupView.findViewById(R.id.popup_exit_btn)).setOnClickListener(this);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.num_interested_btn:
                showPopup();
                break;
            case R.id.interested_btn:
                if (interestedBtn.getText().toString() == "Interested") {
                    incrementInterested();
                } else {
                    decrementInterested();
                }
                break;
            case R.id.popup_exit_btn:
                popupWindow.dismiss();
                break;

        }
    }
}
