package com.juliazluo.www.mdbsocials;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by julia on 2017-02-28.
 */

public class Utils {

    private static String mCurrentPhotoPath = "gallery"; //Stores current photo path

    /**
     * Display alert dialog allowing the user to either take a picture or select an image from gallery
     *
     * @param activity           Activity accessing this method
     * @param requestGalleryCode Request code for gallery access
     * @param requestCameraCode  Request code for image capture from camera
     * @param className          Name of the class accessing this method (for logs)
     */
    public static void selectImageOption(final Activity activity, final int requestGalleryCode,
                                         final int requestCameraCode, final String className) {

        mCurrentPhotoPath = "gallery"; //Starts as "gallery" - indicates gallery option was selected
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        //Initiate alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    //Update current photo path to be the new photo that was taken
                    mCurrentPhotoPath = dispatchTakePictureIntent(className, activity, requestCameraCode);
                } else if (items[item].equals("Choose from Library")) {
                    //Start intent to pick image from gallery
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, requestGalleryCode);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss(); //Dismiss dialog
                }
            }
        });
        builder.show();
    }

    /**
     * Create new image file location on phone and populate that file with photo from camera intent
     *
     * @param className         Name of the class accessing this method (for logs)
     * @param activity          Activity accessing this method
     * @param requestCameraCode Request code for image capture from camera
     * @return String of the path to the newly created image file
     */
    private static String dispatchTakePictureIntent(final String className, Activity activity,
                                                    final int requestCameraCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        String photoPath = "error";
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".png",         /* suffix */
                        storageDir      /* directory */
                );
                photoPath = image.getAbsolutePath(); //Retrieve path of new image file
                photoFile = image;
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(className, "Error occurred while creating photo file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Start intent to take photo with camera, storing the new photo in the new image file location
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, requestCameraCode);
            }
        }
        return photoPath;
    }

    /**
     * Return the image URI of the current image either selected from gallery or taken by camera
     *
     * @param requestCode           Request code of the returned intent
     * @param resultCode            Result code of the returned intent
     * @param data                  Returned intent from gallery or camera
     * @param REQUEST_IMAGE_CAPTURE Request code for image capture from camera
     * @param RESULT_OK             Result code for successful intent
     * @param REQUEST_GALLERY       Request code for gallery access
     * @return URI of the image specified by the intent
     */
    public static Uri getImageURI(int requestCode, int resultCode, Intent data, final int REQUEST_IMAGE_CAPTURE,
                                  final int RESULT_OK, final int REQUEST_GALLERY) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Retrieve file at current photo path
            File f = new File(Utils.mCurrentPhotoPath);
            return Uri.fromFile(f);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            //Return the URI from gallery intent
            return data.getData();
        }
        return null;
    }

    /**
     * AsyncTask that downloads an image into a bitmap using Glide
     */
    public static class DownloadFilesTask extends AsyncTask<String, Void, Bitmap> {

        private Context context;
        private ImageView imageView;
        private int dimension;

        public DownloadFilesTask(Context context, ImageView imageView, int dimension) {
            this.context = context;
            this.imageView = imageView;
            this.dimension = dimension;
        }

        protected Bitmap doInBackground(String... strings) {
            //Download image into a bitmap using Glide (Aneesh said this was okay) and return it
            try {
                return Glide.
                        with(context).
                        load(strings[0]).
                        asBitmap().
                        into(dimension, dimension).
                        get();
            } catch (Exception e) {
                return null;
            }
        }

        protected void onProgressUpdate(Void... progress) {
        }

        protected void onPostExecute(Bitmap result) {
            //Set the image of the image view to the returned bitmap
            imageView.setImageBitmap(result);
        }
    }

    /**
     * Retrieve specified image from Firebase storage and load it into an image view
     *
     * @param className Name of the class accessing this method (for logs)
     * @param imageName Name of the image (specifies location in storage)
     * @param context   Context of the activity loading this image
     * @param imageView Image view to load image into
     * @param dimension Dimension that the image should be (px)
     */
    public static void loadImage(final String className, final String imageName, final Context context,
                                 final ImageView imageView, final int dimension) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Create new asynctask to load image
                new DownloadFilesTask(context, imageView, dimension).execute(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(className, "Couldn't find image file to load");
            }
        });
    }
}
