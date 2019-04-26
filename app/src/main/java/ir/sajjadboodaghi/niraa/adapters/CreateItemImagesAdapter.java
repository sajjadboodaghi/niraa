package ir.sajjadboodaghi.niraa.adapters;

/**
 * Created by Sajjad on 03/27/2018.
 */

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ir.sajjadboodaghi.niraa.R;

public class CreateItemImagesAdapter extends RecyclerView.Adapter<CreateItemImagesAdapter.ViewHolder> {
    // references to our images
    private List<Bitmap> thumbnails;
    private List<Uri> uris;

    public CreateItemImagesAdapter(List<Bitmap> thumbnails, List<Uri> uris) {
        this.thumbnails = thumbnails;
        this.uris = uris;
    }

    public List<Uri> getUris() {
        return this.uris;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_create_item_image_layout, parent, false);
        return new CreateItemImagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.imageView.setImageBitmap(thumbnails.get(position));
        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbnails.remove(position);
                uris.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return thumbnails.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            deleteImageView = (ImageView) itemView.findViewById(R.id.deleteImageView);
        }
    }
}