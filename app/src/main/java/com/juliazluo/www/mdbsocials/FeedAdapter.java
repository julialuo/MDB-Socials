package com.juliazluo.www.mdbsocials;

/**
 * Created by julia on 2017-02-19.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Julia Luo on 2/17/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.CustomViewHolder> {

    protected static final String SOCIAL_ID = "SocialID";
    protected static final String IMAGE_NAME = "ImageName";
    private static final String CLASS_NAME = "FeedAdapter";
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
        //Display the social's information onto the ViewHolder
        final Social social = data.get(position);
        holder.nameText.setText(social.getName());
        holder.emailText.setText("Host: " + social.getEmail());
        holder.attendingText.setText(social.getNumRSVP() + "");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Proceed to details activity with the ID and image name of the clicked social
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(SOCIAL_ID, social.getId());
                intent.putExtra(IMAGE_NAME, social.getImageName());
                FeedActivity.leavingApp = false;
                context.startActivity(intent);
            }
        });

        //Load the social image into image view
        Utils.loadImage(CLASS_NAME, social.getImageName(), context, holder.image, 120);
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

        public CustomViewHolder(View view) {
            super(view);

            //Initiate components on the ViewHolder (list item view)
            this.nameText = (TextView) view.findViewById(R.id.feed_name);
            this.emailText = (TextView) view.findViewById(R.id.feed_email);
            this.attendingText = (TextView) view.findViewById(R.id.feed_attending);
            this.image = (ImageView) view.findViewById(R.id.feed_image);
        }
    }
}

