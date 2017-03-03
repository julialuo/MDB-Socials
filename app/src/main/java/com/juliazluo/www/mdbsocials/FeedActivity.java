package com.juliazluo.www.mdbsocials;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FeedActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "FeedActivity";
    private ArrayList<Social> socials;
    private FeedAdapter adapter;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/socialsList");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private HashMap<String, Social> socialHashMap;
    private static FirebaseAuth mAuth;
    private boolean rememberMe; //Whether to log user out on exit
    protected static boolean leavingApp = true; //Whether user is leaving app or just this screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //Initiate activity components
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewSocialActivity.class);
                leavingApp = false;
                startActivity(intent);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        socials = new ArrayList<>();
        socialHashMap = new HashMap<>();
        adapter = new FeedAdapter(this, socials);
        RecyclerView recyclerAdapter = (RecyclerView) findViewById(R.id.feed_recycler);
        recyclerAdapter.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter.setAdapter(adapter);

        //Initiate user authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // If user is signed out, go back to login screen
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };

        //Listen for changes to the children of the socials list node
        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                if (snapshot.child("social") != null && snapshot.child("timestamp").getValue() != null) {
                    //Retrieve social from database and display on recycler view
                    String id = snapshot.getKey();
                    Social social = snapshot.child("social").getValue(Social.class);
                    socials.add(social);
                    Collections.sort(socials);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                //Retrieve number of interested users and timestamp from database
                String id = snapshot.getKey();
                int numRSVP = (int) (long) snapshot.child("social").child("numRSVP").getValue(Long.class);
                long timestamp = snapshot.child("timestamp").getValue(Long.class);
                Social social;

                if (socialHashMap.containsKey(id)) {
                    //Altering existing social
                    social = socialHashMap.get(id);
                } else {
                    //New social added
                    social = snapshot.child("social").getValue(Social.class);
                    socialHashMap.put(id, social);
                }

                //Update the changed social's number of interested users and timestamp
                social.setTimestamp(timestamp);
                social.setNumRSVP(numRSVP);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Intent intent = getIntent();

        //Change remember me to reflect what was checked in login activity, if applicable
        rememberMe = intent.getBooleanExtra(LoginActivity.REMEMBER_ME, rememberMe);
        leavingApp = true; //Default as true
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        //If user does not want to be remembered and user is leaving app
        if (leavingApp && !rememberMe) {
            mAuth.signOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Initiate the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                //If user clicked log out
                mAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
