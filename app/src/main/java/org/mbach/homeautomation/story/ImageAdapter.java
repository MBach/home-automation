package org.mbach.homeautomation.story;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * ImageAdapter class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolderImage> {

    private List<Model> items;

    private final ImageSearchActivity.OnItemClickListener listener;

    private ViewHolderImage viewHolderImage;

    ImageAdapter(ImageSearchActivity.OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * @param items list of items to display
     */
    void insertItems(List<Model> items) {
        // Don't keep old items (might change in the future to parse next pages)
        this.items = items;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolderImage onCreateViewHolder(final ViewGroup parent, int viewType) {
        viewHolderImage = new ViewHolderImage(new ImageView(parent.getContext()));
        return viewHolderImage;
    }

    @Override
    public void onBindViewHolder(ViewHolderImage holder, int position) {
        Model model = items.get(position);
        if (model == null) {
            return;
        }
        bind(model, listener);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        String imageURI = model.getTheContent();
        holder.image.setLayoutParams(lp);
        holder.image.setAdjustViewBounds(true);
        holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(holder.image.getContext()).load(imageURI).into(holder.image);
    }

    private void bind(final Model item, final ImageSearchActivity.OnItemClickListener listener) {
        viewHolderImage.image.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
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
