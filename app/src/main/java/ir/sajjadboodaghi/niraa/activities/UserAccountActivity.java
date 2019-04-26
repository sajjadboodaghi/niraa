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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertYesOrNoCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;

public class UserAccountActivity extends AppCompatActivity {

    Toolbar userAccountToolbar;
    TextView userPhoneNumberTextView;
    EditText userNameEditText;
    String name;
    ImageView removeImageView;
    RoundedImageView userImageView;
    Uri mCropImageUri = Uri.EMPTY;
    Uri uri = Uri.EMPTY;
    Bitmap userImageBitmap;
    ImageView changeUserImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            startActivity(new Intent(this, LoginActivity.class));
            SharedPrefManager.getInstance(this).setTargetActivity("UserAccountActivity");
            finish();
            return;
        }

        // for avoiding showing keyboard at start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        userAccountToolbar = (Toolbar) findViewById(R.id.userAccountToolbar);
        setSupportActionBar(userAccountToolbar);
        userPhoneNumberTextView = (TextView) findViewById(R.id.userPhoneNumberTextView);
        String userPhoneNumber = SharedPrefManager.getInstance(this).getPhoneNumber();
        userPhoneNumberTextView.setText(userPhoneNumber);

        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        userImageView = (RoundedImageView) findViewById(R.id.userImageCircleImageView);
        changeUserImageView = (ImageView) findViewById(R.id.changeUserImageView);
        removeImageView = (ImageView) findViewById(R.id.removeImageView);

        getUserImageName();
    }

    private void getUserImageName() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.please_wait_a_moment));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_IMAGE_NAME, getUserImageNameParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                String message = getString(R.string.no_internet_access);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getUserImageName();
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
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String message = jsonObject.getString("message");
                        AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                getUserImageName();
                            }

                            @Override
                            public void cancel() {
                                SharedPrefManager.getInstance(UserAccountActivity.this).clear();
                                finish();
                            }
                        });
                    } else {
                        String userName = jsonObject.getString("user_name");
                        userNameEditText.setText(userName);

                        String imageName = jsonObject.getString("image_name");
                        if(!imageName.equals("default.jpg")) {
                            Picasso.with(UserAccountActivity.this)
                                    .load(SharedPrefManager.getInstance(UserAccountActivity.this).userImageUrlMaker(imageName))
                                    .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.image_default_user, null))
                                    .error(ResourcesCompat.getDrawable(getResources(), R.drawable.image_loading, null))
                                    .into(userImageView);

                            removeImageView.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    String message = getString(R.string.problem_parse_response);
                    AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            getUserImageName();
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
                String message = getString(R.string.problem_request);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getUserImageName();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    private Map<String, String> getUserImageNameParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        return params;
    }

    public void selectUserImage(View v) {
        changeUserImageView.setVisibility(View.GONE);
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

                doImageWorks();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                changeUserImageView.setVisibility(View.VISIBLE);
            }
        }

        if ((requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) && resultCode != Activity.RESULT_OK) {
            changeUserImageView.setVisibility(View.VISIBLE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, getResources().getString(R.string.required_permission_denied), Toast.LENGTH_LONG).show();
                changeUserImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .start(this);
    }

    private void doImageWorks() {
        if(uri != null) {
            userImageBitmap = makeThumbnail();
            sendUserImage();
        } else {
            changeUserImageView.setVisibility(View.VISIBLE);
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

    private void sendUserImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.sending_user_image));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.SAVE_USER_IMAGE, sendUserImageParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                changeUserImageView.setVisibility(View.VISIBLE);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendUserImage();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                pd.dismiss();
                changeUserImageView.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                sendUserImage();
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    } else {
                        String imageName = jsonObject.getString("image_name");
                        Picasso.with(UserAccountActivity.this).load(SharedPrefManager.getInstance(UserAccountActivity.this).userImageUrlMaker(imageName))
                                .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.image_default_user, null))
                                .error(ResourcesCompat.getDrawable(getResources(), R.drawable.image_default_user, null))
                                .into(userImageView);
                        AlertHandler.notice(UserAccountActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                            }

                        });
                        if (!imageName.equals("default.jpg")) {
                            removeImageView.setVisibility(View.VISIBLE);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            sendUserImage();
                        }

                        @Override
                        public void cancel() {
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                pd.dismiss();
                changeUserImageView.setVisibility(View.VISIBLE);
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendUserImage();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        });
    }

    private Map<String, String> sendUserImageParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("user_image", imageToString(userImageBitmap, 100));
        return params;
    }

    private String imageToString(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
    }

    public void removeUserImage(final View v) {
        removeImageView.setVisibility(View.GONE);
        String question = getResources().getString(R.string.do_you_want_remove_user_image);
        AlertHandler.yesOrNo(this, question, new AlertYesOrNoCallback() {
            @Override
            public void yes() {
                removeUserImageRequest();
            }

            @Override
            public void no() {
                removeImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void removeUserImageRequest() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.connecting_to_server));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.REMOVE_USER_IMAGE, removeUserImageParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                removeImageView.setVisibility(View.VISIBLE);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        removeUserImageRequest();
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
                    String message = jsonObject.getString("message");
                    boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                removeUserImageRequest();
                            }

                            @Override
                            public void cancel() {
                                removeImageView.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        userImageView.setImageDrawable(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.image_default_user, null));

                        AlertHandler.notice(UserAccountActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                            }

                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    removeImageView.setVisibility(View.VISIBLE);
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            removeUserImageRequest();
                        }

                        @Override
                        public void cancel() {
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                pd.dismiss();
                removeImageView.setVisibility(View.VISIBLE);
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        removeUserImageRequest();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        });
    }

    private Map<String, String> removeUserImageParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        return params;
    }

    public void logout(View v) {
        String question = getResources().getString(R.string.do_you_want_to_logout);
        AlertHandler.yesOrNo(this, question, new AlertYesOrNoCallback() {
            @Override
            public void yes() {
                SharedPrefManager.getInstance(UserAccountActivity.this).clear();
                Toast.makeText(UserAccountActivity.this, getResources().getString(R.string.toast_logged_out), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void no() {

            }
        });
    }

    public void saveUserName(final View saveButton) {
        saveButton.setEnabled(false);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.connecting_to_server));
        pd.setCancelable(false);
        pd.show();
        RequestHandler.makeRequest(this, "POST", Urls.SAVE_USER_NAME, getParamsForSaveUserName(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                saveButton.setEnabled(true);
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        saveUserName(saveButton);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                pd.dismiss();
                saveButton.setEnabled(true);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    String message = jsonObject.getString("message");
                    if(!error) {
                        AlertHandler.notice(UserAccountActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {

                            }

                        });
                    } else {
                        AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                saveUserName(saveButton);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            saveUserName(saveButton);
                        }

                        @Override
                        public void cancel() {

                        }
                    });
                }

            }

            @Override
            public void onRequestError() {
                pd.dismiss();
                saveButton.setEnabled(true);
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(UserAccountActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        saveUserName(saveButton);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        });
    }

    private Map<String, String> getParamsForSaveUserName() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        name =  userNameEditText.getText().toString();
        params.put("name", name);
        return params;
    }

    private void redirectToTargetActivity() {
        switch(SharedPrefManager.getInstance(this).getTargetActivity()) {
            case "BookmarksActivity":
                SharedPrefManager.getInstance(this).setTargetActivity("");
                startActivity(new Intent(this, BookmarksActivity.class));
                break;
            case "CreateItemActivity":
                SharedPrefManager.getInstance(this).setTargetActivity("");
                startActivity(new Intent(this, CreateItemActivity.class));
                break;
            case "CreateStoryActivity":
                SharedPrefManager.getInstance(this).setTargetActivity("");
                startActivity(new Intent(this, CreateStoryActivity.class));
                break;
            case "UserItemsActivity":
                SharedPrefManager.getInstance(this).setTargetActivity("");
                startActivity(new Intent(this, UserItemsActivity.class));
                break;
            case "ContactUsActivity":
                SharedPrefManager.getInstance(this).setTargetActivity("");
                startActivity(new Intent(this, ContactUsActivity.class));
                break;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        redirectToTargetActivity();
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
            redirectToTargetActivity();
        }
        return true;
    }
}
