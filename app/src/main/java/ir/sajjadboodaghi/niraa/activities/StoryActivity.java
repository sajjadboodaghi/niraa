package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.adapters.StoryAdapter;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertYesOrNoCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.LinkHandler;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;

public class StoryActivity extends AppCompatActivity {

    int storyId;
    String link;
    String phone;
    Intent intent;
    ImageView imageView;
    int activityNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        // makeReadableTime layout full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        intent = getIntent();

        imageView = (ImageView) findViewById(R.id.imageView);
        handleLinkAndPhone();

        activityNumber = intent.getIntExtra("activity_number", 0);

        if(activityNumber == StoryAdapter.DEMO_ACTIVITY) {
            Uri uri = Uri.parse(intent.getStringExtra("imageUriStr"));
            imageView.setImageBitmap(makeLargeBitmap(uri));
        } else {
            storyId = intent.getIntExtra("story_id", 0);
            Picasso.with(this).load(Urls.STORIES_BASE_URL + "large_" + storyId + ".jpg")
                    .error(ResourcesCompat.getDrawable(getResources(), R.drawable.image_default, null))
                    .into(imageView);

            if(activityNumber == StoryAdapter.HOME_ACTIVITY) {
                doOtherUsersWorks();
            }

            if(activityNumber == StoryAdapter.USER_STORY_ACTIVITY) {
                    doOwnerWorks();
            }
        }

    }
    private void handleLinkAndPhone() {
        link = intent.getStringExtra("link");
        if(!link.isEmpty()) {
            ImageView linkImageView = (ImageView) findViewById(R.id.linkImageView);
            linkImageView.setVisibility(View.VISIBLE);
        }

        phone = intent.getStringExtra("phone");
        if(!phone.isEmpty()) {
            ImageView phoneImageView = (ImageView) findViewById(R.id.phoneImageView);
            phoneImageView.setVisibility(View.VISIBLE);
        }
    }
    private void doOwnerWorks() {
        ImageView deleteImageView = (ImageView) findViewById(R.id.deleteImageView);
        deleteImageView.setVisibility(View.VISIBLE);

        getVisitsCount();
    }
    private void doOtherUsersWorks() {
        String phoneNumber = intent.getStringExtra("phone_number");
        if(!SharedPrefManager.getInstance(this).isLogin() || !SharedPrefManager.getInstance(this).getPhoneNumber().equals(phoneNumber)) {
            RequestHandler.makeRequest(this, "GET", Urls.INCREMENT_VISIT_STORY + "/?story_id=" + storyId, null, new RequestCallback() {
                @Override
                public void onNoInternetAccess() {

                }

                @Override
                public void onSuccess(String response) {

                }

                @Override
                public void onRequestError() {

                }
            });
        }
    }
    private void getVisitsCount() {
        RequestHandler.makeRequest(this, "GET", Urls.GET_STORY_VISITS + "/?story_id=" + storyId, null, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.notice(StoryActivity.this, message, new AlertNoticeCallback() {
                    @Override
                    public void okay() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String visitsCount = jsonObject.getString("visits_count");

                    TextView visitTextView = (TextView) findViewById(R.id.visitTextView);
                    visitTextView.setText(String.valueOf(visitsCount));

                    LinearLayout visitLinearLayout = (LinearLayout) findViewById(R.id.visitLinearLayout);
                    visitLinearLayout.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();

                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.notice(StoryActivity.this, message, new AlertNoticeCallback() {
                        @Override
                        public void okay() {

                        }
                    });
                }

            }

            @Override
            public void onRequestError() {
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.notice(StoryActivity.this, message, new AlertNoticeCallback() {
                    @Override
                    public void okay() {

                    }
                });
            }
        });
    }
    public void deleteStory(View v) {
        String message = getResources().getString(R.string.want_delete_story);
        AlertHandler.yesOrNo(this, message, new AlertYesOrNoCallback() {
            @Override
            public void yes() {
                sendDeleteStoryRequest();
            }

            @Override
            public void no() {

            }
        });
    }
    private void sendDeleteStoryRequest() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
        progressDialog.setCancelable(false);
        progressDialog.show();
        RequestHandler.makeRequest(this, "POST", Urls.DELETE_STORY, deleteStoryParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(StoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendDeleteStoryRequest();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String message = jsonResponse.getString("message");
                    boolean error = jsonResponse.getBoolean("error");
                    if(error) {
                        AlertHandler.notice(StoryActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                finish();
                            }
                        });
                    } else {
                        AlertHandler.notice(StoryActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                CreateStoryActivity.needs_to_refresh_stories = true;
                                HomeActivity.needs_to_refresh_stories = true;
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(StoryActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            sendDeleteStoryRequest();
                        }

                        @Override
                        public void cancel() {

                        }
                    });
                }

            }

            @Override
            public void onRequestError() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(StoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendDeleteStoryRequest();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        });
    }
    private Map<String, String> deleteStoryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("story_id", String.valueOf(storyId));
        return params;
    }

    public void closeStory(View v) {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Nullable
    private Bitmap makeLargeBitmap(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            double currentHeight = bitmap.getHeight();
            double currentWidth = bitmap.getWidth();

            int newHeight;
            int newWidth;

            if(currentHeight > currentWidth && currentHeight > 1000) {
                newHeight = 1000;
                newWidth = (int) ((currentWidth / currentHeight) * newHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            } else if(currentWidth > 1000) {
                newWidth = 1000;
                newHeight = (int) ((currentHeight / currentWidth) * newWidth);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            }

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void openLink(View v) {
        LinkHandler.open(this, link);
    }
    public void call(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phone));
        startActivity(callIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
