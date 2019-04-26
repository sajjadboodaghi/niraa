package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertPayCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.TimeAndString;
import ir.sajjadboodaghi.niraa.util.IabHelper;
import ir.sajjadboodaghi.niraa.util.IabResult;
import ir.sajjadboodaghi.niraa.util.Inventory;
import ir.sajjadboodaghi.niraa.util.Purchase;

public class PaymentUpdateActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Button purchaseButton;
    ScrollView mainLinearLayout;
    int itemId;

    // The helper object
    IabHelper mHelper;

    // Debug tag, for logging
    static final String TAG = "Niraa.Payment.Update";

    // SKUs for our products: the premium upgrade (non-consumable)
    static final String SKU_PREMIUM = "update_item_cost";

    boolean ready = false;

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10012;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_update);

        Toolbar toolbar = (Toolbar) findViewById(R.id.paymentUpdateToolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        purchaseButton = (Button) findViewById(R.id.purchaseButton);

        mainLinearLayout = (ScrollView) findViewById(R.id.mainLinearLayout);

        itemId = getIntent().getIntExtra("itemId", 0);

        prepareIabHelper();
        showItem();
    }

    private void showItem() {
        TextView textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewTitle.setText(getIntent().getStringExtra("itemTitle"));

        TextView textViewPrice = (TextView) findViewById(R.id.textViewPrice);
        textViewPrice.setText(getIntent().getStringExtra("itemPrice"));

        TextView textViewPlace = (TextView) findViewById(R.id.textViewPlace);
        textViewPlace.setText(getIntent().getStringExtra("itemPlace"));

        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        String time = TimeAndString.makeReadableTime(this, Integer.valueOf(getIntent().getStringExtra("itemTimestamp")));
        textViewTime.setText(time);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int imageCount = getIntent().getIntExtra("itemImageCount", 0);
        if(imageCount > 0) {
            Picasso.with(this).load(Urls.IMAGES_BASE_URL + "item_" + itemId + "_thumbnail.jpg")
                    .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.image_loading, null))
                    .error(ResourcesCompat.getDrawable(getResources(), R.drawable.image_loading, null))
                    .into(imageView);
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.image_default, null));
        }
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
                    AlertHandler.notice(PaymentUpdateActivity.this, message, new AlertNoticeCallback() {
                        @Override
                        public void okay() {
                            finish();
                        }
                    });
                    return;
                }
                // Hooray, IAB is fully set up!
                getUpdateItemPrice();
            }
        });
    }

    private void getUpdateItemPrice() {
        List additionalSkuList = new ArrayList();
        additionalSkuList.add(SKU_PREMIUM);
        mHelper.queryInventoryAsync(true, additionalSkuList, new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                progressDialog.dismiss();
                if (result.isFailure()) {
                    // handle error
                    String message = getResources().getString(R.string.problem_with_connecting_to_payment_system);
                    AlertHandler.notice(PaymentUpdateActivity.this, message, new AlertNoticeCallback() {
                        @Override
                        public void okay() {
                            finish();
                        }
                    });
                    return;
                }

                String advertisementPrice = inventory.getSkuDetails(SKU_PREMIUM).getPrice();
                purchaseButton.setText("تازه کردن آگهی با پرداخت " + advertisementPrice);
                ready = true;
            }
        });

    }

    public void purchase(View v) {
        if(ready) {
            mainLinearLayout.setVisibility(View.GONE);
            mHelper.launchPurchaseFlow(PaymentUpdateActivity.this, SKU_PREMIUM, RC_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, final Purchase purchase) {
                    if (result.isFailure()) {
                        mainLinearLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Error purchasing: " + result);
                        Toast.makeText(PaymentUpdateActivity.this, getResources().getString(R.string.payment_cancled), Toast.LENGTH_SHORT).show();
                    } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                        // give user access to premium content and update the UI
                        consume(purchase);
                    }
                }
            }, "item_id: " + itemId);
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
                    updateItem();
                    Log.d(TAG, "NATIJE masraf: " + result.getMessage() + result.getResponse());
                } else {
                    String message = getResources().getString(R.string.problem_with_consuming);
                    AlertHandler.pay(PaymentUpdateActivity.this, message, new AlertPayCallback() {
                        @Override
                        public void tryAgain() {
                            consume(purchase);
                        }

                        @Override
                        public void pay() {
                            updateProblemAlert();
                        }
                    });
                }

            }
        });
    }

    private void updateProblemAlert() {
        String message = "پرداخت انجام شد ولی متأسفانه مشکلی در یکی از مراحل پایانی به وجود آمد! برای پیگیری از طریق بخش پشتیبانی اقدام نمایید.\nشماره پیگیری: " + itemId;
        AlertHandler.notice(PaymentUpdateActivity.this, message, new AlertNoticeCallback() {
            @Override
            public void okay() {
                finish();
            }
        });
    }

    private void updateItem() {
        String message = getResources().getString(R.string.send_update_item_request);
        progressDialog.setMessage(message);
        progressDialog.show();
        RequestHandler.makeRequest(this, "POST", Urls.UPDATE_ITEM, updateItemParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.pay(PaymentUpdateActivity.this, message, new AlertPayCallback() {
                    @Override
                    public void tryAgain() {updateItem();
                    }

                    @Override
                    public void pay() {
                        updateProblemAlert();
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
                        AlertHandler.pay(PaymentUpdateActivity.this, message, new AlertPayCallback() {
                            @Override
                            public void tryAgain() {
                                updateItem();
                            }

                            @Override
                            public void pay() {
                                updateProblemAlert();
                            }
                        });
                    } else {
                        AlertHandler.notice(PaymentUpdateActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                                HomeActivity.needs_to_refresh_items = true;
                                startActivity(new Intent(PaymentUpdateActivity.this, HomeActivity.class));
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.pay(PaymentUpdateActivity.this, message, new AlertPayCallback() {
                        @Override
                        public void tryAgain() {
                            updateItem();
                        }

                        @Override
                        public void pay() {
                            updateProblemAlert();
                        }
                    });
                }
            }

            @Override
            public void onRequestError() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.problem_request);
                AlertHandler.pay(PaymentUpdateActivity.this, message, new AlertPayCallback() {
                    @Override
                    public void tryAgain() {
                        updateItem();
                    }

                    @Override
                    public void pay() {
                        updateProblemAlert();
                    }
                });
            }
        });
    }

    private Map<String, String> updateItemParams() {
        Map<String, String> params = new HashMap<>();
        params.put("item_id", String.valueOf(itemId));
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


    @Override
    public void onBackPressed() {
        startActivity(new Intent(PaymentUpdateActivity.this, UserItemsActivity.class));
        finish();
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
            startActivity(new Intent(PaymentUpdateActivity.this, UserItemsActivity.class));
            finish();
        }
        return true;
    }

}
