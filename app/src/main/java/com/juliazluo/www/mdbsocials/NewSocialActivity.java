package com.juliazluo.www.mdbsocials;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class NewSocialActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final String CLASS_NAME = "NewSocialActivity";
    private Uri dataIn;
    private StorageReference storageRef;
    private DatabaseReference socialsListRef, socialDetailsRef;
    private EditText name, description, date;
    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_social);

        //Initiate activity components
        storageRef = FirebaseStorage.getInstance().getReference();
        socialsListRef = FirebaseDatabase.getInstance().getReference("/socialsList");
        socialDetailsRef = FirebaseDatabase.getInstance().getReference("/socialDetails");
        mAuth = FirebaseAuth.getInstance();
        ((Button) findViewById(R.id.add_image_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.add_new_btn)).setOnClickListener(this);
        name = (EditText) findViewById(R.id.name_new);
        description = (EditText) findViewById(R.id.description_new);
        date = (EditText) findViewById(R.id.date_new);
        date.setOnClickListener(this);

        //Initiate user authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("User", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("User", "onAuthStateChanged:signed_out");
                }
            }
        };

        //Initiate listener for when user sets the date
        dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                //Set calendar to store the user's selected date
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
    }

    /**
     * Update the date text field to display the user's chosen date
     */
    private void updateLabel() {
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        date.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        //Initially calendar is set to today's date
        calendar = Calendar.getInstance(TimeZone.getDefault());
        updateLabel();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        //Remove currently selected image, if any
        dataIn = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Store image URI returned by camera or gallery intent
        dataIn = Utils.getImageURI(requestCode, resultCode, data, REQUEST_IMAGE_CAPTURE, RESULT_OK,
                REQUEST_GALLERY);
    }

    /**
     * Attempt to create a new social based on the information inputted by the user
     */
    public void submit() {
        //Retrieve name, description, and date of sical
        final String nameTxt = name.getText().toString();
        final String descTxt = description.getText().toString();
        final String dateTxt = date.getText().toString();

        //Generate random ID in database
        final String id = socialsListRef.push().getKey();
        final String imageName = id + ".png";

        //Create Social and DetailedSocial objects based on inputted information
        final Social newSocial = new Social(id, nameTxt, user.getEmail(), 0, imageName, 0);
        final DetailedSocial newDetailedSocial = new DetailedSocial(id, nameTxt, user.getEmail(),
                imageName, descTxt, dateTxt, 0);

        if (nameTxt.equals("")) {
            Toast.makeText(NewSocialActivity.this, "Social name cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (dataIn != null) {
            //Start upload of chosen image to the Firebase storage
            storageRef.child(id + ".png").putFile(dataIn)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Push data to database
                            pushToDatabase(id, newSocial, newDetailedSocial);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.i(CLASS_NAME, "failed upload " + exception.getMessage());
                            Toast.makeText(NewSocialActivity.this, "Image upload failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //Display progress of upload to user
                            int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred())
                                    / taskSnapshot.getTotalByteCount());
                            Toast.makeText(NewSocialActivity.this, "Photo upload is " + progress + "% done",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //Set image URI to default image and push data to database
            newSocial.setImageName("default_icon.png");
            newDetailedSocial.setImageName("default_icon.png");
            pushToDatabase(id, newSocial, newDetailedSocial);
        }
    }

    /**
     * Stores Social and DetailedSocial objects into Firebase database
     *
     * @param id
     * @param social
     * @param detailedSocial
     */
    private void pushToDatabase(String id, Social social, DetailedSocial detailedSocial) {
        //Push data to socials list ref node
        socialsListRef.child(id).child("social").setValue(social);
        socialsListRef.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP);

        //Push data to social details ref node
        socialDetailsRef.child(id).child("social").setValue(detailedSocial);

        //Return to feed activity
        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_image_btn:
                //Open up image options menu and start either camera or gallery intent
                Utils.selectImageOption(this, REQUEST_GALLERY, REQUEST_IMAGE_CAPTURE, CLASS_NAME);
                break;
            case R.id.add_new_btn:
                submit();
                break;
            case R.id.date_new:
                //Display date picker
                new DatePickerDialog(NewSocialActivity.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }
}
