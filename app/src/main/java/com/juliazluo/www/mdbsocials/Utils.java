package com.juliazluo.www.mdbsocials;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by julia on 2017-02-28.
 */

public class Utils {

    public static String mCurrentPhotoPath = "gallery";

    public static void selectImageOption(final Activity activity, final int requestGalleryCode,
                                           final int requestCameraCode, final String className) {

        mCurrentPhotoPath = "gallery";
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    mCurrentPhotoPath = dispatchTakePictureIntent(className, activity, requestCameraCode);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, requestGalleryCode);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

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
                photoPath = image.getAbsolutePath();
                photoFile = image;
            } catch (IOException ex) {
                // Error occurred while creating the File...
                Log.i(className, "Error occurred while creating photo file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, requestCameraCode);
            }
        }
        return photoPath;
    }

    public static Uri getImageURI(int requestCode, int resultCode, Intent data, final int REQUEST_IMAGE_CAPTURE,
                                  final int RESULT_OK, final int REQUEST_GALLERY) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File f = new File(Utils.mCurrentPhotoPath);
            return Uri.fromFile(f);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            return data.getData();
        }
        return null;
    }

}
