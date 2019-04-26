package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;

public class ContactUsActivity extends AppCompatActivity {

    String description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        Toolbar toolbar = (Toolbar) findViewById(R.id.contactUsToolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLogin()) {
            Toast.makeText(this, getResources().getString(R.string.toast_first_login), Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(this).setTargetActivity("ContactUsActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // for avoiding showing keyboard at start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView appVersionTv = (TextView) findViewById(R.id.appVersion);
        appVersionTv.setText("نسخه ".concat(getAppVersionName()));
    }

    private String getAppVersionName() {
        String versionName;
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "";
        }

        return versionName;
    }

    public void sendSuggest(final View v) {
        TextView bugDescriptionEditText = (EditText) findViewById(R.id.bugDescriptionEditText);

        String message = "";
        description = bugDescriptionEditText.getText().toString();
        if(description.replace(" ", "").length() == 0) { message += "بخش توضیحات را تکمیل نمایید!\n"; }
        if(description.length() < 4 && description.length() > 0) { message += "توضیحات بیشتری بنویسید!\n"; }

        if(message.length() > 0) {
            AlertHandler.notice(this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {

                }
            });
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestHandler.makeRequest(this, "POST", Urls.SAVE_SUGGEST, saveSuggestParams(), new RequestCallback() {
            @Override
            public void onNoInternetAccess() {
                progressDialog.dismiss();
                String message = getResources().getString(R.string.no_internet_access);
                AlertHandler.error(ContactUsActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendSuggest(v);
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
                    String message = jsonObject.getString("message");
                    if(error) {
                        AlertHandler.error(ContactUsActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                sendSuggest(v);
                            }

                            @Override
                            public void cancel() {
                                finish();
                            }
                        });
                        return;
                    }

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactUsActivity.this);
                    alertDialogBuilder.setTitle("پیغام");
                    alertDialogBuilder.setMessage(message);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setNeutralButton("متوجه شدم", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    alertDialogBuilder.show();

                } catch (JSONException e) {
                    String message = getResources().getString(R.string.problem_parse_response);
                    AlertHandler.error(ContactUsActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            sendSuggest(v);
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
                AlertHandler.error(ContactUsActivity.this, message, new AlertErrorCallback() {
                    @Override
                    public void tryAgain() {
                        sendSuggest(v);
                    }

                    @Override
                    public void cancel() {
                        finish();
                    }
                });
            }
        });

    }

    private Map<String, String> saveSuggestParams() {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", SharedPrefManager.getInstance(this).getPhoneNumber());
        params.put("token", SharedPrefManager.getInstance(this).getToken());
        params.put("niraa_version", getNiraaVersion(this));
        params.put("android_version", getAndroidVersion());
        params.put("description", description);
        return params;
    }

    public static String getNiraaVersion(Context context) {
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionNumber = pinfo.versionCode;
        String versionName = pinfo.versionName;

        return versionNumber + " | " + versionName;
    }

    public static String getAndroidVersion() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        switch(apiLevel) {
            case(15):
                return "15 | 4.0 – 4.0.4 | Ice Cream Sandwich";
            case (16):
                return "16 | 4.1 – 4.3.1 | Jelly Bean";
            case (17):
                return "17 | 4.1 – 4.3.1 | Jelly Bean";
            case (18):
                return "18 | 4.1 – 4.3.1 | Jelly Bean";
            case (19):
                return "19 | 4.4 – 4.4.4 | KitKat";
            case (20):
                return "20 | 4.4 – 4.4.4 | KitKat";
            case (21):
                return "21 | 5.0 – 5.1.1 | Lollipop";
            case (22):
                return "22 | 5.0 – 5.1.1 | Lollipop";
            case (23):
                return "23 | 6.0 – 6.0.1 | Marshmallow";
            case (24):
                return "24 | 7.0 – 7.1.2 | Nougat";
            case (25):
                return "25 | 7.0 – 7.1.2 | Nougat";
            case (26):
                return "26 | 8.0 – 8.1 | Oreo";
            case (27):
                return "27 | 8.0 – 8.1 | Oreo";
            case (28):
                return "28 | 9.0 | Pie";
            default:
                return apiLevel + " | ? | unknown";
        }
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
