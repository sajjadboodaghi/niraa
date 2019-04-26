package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.BeforeCall;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.BeforeCallBack;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.adapters.SliderAdapter;
import ir.sajjadboodaghi.niraa.handlers.TelegramIntent;

import static android.view.View.GONE;

public class ItemActivity extends AppCompatActivity {

    private int dotsCount;
    private ImageView[] dots;
    String[] imagesUrls;
    Toolbar toolbar;
    ImageView bookmarkImageView;
    String ownerPhoneNumber;
    TextView userNameTextView;
    RoundedImageView userImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int imageCount = getIntent().getIntExtra("itemImageCount", 0);
        if(imageCount > 0) {
            setContentView(R.layout.activity_item);
            imagesUrlMaker(imageCount);
            showImageSlider();
        } else {
            setContentView(R.layout.activity_item_noimage);
        }

        toolbar = (Toolbar) findViewById(R.id.itemPageToolbar);
        setSupportActionBar(toolbar);

        preparePage();

        findViewsAndSetValue();
    }

    private void preparePage() {
        bookmarkImageView = (ImageView) findViewById(R.id.bookmarkImageView);
        ownerPhoneNumber = getIntent().getStringExtra("itemPhoneNumber");
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        userImageView = (RoundedImageView) findViewById(R.id.userImageCircleImageView);

        if(SharedPrefManager.getInstance(this).isLogin() && !ownerPhoneNumber.equals(SharedPrefManager.getInstance(this).getPhoneNumber())) {
            getUserAndBookmarkInfo();
        } else {
            getUserInfo();
        }

    }
    private void getUserAndBookmarkInfo() {
        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_BOOKMARK_INFO, userAndBookmarkInfoParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {

            }

            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(!error) {
                        String userName = jsonObject.getString("user_name");
                        String userImageName = jsonObject.getString("user_image_name");

                        if(userName.trim().equals("")) {
                            userNameTextView.setText(getResources().getString(R.string.user_name_placeholder));
                        } else {
                            userNameTextView.setText(userName);
                        }

                        if(!userImageName.equals("default.jpg")) {
                            Picasso.with(ItemActivity.this).load(SharedPrefManager.getInstance(ItemActivity.this).userImageUrlMaker(userImageName))
                                    .placeholder(ResourcesCompat.getDrawable(ItemActivity.this.getResources(), R.drawable.image_loading, null))
                                    .error(ResourcesCompat.getDrawable(ItemActivity.this.getResources(), R.drawable.image_default_user, null))
                                    .into(userImageView);
                        }

                        boolean bookmarked = jsonObject.getBoolean("bookmarked");
                        if(bookmarked) {
                            bookmarkImageView.setImageDrawable(ContextCompat.getDrawable(ItemActivity.this, R.drawable.icon_bookmarked));
                        } else {
                            bookmarkImageView.setImageDrawable(ContextCompat.getDrawable(ItemActivity.this, R.drawable.icon_unbookmarked));
                        }
                        bookmarkImageView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onRequestError() {
            }
        });
    }
    private Map<String, String> userAndBookmarkInfoParams() {
        Map<String, String> params = new HashMap<>();
        params.put("owner_phone_number", ownerPhoneNumber);
        params.put("item_id", String.valueOf(getIntent().getIntExtra("itemId", 0)));
        params.put("loggedin_phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        return params;
    }

    private void getUserInfo() {
        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_BOOKMARK_INFO, userInfoParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {

            }

            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(!error) {
                        String userName = jsonObject.getString("user_name");
                        String userImageName = jsonObject.getString("user_image_name");

                        if(userName.trim().equals("")) {
                            userNameTextView.setText(getResources().getString(R.string.user_name_placeholder));
                        } else {
                            userNameTextView.setText(userName);
                        }

                        if(!userImageName.equals("default.jpg")) {
                            Picasso.with(ItemActivity.this).load(SharedPrefManager.getInstance(ItemActivity.this).userImageUrlMaker(userImageName))
                                    .placeholder(ResourcesCompat.getDrawable(ItemActivity.this.getResources(), R.drawable.image_loading, null))
                                    .error(ResourcesCompat.getDrawable(ItemActivity.this.getResources(), R.drawable.image_default_user, null))
                                    .into(userImageView);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onRequestError() {
            }
        });
    }
    private Map<String, String> userInfoParams() {
        Map<String, String> params = new HashMap<>();
        params.put("owner_phone_number", ownerPhoneNumber);
        return params;
    }

    private void imagesUrlMaker(int imageCount) {
        imagesUrls = new String[imageCount];
        for(int i = 0; i < imageCount; i++) {
            imagesUrls[i] = Urls.IMAGES_BASE_URL + "item_" + getIntent().getIntExtra("itemId", 0) + "_" + (i+1) + ".jpg";
        }
    }

    private void findViewsAndSetValue() {
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView priceTextView = (TextView) findViewById(R.id.priceTextView);
        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        TextView phoneTextView = (TextView) findViewById(R.id.phoneTextView);

        toolbar.setTitle(getIntent().getStringExtra("itemPlace"));
        toolbar.setSubtitle(getIntent().getStringExtra("itemShamsi").substring(0, 8));

        titleTextView.setText(getIntent().getStringExtra("itemTitle"));

        // if price was empty price part will disappear
        // else price value will set into price edittext
        String itemPrice = getIntent().getStringExtra("itemPrice");
        if(itemPrice.replace(" ", "").equals("")) {
            priceTextView.setVisibility(GONE);
        } else {
            priceTextView.setText(itemPrice);
        }

        descriptionTextView.setText(getIntent().getStringExtra("itemDescription"));
        phoneTextView.setText(ownerPhoneNumber);

        ImageView telegramImageView = (ImageView) findViewById(R.id.telegramImageView);
        TextView reportItem = (TextView) findViewById(R.id.reportItem);

        if(!getIntent().getStringExtra("itemTelegramId").equals("")) {
            telegramImageView.setVisibility(View.VISIBLE);
        }

        if(SharedPrefManager.getInstance(this).isLogin() && !SharedPrefManager.getInstance(this).getPhoneNumber().equals(getIntent().getStringExtra("itemPhoneNumber"))) {
            reportItem.setVisibility(View.VISIBLE);
        }
    }

    private void showImageSlider() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        SliderAdapter sliderAdapter = new SliderAdapter(this, imagesUrls, false);
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

    public void call(View v) {
        BeforeCall.show(this, new BeforeCallBack() {
            @Override
            public void onOK() {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + getIntent().getStringExtra("itemPhoneNumber")));
                startActivity(callIntent);
            }
        });
    }
    public void openTelegramProfile(View view) {
        BeforeCall.show(this, new BeforeCallBack() {
            @Override
            public void onOK() {
                new TelegramIntent().start(ItemActivity.this, getIntent().getStringExtra("itemTelegramId"));
            }
        });
    }
    public void reportItem(final View view) {
        final EditText editText = new EditText(this);
        editText.setLines(3);
        editText.setMaxLines(3);
        editText.setHint("توضیحات...");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        editText.setLayoutParams(lp);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("گزارش تخلف");
        alertDialogBuilder.setMessage("در مورد تخلف صورت گرفته توضیح دهید");
        alertDialogBuilder.setNegativeButton("بازگشت", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialogBuilder.setPositiveButton("ارسال گزارش", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                final String description = editText.getText().toString();
                if(description.replace(" ", "").length() > 3) {
                    Map<String, String> params = new HashMap<>();
                    params.put("phone_number", SharedPrefManager.getInstance(ItemActivity.this).getPhoneNumber());
                    params.put("token", SharedPrefManager.getInstance(ItemActivity.this).getToken());
                    params.put("description", description);
                    params.put("item_id", String.valueOf(getIntent().getIntExtra("itemId", 0)));

                    final ProgressDialog progressDialog = new ProgressDialog(ItemActivity.this);
                    progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    RequestHandler.makeRequest(ItemActivity.this, "POST", Urls.REPORT_ITEM, params, new RequestCallback() {
                        @Override
                        public void onNoInternetAccess() {
                            progressDialog.dismiss();

                            String message = getResources().getString(R.string.no_internet_access);
                            AlertHandler.notice(ItemActivity.this, message, new AlertNoticeCallback() {

                                @Override
                                public void okay() {
                                    // comeback and do nothing
                                }
                            });
                        }

                        @Override
                        public void onSuccess(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Boolean error = jsonObject.getBoolean("error");
                                String message = jsonObject.getString("message");
                                if(error) {
                                    AlertHandler.notice(ItemActivity.this, message, new AlertNoticeCallback() {
                                        @Override
                                        public void okay() {

                                        }
                                    });
                                    return;
                                }

                                AlertHandler.notice(ItemActivity.this, message, new AlertNoticeCallback() {
                                    @Override
                                    public void okay() {

                                    }
                                });


                            } catch (JSONException e) {
                                e.printStackTrace();
                                String message = getResources().getString(R.string.problem_parse_response);
                                AlertHandler.notice(ItemActivity.this, message, new AlertNoticeCallback() {

                                    @Override
                                    public void okay() {
                                        // comeback and do nothing
                                    }
                                });
                            }
                        }

                        @Override
                        public void onRequestError() {
                            progressDialog.dismiss();

                            String message = getResources().getString(R.string.problem_request);
                            AlertHandler.notice(ItemActivity.this, message, new AlertNoticeCallback() {

                                @Override
                                public void okay() {
                                    // comeback and do nothing
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(ItemActivity.this, "گزارش ارسال نشد! (بخش توضیحات تکمیل نشده بود)", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.show();
    }
    public void fullscreen(View view) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra("imagesUrls", imagesUrls);
        startActivity(intent);
    }
    public void bookmark(final View v) {
        v.setVisibility(View.GONE);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.please_wait_a_moment));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.BOOKMARK, bookmarkParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                v.setVisibility(View.VISIBLE);
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(ItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        bookmark(v);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                pd.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String message = jsonObject.getString("message");
                        v.setVisibility(View.VISIBLE);
                        AlertHandler.error(ItemActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                bookmark(v);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    } else {
                        BookmarksActivity.needs_to_refresh = true;
                        String status = jsonObject.getString("status");
                        if(status.equals("bookmarked")) {
                            bookmarkImageView.setImageDrawable(ContextCompat.getDrawable(ItemActivity.this, R.drawable.icon_bookmarked));
                        } else if(status.equals("unbookmarked")) {
                            bookmarkImageView.setImageDrawable(ContextCompat.getDrawable(ItemActivity.this, R.drawable.icon_unbookmarked));
                        }
                        bookmarkImageView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestError() {
                pd.dismiss();
                v.setVisibility(View.VISIBLE);
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(ItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        bookmark(v);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        });
    }

    private Map<String, String> bookmarkParams() {
        Map<String, String> params = new HashMap<>();
        params.put("item_id", String.valueOf(getIntent().getIntExtra("itemId", 0)));
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        return params;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menuBack) {
            finish();
        }
        return true;
    }

}
