package ir.sajjadboodaghi.niraa.adapters;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import ir.sajjadboodaghi.niraa.R;

/**
 * Created by Sajjad on 03/15/2018.
 */

public class SliderAdapter extends PagerAdapter {
    private Context context;
    private String[] imageUrls;
    private boolean isFullscreen;

    public SliderAdapter(Context context, String[] imageUrls, boolean isFullscreen) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isFullscreen = isFullscreen;
    }

    @Override
    public int getCount() {
        return imageUrls.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        if(isFullscreen) {
            ImageView imageView = new PhotoView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Picasso.with(context)
                    .load(imageUrls[position])
                    .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_loading_fullscreen, null))
                    .error(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_default, null))
                    .into(imageView);

            container.addView(imageView);
            return imageView;
        } else {
            RoundedImageView riv = new RoundedImageView(context);
            riv.setCornerRadius((float) 10);
            riv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(context)
                    .load(imageUrls[position])
                    .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_loading, null))
                    .error(ResourcesCompat.getDrawable(context.getResources(), R.drawable.image_default, null))
                    .into(riv);

            container.addView(riv);
            return riv;
        }

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
