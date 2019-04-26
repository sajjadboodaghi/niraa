package ir.sajjadboodaghi.niraa.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.adapters.ItemAdapter;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.models.Item;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Story;
import ir.sajjadboodaghi.niraa.adapters.StoryAdapter;
import ir.sajjadboodaghi.niraa.Urls;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;
    Toolbar homeToolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;

    RecyclerView recyclerView;
    RecyclerView.Adapter itemAdapter;
    List<Item> items;
    List<Story> storiesList;
    StoryAdapter storyAdapter;

    private final static float ITEM_WIDTH_IN_INCH = 1.75f;
    private final static float ITEM_HEIGHT_IN_INCH = 0.75f;

    public static boolean needs_to_refresh_stories = false;
    public static boolean needs_to_refresh_items = false;

    private boolean loading = false;
    private boolean noMoreItems = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    ProgressBar progressBar;

    int columnCount;
    int rowCount;
    int itemsCount;
    RecyclerView storyRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        homeToolbarSetup();
        drawerLayoutSetup();
        navigationViewSetup();
        columnCountAndItemsCountSetup();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            storyRecyclerViewSetup();
        }

        itemsRecyclerViewSetup();
        swipeRefreshLayoutSetup();

        refresh();

    }
    @Override
    protected void onResume() {
        super.onResume();
        getUserNotifications();

        // after delete story or publish story needs_to_refresh_stories will be true
        if(needs_to_refresh_stories) {
            loadStories();
            needs_to_refresh_stories = false;
        }


        // we do this to load last items and also load last stories
        // after delete item needs_to_refresh_items will be true
        if(needs_to_refresh_items || SharedPrefManager.getInstance(HomeActivity.this).isLastRefreshTimeVeryOld()) {
            refresh();
            needs_to_refresh_items = false;
        }

    }

    private void refresh() {
        noMoreItems = false;
        items.clear();
        itemAdapter.notifyDataSetChanged();
        loadItems("first");

        // stories will be refresh inside loadItems method
    }

    private void loadItems(final String last_timestamp) {
        if (loading) {
            return;
        }
        loading = true;

        progressBar.setVisibility(View.VISIBLE);
        String url = Urls.GET_ITEMS + "?last_timestamp=" + last_timestamp + "&items_count=" + itemsCount;
        RequestHandler.makeRequest(this, "GET", url, null, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressBar.setVisibility(View.GONE);
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(HomeActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loading = false;
                        loadItems(last_timestamp);
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                loading = false;
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int nowTimestamp = jsonObject.getInt("now_timestamp");
                    SharedPrefManager.getInstance(HomeActivity.this).setNowTimestamp(nowTimestamp);

                    Boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        String message = jsonObject.getString("message");
                        AlertHandler.error(HomeActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                loadItems(last_timestamp);
                            }

                            @Override
                            public void cancel() {
                                finish();
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
                        }

                        itemAdapter.notifyDataSetChanged();


                    } else {
                        noMoreItems = true;
                        Toast.makeText(HomeActivity.this, getResources().getString(R.string.toast_no_more_items), Toast.LENGTH_SHORT).show();
                    }

                    if(last_timestamp.equals("first")) {
                        // load stories
                        loadStories();

                        // save last refresh time to refresh later at onResume method
                        SharedPrefManager.getInstance(HomeActivity.this).updateLastRefreshTime();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(HomeActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            loadItems(last_timestamp);
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
                loading = false;
                progressBar.setVisibility(View.GONE);
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.error(HomeActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        loadItems(last_timestamp);
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });
    }
    private void getUserNotifications() {
        if(SharedPrefManager.getInstance(this).isLogin()) {
            Map<String, String> params = new HashMap<>();
            params.put("phone_number", SharedPrefManager.getInstance(HomeActivity.this).getPhoneNumber());

            RequestHandler.makeRequest(this, "POST", Urls.GET_USER_NOTIFICATIONS, params, new RequestCallback() {
                @Override
                public void onNoInternetAccess() {
                    // do nothing
                }

                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        int nowTimestamp = jsonObject.getInt("now_timestamp");
                        SharedPrefManager.getInstance(HomeActivity.this).setNowTimestamp(nowTimestamp);

                        JSONArray jsonResponse = jsonObject.getJSONArray("notifications");
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                        alertDialogBuilder.setTitle("پیغام");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setNeutralButton("متوجه شدم", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        for(int i = 0; i < jsonResponse.length(); i++) {
                            alertDialogBuilder.setMessage(jsonResponse.getJSONObject(i).getString("message"));
                            alertDialogBuilder.show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        // do nothing
                    }
                }

                @Override
                public void onRequestError() {
                    // do nothing
                }
            });

        }
    }
    private void homeToolbarSetup() {
        homeToolbar = (Toolbar) findViewById(R.id.homeToolbar);
        setSupportActionBar(homeToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void drawerLayoutSetup() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.top_menu_drawer_toggle_show, R.string.top_menu_drawer_toggle_hide);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
    }
    private void navigationViewSetup() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.navigationCreateItem:
                        startActivity(new Intent(HomeActivity.this, CreateItemActivity.class));
                        break;
                    case R.id.navigationUserItems:
                        startActivity(new Intent(HomeActivity.this, UserItemsActivity.class));
                        break;
                    case R.id.navigationBookmarks:
                        startActivity(new Intent(HomeActivity.this, BookmarksActivity.class));
                        break;
                    case R.id.navigationUserAccount:
                        startActivity(new Intent(HomeActivity.this, UserAccountActivity.class));
                        break;
                    case R.id.navigationCreateStory:
                        startActivity(new Intent(HomeActivity.this, CreateStoryActivity.class));
                        break;
                    case R.id.navigationContactUs:
                        startActivity(new Intent(HomeActivity.this, ContactUsActivity.class));
                        break;
                }
                return false;
            }
        });
    }


    private void itemsRecyclerViewSetup() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        final GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, columnCount);
        recyclerView.setLayoutManager(mGridLayoutManager);
        items = new ArrayList<>();
        itemAdapter = new ItemAdapter(items, HomeActivity.this);
        recyclerView.setAdapter(itemAdapter);

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
    private void columnCountAndItemsCountSetup() {
        // we use this variable for getting width, height,...
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float widthInInch = metrics.widthPixels / (float) metrics.densityDpi;
        columnCount = (int) (widthInInch / ITEM_WIDTH_IN_INCH);

        float heightInInch = metrics.heightPixels / (float) metrics.densityDpi;
        rowCount = (int) (heightInInch / ITEM_HEIGHT_IN_INCH) + 1;

        itemsCount = columnCount * rowCount;
    }
    private void swipeRefreshLayoutSetup() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }
    private void storyRecyclerViewSetup() {
        storiesList = new ArrayList<>();
        storyAdapter = new StoryAdapter(HomeActivity.this, storiesList, StoryAdapter.HOME_ACTIVITY);
        storyRecyclerView = (RecyclerView) findViewById(R.id.storyRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        storyRecyclerView.setAdapter(storyAdapter);
    }

    private void loadStories() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return;

        storyRecyclerView.setVisibility(View.GONE);
        RequestHandler.makeRequest(this, "GET", Urls.GET_STORIES, null, new RequestCallback() {
            @Override
            public void onNoInternetAccess() {

            }

            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int nowTimestamp = jsonObject.getInt("now_timestamp");
                    SharedPrefManager.getInstance(HomeActivity.this).setNowTimestamp(nowTimestamp);

                    boolean error = jsonObject.getBoolean("error");
                    if(error) {
                        return;
                    }

                    storiesList.clear();

                    Story story;
                    JSONArray storiesArray = jsonObject.getJSONArray("stories");
                    if(storiesArray.length() > 0) {
                        storyRecyclerView.setVisibility(View.VISIBLE);
                        JSONObject storyObject;
                        for(int i = 0; i < storiesArray.length(); i++) {
                            storyObject = storiesArray.getJSONObject(i);
                            story = new Story(storyObject.getInt("id"), storyObject.getString("phone_number"), storyObject.getString("link"), storyObject.getString("phone"));
                            storiesList.add(story);
                        }
                    }
                    storyAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestError() {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.filterMenu) {
            startActivity(new Intent(this, FilterActivity.class));
            return false;
        }

        if(item.getItemId() == R.id.searchMenu) {
            startActivity(new Intent(this, SearchActivity.class));
            return false;
        }

        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
