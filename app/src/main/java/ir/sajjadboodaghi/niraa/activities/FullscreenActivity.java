package ir.sajjadboodaghi.niraa.activities;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.adapters.SliderAdapter;

public class FullscreenActivity extends AppCompatActivity {
    private int dotsCount;
    private ImageView[] dots;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        String[] imagesUrls = getIntent().getStringArrayExtra("imagesUrls");
        ViewPager viewPager = (ViewPager) findViewById(R.id.fullscreenViewPager);
        SliderAdapter sliderAdapter = new SliderAdapter(this, imagesUrls, true);
        viewPager.setAdapter(sliderAdapter);

        LinearLayout sliderDotsLinearLayout = (LinearLayout) findViewById(R.id.sliderDotsLinearLayout);
        dotsCount = sliderAdapter.getCount();
        if(dotsCount >= 1) {
            dots = new ImageView[dotsCount];
            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(this);
                dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.x_slider_dot_deactive));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(6, 0, 6, 0);
                sliderDotsLinearLayout.addView(dots[i], params);
            }
            dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.x_slider_dot_active));

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < dotsCount; i++) {
                        dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.x_slider_dot_deactive));
                        dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.x_slider_dot_active));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void close(View view) {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
