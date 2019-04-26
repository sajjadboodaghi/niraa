package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Item;
import ir.sajjadboodaghi.niraa.adapters.ItemAdapter;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.Urls;

public class FilterActivity extends AppCompatActivity {

    Spinner categorySpinner;
    Spinner subCategorySpinner;
    Spinner citySpinner;
    JSONArray jsonCategoryArray;
    Button findItemsByCategoryButton;
    CheckBox onlyItemsWithImageCheckBox;

    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    private final static float ITEM_WIDTH_IN_INCH = 1.75f;
    private final static float ITEM_HEIGHT_IN_INCH = 0.75f;

    private boolean loading = false;
    private boolean noMoreItems = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    ArrayList<Item> items;
    ItemAdapter adapter;
    String last_timestamp;
    int itemsCount;
    LinearLayout optionsLinearLayout;
    Button changeFilterButton;
    int columnCount;
    int rowCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.filterToolbar);
        setSupportActionBar(toolbar);

        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        subCategorySpinner = (Spinner) findViewById(R.id.subCategorySpinner);
        citySpinner = (Spinner) findViewById(R.id.citySpinner);
        prepareCitySpinner();

        onlyItemsWithImageCheckBox = (CheckBox) findViewById(R.id.onlyItemsWithImageCheckBox);
        progressDialog = new ProgressDialog(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        optionsLinearLayout = (LinearLayout) findViewById(R.id.optionsLinearLayout);
        changeFilterButton = (Button) findViewById(R.id.changeFilterButton);

        loadCategory();
        prepareForLoadItems();

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSubCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findItemsByCategoryButton = (Button) findViewById(R.id.findItemsByCategoryButton);
    }
    private void prepareForLoadItems() {
        // we use this variable for getting width, height,...
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float widthInInch = metrics.widthPixels / (float) metrics.densityDpi;
        columnCount = (int) (widthInInch / ITEM_WIDTH_IN_INCH);

        float heightInInch = metrics.heightPixels / (float) metrics.densityDpi;
        rowCount = (int) (heightInInch / ITEM_HEIGHT_IN_INCH) + 1;

        itemsCount = columnCount * rowCount;

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        final GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, columnCount);
        recyclerView.setLayoutManager(mGridLayoutManager);
        items = new ArrayList<>();
        adapter = new ItemAdapter(items, FilterActivity.this);
        recyclerView.setAdapter(adapter);

        last_timestamp = "first";

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            loadItems(items.get(items.size()-1).getTimestamp());
                    }
                }
            }
        });
    }
    private void prepareCitySpinner() {
        List<String> cityList = new ArrayList<>();
        cityList.add("تمام شهرها");
        cityList.add("رامسر");
        cityList.add("تنکابن");
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(FilterActivity.this, android.R.layout.simple_spinner_dropdown_item, cityList);
        citySpinner.setAdapter(cityAdapter);
    }

    private void loadCategory() {
        progressDialog.setMessage(getResources().getString(R.string.receiving_categories));
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestHandler.makeRequest(this, "GET", Urls.GET_CATEGORY_FILTER, null, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
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

            @Override
            public void onSuccess(String response) {
                progressDialog.dismiss();
                try {
                    jsonCategoryArray = new JSONArray(response);
                    List<String> categoryList = new ArrayList<>();
                    for(int i = 0; i < jsonCategoryArray.length(); i++) {
                        categoryList.add(jsonCategoryArray.getJSONObject(i).getString("catname"));
                    }

                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(FilterActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
                    categorySpinner.setAdapter(categoryAdapter);
                    findItemsByCategoryButton.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_getting_categories);
                    AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
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
                progressDialog.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
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

    private void setSubCategory(final int position) {
        try {
            JSONArray jsonSubCategoryArray = jsonCategoryArray.getJSONObject(position).getJSONArray("subcats");
            List<String> subCategoryList = new ArrayList<>();
            for(int i = 0; i < jsonSubCategoryArray.length(); i++) {
                subCategoryList.add(jsonSubCategoryArray.getJSONObject(i).getString("subcat_name"));
            }
            ArrayAdapter<String> subCategoryAdapter = new ArrayAdapter<>(FilterActivity.this, android.R.layout.simple_spinner_dropdown_item, subCategoryList);
            subCategorySpinner.setAdapter(subCategoryAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
            String message = getResources().getString(R.string.problem_getting_subcategories);
            AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
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

    public void findItemsByCategory(View v) {
        noMoreItems = false;
        items.clear();
        adapter.notifyDataSetChanged();
        loadItems(last_timestamp);
    }

    private void loadItems(final String last_timestamp) {
        if(loading) {
           return;
        }
        loading = true;

        String subcatId = "";
        try {
            subcatId = jsonCategoryArray
                      .getJSONObject(categorySpinner.getSelectedItemPosition())
                      .getJSONArray("subcats")
                      .getJSONObject(subCategorySpinner.getSelectedItemPosition())
                      .getString("subcat_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(subcatId.isEmpty()) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("last_timestamp", last_timestamp);
        params.put("items_count", String.valueOf(itemsCount));
        params.put("subcat_id", subcatId);
        params.put("city", citySpinner.getSelectedItem().toString());
        if(onlyItemsWithImageCheckBox.isChecked()) {
            params.put("image_count", "1");
        } else {
            params.put("image_count", "0");
        }
        progressBar.setVisibility(View.VISIBLE);

        RequestHandler.makeRequest(this, "POST", Urls.GET_FILTERED_ITEMS, params, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                loading = false;
                progressBar.setVisibility(View.GONE);
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadItems(last_timestamp);
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
                    SharedPrefManager.getInstance(FilterActivity.this).setNowTimestamp(nowTimestamp);

                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String message = jsonObject.getString("message");
                        AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                loadItems(last_timestamp);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                        return;
                    }

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

                            optionsLinearLayout.setVisibility(View.GONE);
                            changeFilterButton.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        if(last_timestamp.equals("first")) {
                            Toast.makeText(FilterActivity.this, getResources().getString(R.string.no_filtered_items), Toast.LENGTH_SHORT).show();
                        } else {
                            noMoreItems = true;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            loadItems(last_timestamp);
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
                AlertHandler.error(FilterActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadItems(last_timestamp);
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        });

    }

    public void changeFilter(View view) {
        items.clear();
        adapter.notifyDataSetChanged();

        optionsLinearLayout.setVisibility(View.VISIBLE);
        changeFilterButton.setVisibility(View.GONE);

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
