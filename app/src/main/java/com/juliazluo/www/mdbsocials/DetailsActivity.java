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

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

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
    private int numInterested;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Initiate activity components
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
        numInterestedBtn.setOnClickListener(this);
        interestedBtn.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        //Initiate user authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    firebaseUser = user;

                    //Retrieve the current user's name and profile image URI
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

        //Retrieve the id and image name of the social
        Intent intent = getIntent();
        id = intent.getStringExtra(FeedAdapter.SOCIAL_ID);
        String imageName = intent.getStringExtra(FeedAdapter.IMAGE_NAME);

        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Retrieve the detailed social from database and update activity components based on the information
                DetailedSocial social = dataSnapshot.child(id).child("social").getValue(DetailedSocial.class);
                name.setText(social.getName());
                email.setText("Host: " + social.getEmail());
                description.setText(social.getDescription());
                date.setText("Date: " + social.getDate());
                numInterested = social.getNumRSVP();
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

        //Load the social image into image view
        Utils.loadImage(CLASS_NAME, imageName, this, imageView, 300);
    }

    /**
     * Increment the number of users interested in the social
     */
    private void incrementInterested() {
        if (firebaseUser != null) {
            //Add user to RSVP list in database
            User user = new User(displayName, profileUri.toString());
            detailsRef.child(id).child("usersRSVP").child(firebaseUser.getUid()).setValue(user);
        }

        //Increment the number of interested users
        numInterested += 1;
        updateInterested();
        interestedBtn.setText("Not interested");
    }

    /**
     * Decrement the number of users interested in the social
     */
    private void decrementInterested() {
        if (firebaseUser != null) {
            //Remove user from RSVP list
            detailsRef.child(id).child("usersRSVP").child(firebaseUser.getUid()).removeValue();
        }

        //Decrement the number of interested users
        numInterested -= 1;
        updateInterested();
        interestedBtn.setText("Interested");
    }

    /**
     * Update Firebase database and button to reflect change in number of interested users
     */
    private void updateInterested() {
        detailsRef.child(id).child("social").child("numRSVP").setValue(numInterested);
        socialsListRef.child(id).child("social").child("numRSVP").setValue(numInterested);
        numInterestedBtn.setText(numInterested + " Interested");
    }

    /**
     * Display the popup with recycler view of interested users
     */
    private void showPopup() {
        //Initialize popup and recyclerview for the popup
        final View popupView = LayoutInflater.from(this).inflate(R.layout.interested_popup, null);
        popupWindow = new PopupWindow(popupView, 1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.interested_recycler);
        final ArrayList<User> users = new ArrayList<>();
        final PopupAdapter adapter = new PopupAdapter(this, users);

        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView textView = (TextView) popupView.findViewById(R.id.popup_text);

                if (dataSnapshot.child(id).child("usersRSVP").hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.child(id).child("usersRSVP").getChildren()) {
                        //Add user to the list of interested users
                        User user = snapshot.getValue(User.class);
                        users.add(user);
                    }
                    adapter.notifyDataSetChanged();

                    //Effectively remove the textView meant for no users interested
                    textView.setText("");
                    textView.setHeight(0);
                } else {
                    //Display that no users are interested
                    ((TextView) popupView.findViewById(R.id.popup_text)).setText("No users interested yet");
                    ((TextView) popupView.findViewById(R.id.popup_text)).setHeight(120);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i(CLASS_NAME, "Failed to read value.", error.toException());
            }
        });

        //Finalize adapter
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        //Finalize and display popup
        ((Button) popupView.findViewById(R.id.popup_exit_btn)).setOnClickListener(this);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.num_interested_btn:
                showPopup();
                break;
            case R.id.interested_btn:
                //Check if user clicked "Interested" or "Not interested" and act accordingly
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
