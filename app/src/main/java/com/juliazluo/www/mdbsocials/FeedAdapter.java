package com.juliazluo.www.mdbsocials;

/**
 * Created by julia on 2017-02-19.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Julia Luo on 2/17/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.CustomViewHolder> {

    private Context context;
    private ArrayList<Social> data;

    public FeedAdapter(Context context, ArrayList<Social> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new CustomViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        final Social social = data.get(position);
        holder.nameText.setText(social.getName());
        holder.emailText.setText("Host: " + social.getEmail());
        holder.attendingText.setText(social.getNumRSVP() + "");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("SOCIAL_ID", social.getId());
                intent.putExtra("IMAGE_NAME", social.getImageName());
                FeedActivity.leavingApp = false;
                context.startActivity(intent);
            }
        });

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child(social.getImageName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .override(150, 150)
                        .into(holder.image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Storage", "Couldn't find file");
            }
        });

        /*
        //haven't taught this yet but essentially it runs separately from the UI
        class DownloadFilesTask extends AsyncTask<String, Void, Bitmap> {
            protected Bitmap doInBackground(String... strings) {
                try {return Glide.
                        with(context).
                        load(strings[0]).
                        asBitmap().
                        into(100, 100). // Width and height
                        get();}
                catch (Exception e) {return null;}
            }

            protected void onProgressUpdate(Void... progress) {}

            protected void onPostExecute(Bitmap result) {
                holder.imageView.setImageBitmap(result);
            }
        }

        //Part 4: Load the image from the url. Use
        // new DownloadFilesTask().execute(uri.toString())
        // to get set the imageView using the resulting Uri. If it fails, log the exception

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child(m.firebaseImageUrl).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                new DownloadFilesTask().execute(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Storage", "Couldn't find file");
            }
        });
        //dw if it doesn't work, bc it didn't work for me :( */
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * A card displayed in the RecyclerView
     */
    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, attendingText;
        ImageView image;

        public CustomViewHolder (View view) {
            super(view);
            this.nameText = (TextView) view.findViewById(R.id.feed_name);
            this.emailText = (TextView) view.findViewById(R.id.feed_email);
            this.attendingText = (TextView) view.findViewById(R.id.feed_attending);
            this.image = (ImageView) view.findViewById(R.id.feed_image);
        }
    }
}

