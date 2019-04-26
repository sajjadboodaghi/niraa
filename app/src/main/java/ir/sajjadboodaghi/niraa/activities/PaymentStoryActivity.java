package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.adapters.StoryAdapter;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertPayCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertYesOrNoCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.models.Story;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.util.IabHelper;
import ir.sajjadboodaghi.niraa.util.IabResult;
import ir.sajjadboodaghi.niraa.util.Inventory;
import ir.sajjadboodaghi.niraa.util.Purchase;

public class PaymentStoryActivity extends AppCompatActivity {

    // Debug tag, for logging
    static final String TAG = "Niraa.Payment.Story";
    // SKUs for our products: the premium upgrade (non-consumable)
    static final String SKU_PREMIUM = "advertisement_cost";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10011;
    // The helper object
    IabHelper mHelper;
    boolean ready = false;
    Button purchaseButton;
    ProgressDialog progressDialog;
    int storyId;
    String link;
    String phone;
    LinearLayout mainLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_story);

        Toolbar toolbar = (Toolbar) findViewById(R.id.paymentStoryToolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        purchaseButton = (Button) findViewById(R.id.purchaseButton);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        prepareIabHelper();
        storyId = getIntent().getIntExtra("story_id", 0);
        link = getIntent().getStringExtra("link");
        phone = getIntent().getStringExtra("phone");
        setupUserStoryRecyclerView();
    }

    private void setupUserStoryRecyclerView() {
        RecyclerView storyRecyclerView = (RecyclerView) findViewById(R.id.storyRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        List<Story> userStoryList = new ArrayList<>();
        Story story = new Story(storyId, SharedPrefManager.getInstance(this).getPhoneNumber(), link, phone);
        userStoryList.add(story);
        StoryAdapter storyAdapter = new StoryAdapter(this, userStoryList, StoryAdapter.PAYMENT_ACTIVITY);
        storyRecyclerView.setAdapter(storyAdapter);
    }

    private void prepareIabHelper() {
        progressDialog.setMessage(getResources().getString(R.string.connecting_to_payment_system));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String base64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDhCardKVkBBcgJs9hrzMfcMGsPulYG0KOXEpnbfnKnokW1ixGs1C7q/jJunrfK1gcR2eVB2uC9QCltulb9IOwHmdhoKMFexBIv44V6gCLWuRMONAICCRPFdCNiIaDvi0dMn+oXvqfTT5/bm08MUSt8eO+73r4qsoJtaA700MyQPCFXR9DEGD2rzntjVlOQ1hvK0L7NrCS3ATVwjj3tsSNb5ilLjsiNp1Et4XrP6acCAwEAAQ==";
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                Log.d(TAG, "Setup finished.");
                if (!result.isSuccess()) {
                    progressDialog.dismiss();

                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    String message = getResources().getString(R.string.problem_with_connecting_to_payment_system);
                    AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
                        @Override
                        public void okay() {
                            finish();
                        }
                    });
                    return;
                }
                // Hooray, IAB is fully set up!
                getAdvertisementPrice();
            }
        });
    }

    private void getAdvertisementPrice() {
        List additionalSkuList = new ArrayList();
        additionalSkuList.add(SKU_PREMIUM);
        mHelper.queryInventoryAsync(true, additionalSkuList, new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                progressDialog.dismiss();
                if (result.isFailure()) {
                    // handle error
                    String message = getResources().getString(R.string.problem_with_connecting_to_payment_system);
                    AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
                        @Override
                        public void okay() {
                            finish();
                        }
                    });
                    return;
                }

                String advertisementPrice = inventory.getSkuDetails(SKU_PREMIUM).getPrice();
                purchaseButton.setText("مبلغ قابل پرداخت " + advertisementPrice);
                ready = true;
            }
        });

    }

    public void purchase(View v) {
        if(ready) {
            mainLinearLayout.setVisibility(View.GONE);
            mHelper.launchPurchaseFlow(PaymentStoryActivity.this, SKU_PREMIUM, RC_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, final Purchase purchase) {
                    if (result.isFailure()) {
                        mainLinearLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Error purchasing: " + result);
                        Toast.makeText(PaymentStoryActivity.this, getResources().getString(R.string.payment_cancled), Toast.LENGTH_SHORT).show();
                    } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                        // give user access to premium content and update the UI
                        consume(purchase);
                    }
                }
            }, "story_id: " + storyId);
        }
    }

    private void consume(Purchase purchase) {
        // برای اینکه کاربر فقط یکبار بتواند از کالای فروشی استفاده کند
        // باید بعد از خرید آن کالا را مصرف کنیم
        // در غیر اینصورت کاربر با یکبار خرید محصول می تواند چندبار از آن استفاده کند
        mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(final Purchase purchase, IabResult result) {
                if (result.isSuccess()) {
                    publishStory();
                    Log.d(TAG, "NATIJE masraf: " + result.getMessage() + result.getResponse());
                } else {
                    String message = getResources().getString(R.string.problem_with_consuming);
                    AlertHandler.pay(PaymentStoryActivity.this, message, new AlertPayCallback() {
                        @Override
                        public void tryAgain() {
                            consume(purchase);
                        }

                        @Override
                        public void pay() {
                            publishProblemAlert();
                        }
                    });
                }

            }
        });
    }

    private void publishProblemAlert() {
        String message = "پرداخت انجام شد ولی متأسفانه مشکلی در یکی از مراحل پایانی به وجود آمد! برای پیگیری از طریق بخش پشتیبانی اقدام نمایید.\nشماره پیگیری: " + storyId;
        AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
            @Override
            public void okay() {
                finish();
            }
        });
    }

    private void publishStory() {
        String message = getResources().getString(R.string.send_publish_story_request);
        progressDialog.setMessage(message);
        progressDialog.show();
        RequestHandler.makeRequest(this, "POST", Urls.PUBLISH_STORY, publishStoryParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.pay(PaymentStoryActivity.this, message, new AlertPayCallback() {
                    @Override
                    public void tryAgain() {
                        publishStory();
                    }

                    @Override
                    public void pay() {
                        publishProblemAlert();
                    }
                });
            }

            @Override
            public void onSuccess(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    String message = jsonObject.getString("message");
                    if(error) {
                        AlertHandler.pay(PaymentStoryActivity.this, message, new AlertPayCallback() {
                            @Override
                            public void tryAgain() {
                                publishStory();
                            }

                            @Override
                            public void pay() {
                                publishProblemAlert();
                            }
                        });
                    } else {
                        AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                HomeActivity.needs_to_refresh_stories = true;
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.pay(PaymentStoryActivity.this, message, new AlertPayCallback() {
                        @Override
                        public void tryAgain() {
                            publishStory();
                        }

                        @Override
                        public void pay() {
                            publishProblemAlert();
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.pay(PaymentStoryActivity.this, message, new AlertPayCallback() {
                    @Override
                    public void tryAgain() {
                        publishStory();
                    }

                    @Override
                    public void pay() {
                        publishProblemAlert();
                    }
                });
            }
        });
    }

    private Map<String, String> publishStoryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("story_id", String.valueOf(storyId));
        return params;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    @Override
    public void onDestroy() {
        //از سرویس در زمان اتمام عمر activity قطع شوید
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public void deleteStory() {
        String message = getResources().getString(R.string.want_delete_draft_story);
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
                AlertHandler.error(PaymentStoryActivity.this, message, new AlertErrorCallback() {
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
                        AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                finish();
                            }
                        });
                    } else {
                        AlertHandler.notice(PaymentStoryActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                HomeActivity.needs_to_refresh_stories = true;
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(PaymentStoryActivity.this, message, new AlertErrorCallback() {
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
                AlertHandler.error(PaymentStoryActivity.this, message, new AlertErrorCallback() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.payment, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menuBack) {
            finish();
        }

        if(item.getItemId() == R.id.menuDeleteStory) {
            deleteStory();
        }
        return true;
    }
}
