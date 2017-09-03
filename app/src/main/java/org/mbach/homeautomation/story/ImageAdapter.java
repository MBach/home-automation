package org.mbach.homeautomation.story;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.mbach.homeautomation.Constants;

import java.util.List;

/**
 * ImageAdapter class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolderImage> {

    public interface OnClickImageListener {
        void onClick(Bitmap bitmap);
    }

    private static final String TAG = "ImageAdapter";

    private List<Model> items;

    private ImageSearchActivity imageSearchActivity;

    public ImageAdapter(ImageSearchActivity imageSearchActivity) {
        this.imageSearchActivity = imageSearchActivity;
    }

    /**
     * @param items list of items to display
     */
    void insertItems(List<Model> items) {
        // Don't keep old items (might change in the future to parse next pages)
        this.items = items;
        notifyDataSetChanged();
    }

    private ViewHolderImage viewHolderImage;

    /*private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Click !");
            //Toast.makeText(parent.getContext(), "Click on view", Toast.LENGTH_SHORT).show();
            viewHolderImage.image.buildDrawingCache();
            Bitmap bitmap = viewHolderImage.image.getDrawingCache();
            //Environment.getDataDirectory();

            /// TODO save image to FS and pass only the link
            //Intent intent = new Intent(imageSearchActivity.getApplicationContext(), ImageSearchActivity.class);
            //intent.putExtra("test2", bitmap);
            //imageSearchActivity.setIntent(intent);
            //imageSearchActivity.setResult(Constants.RES_IMAGE_PICKED_BY_ONE);
            //imageSearchActivity.finish();
            imageSearchActivity.onClick(bitmap);
            //imageSearchActivity.processBitmap(bitmap);
            //imageSearchActivity.moveTaskToBack(true);
            //processBitmap(bitmap);
            //ImageAdapter.on();
        }
    };*/

    @Override
    public ViewHolderImage onCreateViewHolder(final ViewGroup parent, int viewType) {
        viewHolderImage = new ViewHolderImage(new ImageView(parent.getContext()));
        viewHolderImage.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "ICI ?");
    }*/

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
