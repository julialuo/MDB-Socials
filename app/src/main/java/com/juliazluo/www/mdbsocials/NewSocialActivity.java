package com.juliazluo.www.mdbsocials;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NewSocialActivity extends AppCompatActivity implements View.OnClickListener{

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

        storageRef = FirebaseStorage.getInstance().getReference();
        socialsListRef= FirebaseDatabase.getInstance().getReference("/socialsList");
        socialDetailsRef= FirebaseDatabase.getInstance().getReference("/socialDetails");

        mAuth = FirebaseAuth.getInstance();

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

        ((Button) findViewById(R.id.add_image_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.add_new_btn)).setOnClickListener(this);

        name = (EditText) findViewById(R.id.name_new);
        description = (EditText) findViewById(R.id.description_new);
        date = (EditText) findViewById(R.id.date_new);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        date.setOnClickListener(this);
    }

    private void updateLabel() {
        String dateFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        date.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        calendar = Calendar.getInstance(TimeZone.getDefault());
        Log.i("Got", "bla");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        dataIn = null;
    }

    private void selectImage() {
        Utils.selectImageOption(this, REQUEST_GALLERY, REQUEST_IMAGE_CAPTURE, CLASS_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dataIn = Utils.getImageURI(requestCode, resultCode, data, REQUEST_IMAGE_CAPTURE, RESULT_OK,
                REQUEST_GALLERY);
    }

    public void submit() {
        final String nameTxt = name.getText().toString();
        final String descTxt = description.getText().toString();
        final String dateTxt = date.getText().toString();

        final String id = socialsListRef.push().getKey();
        final String imageName = id + ".png";

        Social newSocial = new Social(id, nameTxt, user.getEmail(), 0, imageName, 0);

        if (dataIn != null) {
            storageRef.child(id + ".png").putFile(dataIn)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Push data to socials list ref node
                            socialsListRef.child(id).child("imageName").setValue(id + ".png");
                            socialsListRef.child(id).child("name").setValue(nameTxt);
                            socialsListRef.child(id).child("email").setValue(user.getEmail());
                            socialsListRef.child(id).child("numRSVP").setValue(new Long(0));
                            socialsListRef.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            //Push data to social details ref node
                            socialDetailsRef.child(id).child("imageName").setValue(id + ".png");
                            socialDetailsRef.child(id).child("name").setValue(nameTxt);
                            socialDetailsRef.child(id).child("email").setValue(user.getEmail());
                            socialDetailsRef.child(id).child("numRSVP").setValue(new Long(0));
                            socialDetailsRef.child(id).child("description").setValue(descTxt);
                            socialDetailsRef.child(id).child("date").setValue(dateTxt);

                            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                            startActivity(intent);
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
                            int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred())
                                    / taskSnapshot.getTotalByteCount());
                            Toast.makeText(NewSocialActivity.this, "Photo upload is " + progress + "% done",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Log.i("Got", "Default image used");


            //Push data to socials list ref node
            socialsListRef.child(id).child("imageName").setValue("default_icon.png");
            socialsListRef.child(id).child("name").setValue(nameTxt);
            socialsListRef.child(id).child("email").setValue(user.getEmail());
            socialsListRef.child(id).child("numRSVP").setValue(new Long(0));
            socialsListRef.child(id).child("timestamp").setValue(ServerValue.TIMESTAMP);

            //Push data to social details ref node
            socialDetailsRef.child(id).child("imageName").setValue("default_icon.png");
            socialDetailsRef.child(id).child("name").setValue(nameTxt);
            socialDetailsRef.child(id).child("email").setValue(user.getEmail());
            socialDetailsRef.child(id).child("numRSVP").setValue(new Long(0));
            socialDetailsRef.child(id).child("description").setValue(descTxt);
            socialDetailsRef.child(id).child("date").setValue(dateTxt);

            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.add_image_btn:
                selectImage();
                break;
            case R.id.add_new_btn:
                submit();
                break;
            case R.id.date_new:
                new DatePickerDialog(NewSocialActivity.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }
}
