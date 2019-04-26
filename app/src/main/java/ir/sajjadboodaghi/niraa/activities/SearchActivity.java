package ir.sajjadboodaghi.niraa.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Item;

public class SearchActivity extends AppCompatActivity {


    private final static float ITEM_WIDTH_IN_INCH = 1.75f;
    private final static float ITEM_HEIGHT_IN_INCH = 0.75f;

    EditText searchEditText;
    boolean loading = false;
    private boolean noMoreItems = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    ProgressBar progressBar;
    RecyclerView searchRecyclerView;
    List<Item> items;
    ItemAdapter adapter;
    int columnCount;
    int rowCount;
    int itemsCount;
    String searchString;
    ImageView clearImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.requestFocus();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        itemsCountCalculator();
        prepareForLoadItems();

        clearImageView = (ImageView) findViewById(R.id.clearImageView);

        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    newSearch();
                    return true;
                }
                return false;
            }
        });


    }

    private void itemsCountCalculator() {
        // we use this variable for getting width, height,...
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float widthInInch = metrics.widthPixels / (float) metrics.densityDpi;
        columnCount = (int) (widthInInch / ITEM_WIDTH_IN_INCH);

        float heightInInch = metrics.heightPixels / (float) metrics.densityDpi;
        rowCount = (int) (heightInInch / ITEM_HEIGHT_IN_INCH) + 1;

        itemsCount = columnCount * rowCount;
    }
    private void prepareForLoadItems() {
        searchRecyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setHasFixedSize(true);
        final GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, columnCount);
        searchRecyclerView.setLayoutManager(mGridLayoutManager);
        items = new ArrayList<>();
        adapter = new ItemAdapter(items, SearchActivity.this);
        searchRecyclerView.setAdapter(adapter);

        searchRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mGridLayoutManager.getChildCount();
                    totalItemCount = mGridLayoutManager.getItemCount();
                    pastVisibleItems = mGridLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount)
                    {
                        if(!noMoreItems && !loading)
                            search(items.get(items.size()-1).getTimestamp());
                    }
                }
            }
        });
    }

    private void search(final String last_timestamp) {
        searchString = searchEditText.getText().toString();
        if(searchString.trim().length() < 3) {
            Toast.makeText(this, getString(R.string.too_short_search_statement), Toast.LENGTH_SHORT).show();
            return;
        }

        if(loading) {
            return;
        }
        loading = true;

        progressBar.setVisibility(View.VISIBLE);

        RequestHandler.makeRequest(this, "POST", Urls.SEARCH, searchParams(last_timestamp), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                loading = false;
                progressBar.setVisibility(View.GONE);
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(SearchActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        search(last_timestamp);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                progressBar.setVisibility(View.GONE);
                loading = false;
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int nowTimestamp = jsonObject.getInt("now_timestamp");
                    SharedPrefManager.getInstance(SearchActivity.this).setNowTimestamp(nowTimestamp);

                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String message = jsonObject.getString("message");
                        AlertHandler.error(SearchActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                search(last_timestamp);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                    } else {
                        JSONArray newItems = jsonObject.getJSONArray("items");
                        if(newItems.length() > 0) {
                            for(int i = 0; i < newItems.length(); i++) {
                                JSONObject object = newItems.getJSONObject(i);
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
                            if(last_timestamp.equals("first")) {
                                Toast.makeText(SearchActivity.this, getResources().getString(R.string.search_not_found), Toast.LENGTH_SHORT).show();
                            } else {
                                noMoreItems = true;
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(SearchActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            search(last_timestamp);
                        }

                        @Override
                        public void cancel() {

                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                progressBar.setVisibility(View.GONE);
                loading = false;
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(SearchActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        search(last_timestamp);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        });
    }

    private Map<String, String> searchParams(String timestamp) {
        Map<String, String> params = new HashMap<>();
        params.put("search_string", searchString);
        params.put("last_timestamp", timestamp);
        params.put("items_count", String.valueOf(itemsCount));
        return params;
    }

    public void searchButtonHandle(View view) {
        newSearch();
    }

    private void newSearch() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        noMoreItems = false;
        items.clear();
        adapter.notifyDataSetChanged();
        search("first");
    }

    public void back(View view) {
        finish();
    }
    public void clearSearchEditBox(View view) {
        searchEditText.setText("");
        searchEditText.requestFocus();
    }



}
