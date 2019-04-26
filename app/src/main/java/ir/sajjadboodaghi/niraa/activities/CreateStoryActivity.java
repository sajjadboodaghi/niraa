package ir.sajjadboodaghi.niraa.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.LinkHandler;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Story;
import ir.sajjadboodaghi.niraa.adapters.StoryAdapter;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;

public class CreateStoryActivity extends AppCompatActivity {
    private static final int SMALL_IMAGE = 770;
    private static final int LARGE_IMAGE = 771;
    private int which_image;
    public static boolean needs_to_refresh_stories = false;

    Uri mCropImageUri = Uri.EMPTY;
    Uri uri = Uri.EMPTY;
    Uri largeUri = Uri.EMPTY;
    ImageView smallImageView;
    Bitmap smallBitmap = null;
    Bitmap largeBitmap = null;
    EditText linkEditText;
    EditText phoneEditText;
    ProgressDialog progressDialog;
    List<Story> userStoryList;
    RecyclerView storyRecyclerView;
    StoryAdapter storyAdapter;
    TextView rulesTextView;
    LinearLayout rootLinearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);


        Toolbar toolbar = (Toolbar) findViewById(R.id.createStoryToolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            Toast.makeText(this, getResources().getString(R.string.toast_first_login), Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(this).setTargetActivity("CreateStoryActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        smallImageView = (ImageView) findViewById(R.id.smallImageView);
        linkEditText = (EditText) findViewById(R.id.linkEditText);
        phoneEditText = (EditText) findViewById(R.id.phoneEditText);
        progressDialog = new ProgressDialog(this);
        rulesTextView = (TextView) findViewById(R.id.rulesTextView);
        rootLinearLayout = (LinearLayout) findViewById(R.id.rootLinearLayout);

        getDraftUserStories();
    }

    private void getDraftUserStories() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.please_wait_a_moment));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_STORIES, getDraftUserStoriesParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getDraftUserStories();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                pd.dismiss();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean error = jsonResponse.getBoolean("error");
                    if(error) {
                        String message = jsonResponse.getString("message");
                        AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                getDraftUserStories();
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                    } else {
                        JSONArray draftUserStoryArray = jsonResponse.getJSONArray("draft_user_stories");
                        if(draftUserStoryArray.length() > 0) {
                            int storyId = draftUserStoryArray.getJSONObject(0).getInt("id");
                            String link = draftUserStoryArray.getJSONObject(0).getString("link");
                            String phone = draftUserStoryArray.getJSONObject(0).getString("phone");
                            Intent paymentIntent = new Intent(CreateStoryActivity.this, PaymentStoryActivity.class);
                            paymentIntent.putExtra("story_id", storyId);
                            paymentIntent.putExtra("link", link);
                            paymentIntent.putExtra("phone", phone);
                            startActivity(paymentIntent);
                            finish();
                        } else {
                            setupUserStoryRecyclerView();
                            getUserStories();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            getDraftUserStories();
                        }

                        @Override
                        public void cancel() {
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                pd.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getDraftUserStories();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    private Map<String, String> getDraftUserStoriesParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("status", "draft");
        return params;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(needs_to_refresh_stories) {
            getUserStories();
            needs_to_refresh_stories = false;
        }
    }

    private void setupUserStoryRecyclerView() {
        storyRecyclerView = (RecyclerView) findViewById(R.id.storyRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        userStoryList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, userStoryList, StoryAdapter.USER_STORY_ACTIVITY);
        storyRecyclerView.setAdapter(storyAdapter);
    }
    public void getUserStories() {
        progressDialog.setMessage(getResources().getString(R.string.getting_user_stories));
        progressDialog.setCancelable(false);
        progressDialog.show();
        storyRecyclerView.setVisibility(View.GONE);
        userStoryList.clear();
        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_STORIES, getPublishedUserStoriesParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getUserStories();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean error = jsonResponse.getBoolean("error");
                    if(error) {
                        String message = jsonResponse.getString("message");
                        AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                getUserStories();
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                    } else {
                        String rules = jsonResponse.getString("rules");
                        rulesTextView.setText(rules);
                        rootLinearLayout.setVisibility(View.VISIBLE);

                        JSONArray publishedUserStoryArray = jsonResponse.getJSONArray("published_user_stories");
                        if(publishedUserStoryArray.length() > 0) {
                            JSONObject storyObject;
                            for(int i = 0; i < publishedUserStoryArray.length(); i++) {
                                storyObject = publishedUserStoryArray.getJSONObject(i);
                                userStoryList.add(new Story(storyObject.getInt("id"), storyObject.getString("phone_number"), storyObject.getString("link"), storyObject.getString("phone")));
                            }
                            storyAdapter.notifyDataSetChanged();
                            storyRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            getUserStories();
                        }

                        @Override
                        public void cancel() {
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getUserStories();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    private Map<String, String> getPublishedUserStoriesParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("status", "published");
        return params;
    }

    public void selectSmallImage(View v) {
        which_image = SMALL_IMAGE;
        selectImage();
    }
    public void selectLargeImage(View v) {
        which_image = LARGE_IMAGE;
        selectImage();
    }
    private void selectImage() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();

                if(which_image == SMALL_IMAGE)
                    doSmallImageWorks();
                if(which_image == LARGE_IMAGE)
                    doLargeImageWorks();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, getResources().getString(R.string.required_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        if(which_image == LARGE_IMAGE) {
            CropImage.activity(imageUri)
                    .setAspectRatio(100, 170)
                    .start(this);
        } else if(which_image == SMALL_IMAGE) {
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
        }
    }

    private void doSmallImageWorks() {
        if(uri != null) {
            smallBitmap = makeThumbnail();
            smallImageView.setImageBitmap(smallBitmap);
        }
    }
    private void doLargeImageWorks() {
        if(uri != null) {
            largeUri = uri;
            largeBitmap = makeLargeBitmap();
            Toast.makeText(this, getResources().getString(R.string.toast_story_large_image_selected), Toast.LENGTH_LONG).show();
        }
    }
    public void showDemo(View view) {
        String link = linkEditText.getText().toString();
        link = LinkHandler.verifyUrl(link);
        String phone = phoneEditText.getText().toString();
        if(largeBitmap != null) {
            Intent intent = new Intent(this, StoryActivity.class);
            intent.putExtra("imageUriStr", largeUri.toString());
            intent.putExtra("link", link);
            intent.putExtra("phone", phone);
            intent.putExtra("activity_number", StoryAdapter.DEMO_ACTIVITY);
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_story_no_large_image_yet), Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Bitmap makeThumbnail() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // makeReadableTime thumbnail for showing in images list -
            final int THUMBSIZE = 150;
            return ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Nullable
    private Bitmap makeLargeBitmap() {
        int LARGE_IMAGE_SIZE = 1000;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            double currentHeight = bitmap.getHeight();
            double currentWidth = bitmap.getWidth();

            int newHeight;
            int newWidth;

            if(currentHeight > currentWidth && currentHeight > LARGE_IMAGE_SIZE) {
                newHeight = LARGE_IMAGE_SIZE;
                newWidth = (int) ((currentWidth / currentHeight) * newHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            } else if(currentWidth > LARGE_IMAGE_SIZE) {
                newWidth = LARGE_IMAGE_SIZE;
                newHeight = (int) ((currentHeight / currentWidth) * newWidth);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            }

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void createStory(final View view) {
        if(smallBitmap != null && largeBitmap != null) {
            view.setVisibility(View.GONE);
            progressDialog.setMessage(getResources().getString(R.string.sending_story_images));
            progressDialog.setCancelable(false);
            progressDialog.show();
            RequestHandler.makeRequest(this, "POST", Urls.CREATE_STORY, createStoryParams(), new RequestCallback() {
                @Override
                public void onNoInternetAccess() {
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.no_internet_access);
                    AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            createStory(view);
                        }

                        @Override
                        public void cancel() {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onSuccess(String response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        boolean error = jsonObject.getBoolean("error");
                        if(error) {
                            view.setVisibility(View.VISIBLE);
                            AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                                @Override
                                public void tryAgain() {
                                    createStory(view);
                                }

                                @Override
                                public void cancel() {
                                    view.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            int storyId = Integer.valueOf(jsonObject.getString("story_id"));
                            Intent paymentIntent = new Intent(CreateStoryActivity.this, PaymentStoryActivity.class);
                            paymentIntent.putExtra("story_id", storyId);
                            paymentIntent.putExtra("link", LinkHandler.verifyUrl(linkEditText.getText().toString()));
                            paymentIntent.putExtra("phone", phoneEditText.getText().toString());
                            startActivity(paymentIntent);
                            Toast.makeText(CreateStoryActivity.this, getResources().getString(R.string.story_draft_saved), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String message = getResources().getString(R.string.problem_parse_response);
                        AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                createStory(view);
                            }

                            @Override
                            public void cancel() {
                            }
                        });
                    }
                }

                @Override
                public void onRequestError() {
                    view.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.problem_request);
                    AlertHandler.error(CreateStoryActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            createStory(view);
                        }

                        @Override
                        public void cancel() {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_story_not_selected_image), Toast.LENGTH_LONG).show();
        }
    }
    private Map<String, String> createStoryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("small_image", imageToString(smallBitmap, 100));
        params.put("large_image", imageToString(largeBitmap, 100));
        params.put("link", LinkHandler.verifyUrl(linkEditText.getText().toString()));
        params.put("phone", phoneEditText.getText().toString());
        return params;
    }
    private String imageToString(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
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
