package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.adapters.ItemAdapter;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Item;

public class BookmarksActivity extends AppCompatActivity {

    private final static float ITEM_WIDTH_IN_INCH = 1.75f;
    List<Item> items;
    ItemAdapter adapter;
    RecyclerView recyclerView;

    public static boolean needs_to_refresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.bookmarksToolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            Toast.makeText(this, getResources().getString(R.string.toast_first_login), Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(this).setTargetActivity("BookmarksActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        needs_to_refresh = true;

        prepareRecyclerView();

    }


    @Override
    protected void onResume() {
        super.onResume();

        if(needs_to_refresh) {
            items.clear();
            adapter.notifyDataSetChanged();
            getBookmarks();
        }
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
        adapter = new ItemAdapter(items, BookmarksActivity.this);
        recyclerView.setAdapter(adapter);
    }


    private void getBookmarks() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.please_wait_a_moment));
        pd.setCancelable(false);
        pd.show();

        RequestHandler.makeRequest(this, "POST", Urls.GET_BOOKMARKS, getBookmarksParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                pd.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(BookmarksActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getBookmarks();
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

                    int nowTimestamp = jsonObject.getInt("now_timestamp");
                    SharedPrefManager.getInstance(BookmarksActivity.this).setNowTimestamp(nowTimestamp);

                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String errorMessage = jsonObject.getString("message");
                        AlertHandler.error(BookmarksActivity.this, errorMessage, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                getBookmarks();
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                    } else {
                        needs_to_refresh = false;
                        JSONArray bookmarks = jsonObject.getJSONArray("bookmarks");
                        if(bookmarks.length() > 0) {
                            for (int i = 0; i < bookmarks.length(); i++) {
                                JSONObject object = bookmarks.getJSONObject(i);
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
                            AlertHandler.notice(BookmarksActivity.this, message, new AlertNoticeCallback() {
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
                    AlertHandler.error(BookmarksActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            getBookmarks();
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
                AlertHandler.error(BookmarksActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        getBookmarks();
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }

    private Map<String, String> getBookmarksParams() {
        Map<String, String> params = new HashMap<>();
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
