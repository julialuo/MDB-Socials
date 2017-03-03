package com.juliazluo.www.mdbsocials;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by julia on 2017-02-23.
 */

public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.CustomViewHolder> {

    private Context context;
    private ArrayList<User> data;

    public PopupAdapter(Context context, ArrayList<User> data) {
        this.context = context;
        this.data = data;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_item, parent, false);
        return new CustomViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        //Display user information
        User user = data.get(position);
        holder.nameText.setText(user.getName());

        //Load the user profile picture into image view
        new Utils.DownloadFilesTask(context, holder.image, 100).execute(user.getImageURI());
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * A card displayed in the RecyclerView
     */
    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageView image;

        public CustomViewHolder(View view) {
            super(view);

            //Initiate components on the ViewHolder (list item view)
            this.nameText = (TextView) view.findViewById(R.id.popup_name);
            this.image = (ImageView) view.findViewById(R.id.popup_image);
        }
    }
}
