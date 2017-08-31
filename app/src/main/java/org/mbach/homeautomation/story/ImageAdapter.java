package org.mbach.homeautomation.story;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * ImageAdapter class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolderImage> {

    private static final String TAG = "ImageAdapter";

    private List<Model> items;

    /**
     * @param items list of items to display
     */
    void insertItems(List<Model> items) {
        if (this.items == null) {
            this.items = items;
        } else {
            this.items.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolderImage onCreateViewHolder(final ViewGroup parent, int viewType) {
        ViewHolderImage viewHolderImage = new ViewHolderImage(new ImageView(parent.getContext()));
        viewHolderImage.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Click !");
                Toast.makeText(parent.getContext(), "Click on view", Toast.LENGTH_SHORT).show();
                /// TODO save image to Story
            }
        });
        return viewHolderImage;
    }

    @Override
    public void onBindViewHolder(ViewHolderImage holder, int position) {
        Model model = items.get(position);
        if (model == null) {
            return;
        }
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        String imageURI = model.getTheContent();
        holder.image.setLayoutParams(lp);
        holder.image.setAdjustViewBounds(true);
        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(holder.image.getContext()).load(imageURI).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    /**
     *
     */
    static class ViewHolderImage extends RecyclerView.ViewHolder {

        @NonNull
        private final ImageView image;

        ViewHolderImage(@NonNull View view) {
            super(view);
            this.image = (ImageView) view;
        }
    }
}
