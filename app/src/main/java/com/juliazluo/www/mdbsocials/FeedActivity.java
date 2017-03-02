package com.juliazluo.www.mdbsocials;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/*
ASK:
Asynctask
Implementing onClickListener
Is having feed activity as main activity okay?
 */
public class FeedActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "FeedActivity";
    private ArrayList<Social> socials;
    private FeedAdapter adapter;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/socialsList");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private HashMap<String, Social> socialHashMap;
    private static FirebaseAuth mAuth;
    private boolean rememberMe;
    protected static boolean leavingApp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };

        socials = new ArrayList<>();
        socialHashMap = new HashMap<>();
        adapter = new FeedAdapter(getApplicationContext(), socials);
        RecyclerView recyclerAdapter = (RecyclerView)findViewById(R.id.feed_recycler);
        recyclerAdapter.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter.setAdapter(adapter);

        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                if (snapshot.child("name") != null && snapshot.child("email") != null &&
                        snapshot.child("numRSVP") != null && snapshot.child("imageName") != null) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    long numRSVP = snapshot.child("numRSVP").getValue(Long.class);
                    String imageName = snapshot.child("imageName").getValue(String.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    Social social = new Social(id, name, email, numRSVP, imageName, timestamp);
                    socialHashMap.put(id, social);
                    socials.add(social);
                    Collections.sort(socials);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                String id = snapshot.getKey();
                long numRSVP = snapshot.child("numRSVP").getValue(Long.class);
                Social changedSocial = socialHashMap.get(id);
                changedSocial.setNumRSVP(numRSVP);
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

        /*ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                socials.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (snapshot.child("name") != null && snapshot.child("email") != null &&
                            snapshot.child("numRSVP") != null && snapshot.child("imageName") != null) {
                        String id = snapshot.getKey();
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        long numRSVP = snapshot.child("numRSVP").getValue(Long.class);
                        String imageName = snapshot.child("imageName").getValue(String.class);
                        Social social = new Social(id, name, email, numRSVP, imageName);
                        socials.add(social);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Database", "Failed to read value.", error.toException());
            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Intent intent = getIntent();
        rememberMe = intent.getBooleanExtra(LoginActivity.REMEMBER_ME, rememberMe);
        leavingApp = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (leavingApp && !rememberMe) {
            mAuth.signOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                mAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
