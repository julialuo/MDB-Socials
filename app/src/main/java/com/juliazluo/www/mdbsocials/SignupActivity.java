package com.juliazluo.www.mdbsocials;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.CalendarContract.CalendarCache.URI;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_NAME = "SignupActivity";
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/" +
            "mdbsocials-e7639.appspot.com/o/default_member.png?alt=media&token=023caaae-d694-469a" +
            "-961d-402c29f425c8";
    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference storageRef;
    private TextView nameText;
    private String userImageUri;
    private Uri dataIn;
    private static int userNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Initiate activity components
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        nameText = (TextView) findViewById(R.id.name_signup);
        ((Button) findViewById(R.id.profile_pic_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.signup_btn)).setOnClickListener(this);
        ((TextView) findViewById(R.id.to_login)).setOnClickListener(this);

        //Initiate user authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(CLASS_NAME, "onAuthStateChanged:signed_in:" + user.getUid());

                    //Update user profile with name and profile picture URI
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nameText.getText().toString())
                            .setPhotoUri(Uri.parse(userImageUri))
                            .build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Log out user due to bug in Firebase about accessing name and profile image right after registration
                                mAuth.signOut();
                                Toast.makeText(SignupActivity.this, "Success! Please log in with your new account",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    // User is signed out
                    Log.i("User", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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
     * Attempt to upload the current image file to Firebase storage
     */
    private void attemptUpload() {
        //Generate unique image name for user
        String location = "user" + userNum + ".png";
        userNum += 1;

        if (dataIn != null) {
            //Start upload of chosen image to the Firebase storage
            storageRef.child(location).putFile(dataIn)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Store image URI and attempt to sign up the user
                            userImageUri = taskSnapshot.getDownloadUrl().toString();
                            attemptSignup();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //Handle unsuccessful uploads
                            Log.i(CLASS_NAME, "Failed upload " + exception.getMessage());
                            Toast.makeText(SignupActivity.this, "Image upload failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //Display progress of upload to user
                            int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) /
                                    taskSnapshot.getTotalByteCount());
                            Toast.makeText(SignupActivity.this, "Photo upload is " + progress + "% done",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //Set image URI to default image and attempt to sign up the user
            userImageUri = DEFAULT_IMAGE;
            attemptSignup();
        }
    }

    /**
     * Attempt to register the user into Firebase authentication
     */
    private void attemptSignup() {
        //Retrieve the user's input
        String email = ((EditText) findViewById(R.id.email_signup)).getText().toString();
        String password = ((EditText) findViewById(R.id.password_signup)).getText().toString();
        String confirm = ((EditText) findViewById(R.id.confirm_signup)).getText().toString();

        if (!email.equals("") && !password.equals("") && confirm.equals(password)) {
            //Use Firebase authentication to create new user with given email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.i(CLASS_NAME, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                //Notify user that authentication failed
                                Toast.makeText(SignupActivity.this, "Authentication failed, please " +
                                                "try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (!confirm.equals(password)) {
            //Passwords do not match
            Toast.makeText(SignupActivity.this, "The two passwords do not match",
                    Toast.LENGTH_SHORT).show();
        } else {
            //Email or password is blank
            Toast.makeText(SignupActivity.this, "Your email or password cannot be blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_pic_btn:
                //Open up image options menu and start either camera or gallery intent
                Utils.selectImageOption(this, REQUEST_GALLERY, REQUEST_IMAGE_CAPTURE, CLASS_NAME);
                break;
            case R.id.signup_btn:
                attemptUpload();
                break;
            case R.id.to_login:
                //Proceed to login screen
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
}
