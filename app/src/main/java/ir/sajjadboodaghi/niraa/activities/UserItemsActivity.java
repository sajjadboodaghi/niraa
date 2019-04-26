package ir.sajjadboodaghi.niraa.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.models.Item;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.adapters.UserItemAdapter;

public class UserItemsActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;

    ArrayList<Item> items;
    UserItemAdapter adapter;
    public static Activity me;

    private final static float ITEM_WIDTH_IN_INCH = 1.75f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_items);

        toolbar = (Toolbar) findViewById(R.id.userItemsToolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            Toast.makeText(this, getResources().getString(R.string.toast_first_login), Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(this).setTargetActivity("UserItemsActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        prepareRecyclerView();

        loadUserItems();
        me = this;
    }

    private void prepareRecyclerView() {
        // we use this variable for getting width, height,...
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float widthInInch = metrics.widthPixels / (float) metrics.densityDpi;
        int columnCount = (int) (widthInInch / ITEM_WIDTH_IN_INCH);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        final GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, columnCount);
        recyclerView.setLayoutManager(mGridLayoutManager);
        items = new ArrayList<>();
        adapter = new UserItemAdapter(items, UserItemsActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private void loadUserItems() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
        progressDialog.show();

        Map<String, String> params = new HashMap<>();
        params.put("phoneNumber", SharedPrefManager.getInstance(UserItemsActivity.this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(UserItemsActivity.this).getToken());

        RequestHandler.makeRequest(this, "POST", Urls.GET_USER_ITEMS, params, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadUserItems();
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
                    JSONObject jsonObject = new JSONObject(response);

                    int nowTimestamp = jsonObject.getInt("now_timestamp");
                    SharedPrefManager.getInstance(UserItemsActivity.this).setNowTimestamp(nowTimestamp);

                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String errorMessage = jsonObject.getString("message");
                        AlertHandler.error(UserItemsActivity.this, errorMessage, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                loadUserItems();
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                    } else {
                        JSONArray userItems = jsonObject.getJSONArray("userItems");
                        if(userItems.length() > 0) {
                            for (int i = 0; i < userItems.length(); i++) {
                                JSONObject object = userItems.getJSONObject(i);
                                Item item = new Item(
                                        object.getInt("id"),
                                        object.getString("phone_number"),
                                        object.getString("telegram_id"),
                                        object.getString("title"),
                                        object.getString("description"),
                                        object.getString("price"),
                                        object.getString("place"),
                                        object.getString("subcat_name"),
                                        object.getInt("subcat_id"),
                                        object.getString("shamsi"),
                                        object.getString("timestamp"),
                                        object.getInt("image_count"),
                                        object.getInt("verified")
                                );

                                items.add(item);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            String message = getResources().getString(R.string.not_found);
                            AlertHandler.notice(UserItemsActivity.this, message, new AlertNoticeCallback() {
                                @Override
                                public void okay() {
                                    finish();
                                }
                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            loadUserItems();
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
                AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadUserItems();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }

    public void deleteItem(final View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserItemsActivity.this);
        alertDialogBuilder.setTitle("هشدار");
        alertDialogBuilder.setMessage("آیا از حذف این آگهی اطمینان دارید؟");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("بله، حذف شود", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final ProgressDialog progressDialog = new ProgressDialog(UserItemsActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
                progressDialog.setCancelable(false);
                progressDialog.show();
                RequestHandler.makeRequest(UserItemsActivity.this, "POST", Urls.DELETE_ITEM, deleteItemParams(String.valueOf(v.getContentDescription())), new RequestCallback() {
                    @Override
                    public void onNoInternetAccess() {
                        progressDialog.dismiss();
                        String message = getResources().getString(R.string.no_internet_access);
                        AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                deleteItem(v);
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
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean error = jsonObject.getBoolean("error");
                            if(error) {
                                String errorMessage = jsonObject.getString("message");
                                AlertHandler.error(UserItemsActivity.this, errorMessage, new AlertErrorCallback() {
                                    @Override
                                    public void tryAgain() {
                                        deleteItem(v);
                                    }

                                    @Override
                                    public void cancel() {

                                    }
                                });
                                return;
                            }

                            String message = jsonObject.getString("message");
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserItemsActivity.this);
                            alertDialogBuilder.setTitle("پیغام");
                            alertDialogBuilder.setMessage(message);
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setNeutralButton("متوجه شدم" , new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(UserItemsActivity.this, UserItemsActivity.class));
                                    HomeActivity.needs_to_refresh_items = true;
                                    finish();
                                }
                            });
                            alertDialogBuilder.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            String message = getResources().getString(R.string.problem_parse_response);
                            AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                                @Override
                                public void tryAgain() {
                                    deleteItem(v);
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
                        AlertHandler.error(UserItemsActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                deleteItem(v);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    }
                });
            }
        });

        alertDialogBuilder.setNegativeButton("خیر، بازگشت", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuilder.show();

    }
    private Map<String, String> deleteItemParams(String itemId) {
        Map<String, String> params = new HashMap<>();
        params.put("phoneNumber", SharedPrefManager.getInstance(UserItemsActivity.this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(UserItemsActivity.this).getToken());
        params.put("itemId", itemId);
        return params;
    }

    public void showWaitingMessage(View view) {
        String message = getResources().getString(R.string.waiting_item_message);
        AlertHandler.notice(this, message, new AlertNoticeCallback() {
            @Override
            public void okay() {

            }
        });
    }

    public void showVerifiedMessage(View view) {
        String message = getResources().getString(R.string.verified_item_message);
        AlertHandler.notice(this, message, new AlertNoticeCallback() {
            @Override
            public void okay() {

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
            finish();
        }
        return true;
    }
}
