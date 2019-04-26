package ir.sajjadboodaghi.niraa.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ir.sajjadboodaghi.niraa.activities.StoryActivity;
import ir.sajjadboodaghi.niraa.models.Story;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;

/**
 * Created by Sajjad on 09/23/2018.
 */

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Context context;
    private List<Story> stories;
    private int activityNumber;
    public static final int HOME_ACTIVITY = 551;
    public static final int USER_STORY_ACTIVITY = 552;
    public static final int PAYMENT_ACTIVITY = 553;
    public static final int DEMO_ACTIVITY = 554;

    public StoryAdapter(Context context, List<Story> stories, int activityNumber) {
        this.context = context;
        this.stories = stories;
        this.activityNumber = activityNumber;
    }

    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_story_layout, parent, false);
        return new StoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoryAdapter.ViewHolder holder, final int position) {

        final Story story = stories.get(position);
        final int id = story.getId();
        Picasso.with(context).load(Urls.STORIES_BASE_URL + "small_" + id + ".jpg")
                .error(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_default, null))
                .into(holder.storyImageView);

        final String link = story.getLink();
        final String phone = story.getPhone();

        holder.storyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), StoryActivity.class);
                intent.putExtra("story_id", id);
                intent.putExtra("link", link);
                intent.putExtra("phone", phone);
                intent.putExtra("phone_number", story.getPhoneNumber());
                intent.putExtra("activity_number", activityNumber);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storyImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            storyImageView = itemView.findViewById(R.id.storyImageView);
        }
    }
}
