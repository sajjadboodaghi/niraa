package ir.sajjadboodaghi.niraa.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;

import ir.sajjadboodaghi.niraa.activities.ItemActivity;
import ir.sajjadboodaghi.niraa.activities.PaymentUpdateActivity;
import ir.sajjadboodaghi.niraa.activities.UserItemsActivity;
import ir.sajjadboodaghi.niraa.models.Item;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;

/**
 * Created by Sajjad on 02/05/2018.
 */

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.ViewHolder> {

    private List<Item> items;
    private Context context;

    public UserItemAdapter(List<Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Item item = items.get(position);

        holder.textViewTitle.setText(item.getTitle());
        holder.deleteItem.setContentDescription(String.valueOf(item.getId()));
        holder.updateItem.setContentDescription(String.valueOf(item.getId()));

        if(item.getVerified() == 1) {
            holder.updateItem.setVisibility(View.VISIBLE);
            holder.verifiedItem.setVisibility(View.VISIBLE);
            holder.waitingItem.setVisibility(View.GONE);
        } else {
            holder.updateItem.setVisibility(View.GONE);
            holder.verifiedItem.setVisibility(View.GONE);
            holder.waitingItem.setVisibility(View.VISIBLE);
        }

        if(item.getImageCount() > 0) {
            Picasso.with(context).load(Urls.IMAGES_BASE_URL + "item_" + item.getId() + "_thumbnail.jpg")
                    .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_loading, null))
                    .error(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_loading, null))
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_default, null));
        }

        holder.updateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PaymentUpdateActivity.class);
                intent.putExtra("itemId", item.getId());
                intent.putExtra("itemPhoneNumber", item.getPhoneNumber());
                intent.putExtra("itemTelegramId", item.getTelegramId());
                intent.putExtra("itemTitle", item.getTitle());
                intent.putExtra("itemDescription", item.getDescription());
                intent.putExtra("itemPrice", item.getPrice());
                intent.putExtra("itemPlace", item.getPlace());
                intent.putExtra("itemSubcatName", item.getSubcatName());
                intent.putExtra("itemSubcatId", item.getSubcatId());
                intent.putExtra("itemShamsi", item.getShamsi());
                intent.putExtra("itemTimestamp", item.getTimestamp());
                intent.putExtra("itemImageCount", item.getImageCount());
                intent.putExtra("itemVerified", item.getVerified());
                view.getContext().startActivity(intent);
                UserItemsActivity.me.finish();
            }
        });

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ItemActivity.class);
                intent.putExtra("itemId", item.getId());
                intent.putExtra("itemPhoneNumber", item.getPhoneNumber());
                intent.putExtra("itemTelegramId", item.getTelegramId());
                intent.putExtra("itemTitle", item.getTitle());
                intent.putExtra("itemDescription", item.getDescription());
                intent.putExtra("itemPrice", item.getPrice());
                intent.putExtra("itemPlace", item.getPlace());
                intent.putExtra("itemSubcatName", item.getSubcatName());
                intent.putExtra("itemSubcatId", item.getSubcatId());
                intent.putExtra("itemShamsi", item.getShamsi());
                intent.putExtra("itemTimestamp", item.getTimestamp());
                intent.putExtra("itemImageCount", item.getImageCount());
                intent.putExtra("itemVerified", item.getVerified());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        ImageView deleteItem;
        ImageView updateItem;
        ImageView waitingItem;
        ImageView verifiedItem;
        ImageView imageView;
        RelativeLayout itemLayout;

        ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            deleteItem = (ImageView) itemView.findViewById(R.id.deleteItem);
            updateItem = (ImageView) itemView.findViewById(R.id.updateItem);
            waitingItem = (ImageView) itemView.findViewById(R.id.waitingItem);
            verifiedItem = (ImageView) itemView.findViewById(R.id.verifiedItem);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.itemLayout);
        }
    }

}
