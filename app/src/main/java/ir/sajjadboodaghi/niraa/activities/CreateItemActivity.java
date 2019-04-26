package ir.sajjadboodaghi.niraa.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

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
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertYesOrNoCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.adapters.CreateItemImagesAdapter;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.handlers.TimeAndString;

public class CreateItemActivity extends AppCompatActivity {

    EditText titleEditText;
    EditText descriptionEditText;
    EditText priceEditText;
    Button sendNewItemButton;

    EditText telegramEditText;

    List<Uri> uris = new ArrayList<>();
    List<Bitmap> thumbnails = new ArrayList<>();
    CreateItemImagesAdapter createItemImagesAdapter;
    RecyclerView imagesRecyclerView;

    JSONArray jsonCategoryArray;
    Spinner categorySpinner;
    Spinner subCategorySpinner;

    ProgressDialog progressDialog1;
    ProgressDialog progressDialog2;

    private Uri mCropImageUri = Uri.EMPTY;

    Map<String, String> newItemFields = new HashMap<>();

    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.createItemToolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            Toast.makeText(this, getResources().getString(R.string.toast_first_login), Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(this).setTargetActivity("CreateItemActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // for avoiding showing keyboard at start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog1 = new ProgressDialog(this);
        progressDialog2 = new ProgressDialog(this);

        if(!RequestHandler.isDeviceConnected(this)) {
            String message = getResources().getString(R.string.no_internet_access);
            AlertHandler.notice(CreateItemActivity.this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {
                    finish();
                }
            });
            return;
        }

        sendNewItemButton = (Button) findViewById(R.id.sendNewItemButton);
        sendNewItemButton.setVisibility(View.VISIBLE);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        telegramEditText = (EditText) findViewById(R.id.telegramEditText);

        canCreateMoreItem();

    }

    private void canCreateMoreItem() {
        progressDialog1.setMessage(getResources().getString(R.string.connecting_to_server));
        progressDialog1.setCancelable(false);
        progressDialog1.show();


        RequestHandler.makeRequest(this, "POST", Urls.CAN_CREATE_MORE_ITEM, canCreateMoreItemParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog1.dismiss();
                finish();
            }

            @Override
            public void onSuccess(String response) {
                progressDialog1.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String errorMessage = jsonObject.getString("message");
                        AlertHandler.error(CreateItemActivity.this, errorMessage, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                canCreateMoreItem();
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                    }

                    boolean hasTooManyWaitingItems = jsonObject.getBoolean("hasTooManyWaitingItems");
                    String message = jsonObject.getString("message");
                    // if user had an item which was waiting for verification
                    if(hasTooManyWaitingItems) {
                        AlertHandler.notice(CreateItemActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                startActivity(new Intent(CreateItemActivity.this, UserItemsActivity.class));
                                finish();
                            }
                        });
                    } else {
                        prepareCategorySpinners();

                        prepareImagesRecyclerView();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            canCreateMoreItem();
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
                progressDialog1.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        canCreateMoreItem();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    private Map<String, String> canCreateMoreItemParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phoneNumber", SharedPrefManager.getInstance(CreateItemActivity.this).getPhoneNumber());
        return params;
    }

    private void prepareImagesRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imagesRecyclerView = (RecyclerView) findViewById(R.id.imagesRecyclerView);
        imagesRecyclerView.setLayoutManager(layoutManager);

        createItemImagesAdapter = new CreateItemImagesAdapter(thumbnails, uris);
        imagesRecyclerView.setAdapter(createItemImagesAdapter);
    }
    private void prepareCategorySpinners() {
        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        subCategorySpinner = (Spinner) findViewById(R.id.subCategorySpinner);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONArray jsonSubCategoryArray = jsonCategoryArray.getJSONObject(position).getJSONArray("subcats");
                    ArrayList<String> subCategoryList = new ArrayList<>();
                    for(int i = 0; i < jsonSubCategoryArray.length(); i++) {
                        subCategoryList.add(jsonSubCategoryArray.getJSONObject(i).getString("subcat_name"));
                    }

                    ArrayAdapter<String> subCategoryAdapter = new ArrayAdapter<>(CreateItemActivity.this, android.R.layout.simple_spinner_dropdown_item, subCategoryList);
                    subCategorySpinner.setAdapter(subCategoryAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_getting_subcategories);
                    AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            loadCategory();
                        }

                        @Override
                        public void cancel() {
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadCategory();
    }

    private void loadCategory() {
        progressDialog1.setMessage(getResources().getString(R.string.receiving_categories));
        progressDialog1.setCancelable(false);
        progressDialog1.show();
        RequestHandler.makeRequest(this, "GET", Urls.GET_CATEGORY, null, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog1.dismiss();
                finish();
            }

            @Override
            public void onSuccess(String response) {
                progressDialog1.dismiss();
                try {
                    jsonCategoryArray = new JSONArray(response);
                    List<String> categoryList = new ArrayList<>();
                    for(int i = 0; i < jsonCategoryArray.length(); i++) {
                        categoryList.add(jsonCategoryArray.getJSONObject(i).getString("catname"));
                    }

                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(CreateItemActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
                    categorySpinner.setAdapter(categoryAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_getting_categories);
                    AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            loadCategory();
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
                progressDialog1.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadCategory();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    public void addImage(View v) {
        int imageCountLimit = 10;
        if(createItemImagesAdapter.getItemCount() < imageCountLimit) {
            CropImage.startPickImageActivity(this);
        } else {
            String message = getResources().getString(R.string.cannot_add_more_image);
            AlertHandler.notice(CreateItemActivity.this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {
                    // comeback to create item page and do nothing
                }
            });
        }
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
                Uri resultUri = result.getUri();

                uris.add(resultUri);
                showThumbnailInRecyclerView(resultUri);

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
        CropImage.activity(imageUri).start(this);
    }

    private void showThumbnailInRecyclerView(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // makeReadableTime thumbnail for showing in images list -
            final int THUMBSIZE = 100;
            Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);
            thumbnails.add(thumbBitmap);
            // - makeReadableTime thumbnail for showing in images list

            createItemImagesAdapter.notifyDataSetChanged();
            imagesRecyclerView.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void sendNewItem(final View v) {
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        priceEditText = (EditText) findViewById(R.id.priceEditText);

        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        String message = "";
        if(title.replace(" ", "").length() == 0) { message += getResources().getString(R.string.title_field_is_empty); }
        if(title.length() < 4 && title.length() > 0) { message += "\n" + getResources().getString(R.string.title_is_too_short); }
        if(description.replace(" ", "").length() == 0) { message += "\n" + getResources().getString(R.string.description_field_is_empty); }
        if(description.length() < 4 && description.length() > 0) { message += "\n" + getResources().getString(R.string.description_is_too_short); }

        if(message.length() > 0) {
            AlertHandler.notice(this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {}
            });
            return;
        }

        sendNewItemButton.setVisibility(View.GONE);

        prepareNewItemFields();
    }
    private void prepareNewItemFields() {
        newItemFields.put("phoneNumber", SharedPrefManager.getInstance(this).getPhoneNumber());

        String telegramId = telegramEditText.getText().toString().replace("@", "").replace(" ", "");
        newItemFields.put("telegramId", telegramId);

        newItemFields.put("token", SharedPrefManager.getInstance(this).getToken());
        newItemFields.put("title", titleEditText.getText().toString());
        newItemFields.put("description", descriptionEditText.getText().toString());
        newItemFields.put("price", TimeAndString.changeEnglishDigitsToFarsiDigits(priceEditText.getText().toString()));

        RadioGroup cityRadioGroup = (RadioGroup) findViewById(R.id.cityRadioGroup);
        RadioButton cityRadioButton = (RadioButton) findViewById(cityRadioGroup.getCheckedRadioButtonId());
        newItemFields.put("place", cityRadioButton.getText().toString());

        newItemFields.put("subcat_name", subCategorySpinner.getSelectedItem().toString());

        int subcatId = 0;
        try {
            subcatId = jsonCategoryArray
                    .getJSONObject(categorySpinner.getSelectedItemPosition())
                    .getJSONArray("subcats")
                    .getJSONObject(subCategorySpinner.getSelectedItemPosition())
                    .getInt("subcat_id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        newItemFields.put("subcat_id", String.valueOf(subcatId));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progressDialog2.setMessage(getResources().getString(R.string.preparing_content));
                progressDialog2.setCancelable(false);
                progressDialog2.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                int imageCount = createItemImagesAdapter.getItemCount();
                uris = createItemImagesAdapter.getUris();
                newItemFields.put("imageCount", String.valueOf(imageCount));
                for(int i = 0; i < imageCount; i++) {
                    newItemFields.put("image_" + (i+1), imageToString(makeBitmap(uris.get(i)), 80));
                }
                if(imageCount > 0) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uris.get(0));
                        final int THUMBSIZE = 150;
                        Bitmap imageThumbnail = ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);
                        newItemFields.put("image_thumbnail", imageToString(imageThumbnail, 80));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                progressDialog2.dismiss();
                sendRequest();
            }
        }.execute();
    }

    @Nullable
    private Bitmap makeBitmap(Uri uri) {
        int LARGER_SIDE_SIZE = 800;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            double currentHeight = bitmap.getHeight();
            double currentWidth = bitmap.getWidth();

            int newHeight;
            int newWidth;

            if(currentHeight > currentWidth && currentHeight > LARGER_SIDE_SIZE) {
                newHeight = LARGER_SIDE_SIZE;
                newWidth = (int) ((currentWidth / currentHeight) * newHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            } else if(currentWidth > LARGER_SIDE_SIZE) {
                newWidth = LARGER_SIDE_SIZE;
                newHeight = (int) ((currentHeight / currentWidth) * newWidth);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            }

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String imageToString(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
    }

    private void sendRequest() {
        progressDialog1.setMessage(getResources().getString(R.string.sending_new_item));
        progressDialog1.setCancelable(false);
        progressDialog1.show();

        RequestHandler.makeRequest(this, "POST", Urls.CREATE_ITEM, newItemFields, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog1.dismiss();
                sendNewItemButton.setVisibility(View.VISIBLE);

                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendRequest();
                    }

                    @Override
                    public void cancel() {
                        // comeback to create item page and do nothing
                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                progressDialog1.dismiss();
                sendNewItemButton.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");

                    Boolean error = jsonObject.getBoolean("error");
                    String errorName = jsonObject.getString("error_name");
                    if(error) {
                        if(errorName.equals("has_waiting_item")) {
                            startActivity(new Intent(CreateItemActivity.this, UserItemsActivity.class));
                            finish();
                        } else {
                            AlertHandler.notice(CreateItemActivity.this, message, new AlertNoticeCallback() {
                                @Override
                                public void okay() {

                                }
                            });
                        }

                    } else {
                        AlertHandler.notice(CreateItemActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                startActivity(new Intent(CreateItemActivity.this, UserItemsActivity.class));
                                finish();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            sendRequest();
                        }

                        @Override
                        public void cancel() {
                            // comeback to create item page and do nothing
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                progressDialog1.dismiss();
                sendNewItemButton.setVisibility(View.VISIBLE);

                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(CreateItemActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendRequest();
                    }

                    @Override
                    public void cancel() {
                        // comeback to create item page and do nothing
                    }
                });
            }
        });
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
            String message = getResources().getString(R.string.want_leave_create_item);
            AlertHandler.yesOrNo(this, message, new AlertYesOrNoCallback() {
                @Override
                public void yes() {
                    finish();
                }

                @Override
                public void no() {

                }
            });
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        String message = getResources().getString(R.string.want_leave_create_item);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_dialog_title_danger));
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.alert_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.alert_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialogBuilder.show();
    }
}