package ir.sajjadboodaghi.niraa.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import ir.sajjadboodaghi.niraa.handlers.TimeAndString;

public class VerifyActivity extends AppCompatActivity {

    TextView descriptionTextView;
    EditText verificationEditText;
    String phoneNumber;
    Toolbar verifyToolbar;
    ProgressDialog progressDialog1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        verifyToolbar = (Toolbar) findViewById(R.id.verifyToolbar);
        setSupportActionBar(verifyToolbar);

        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        verificationEditText = (EditText) findViewById(R.id.verificationEditText);

        phoneNumber = SharedPrefManager.getInstance(this).getPhoneNumber();
        descriptionTextView.setText("رمز پیامک شده به شماره " + phoneNumber + " را در قسمت زیر وارد نمایید.");

        progressDialog1 = new ProgressDialog(this);

    }

    public void changePhoneNumber(View v) {
        // this will clear sharedPreferences data
        SharedPrefManager.getInstance(VerifyActivity.this).clear();

        startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
    }

    public void sendAgain(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyActivity.this);
        builder.setTitle("توجه");
        builder.setMessage("آیا می\u200Cخواهید دوباره به شماره «" + phoneNumber + "» رمز را پیامک کنیم؟");
        builder.setNeutralButton("بله", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                progressDialog1.setMessage(getResources().getString(R.string.connecting_to_server));
                progressDialog1.setCancelable(false);
                progressDialog1.show();

                Map<String, String> params = new HashMap<>();
                params.put("phoneNumber", phoneNumber);

                RequestHandler.makeRequest(VerifyActivity.this, "POST", Urls.SEND_VERIFICATION_CODE, params, new RequestCallback() {
                    @Override
                    public void onNoInternetAccess() {
                        progressDialog1.dismiss();
                        String message = getResources().getString(R.string.no_internet_access);
                        AlertHandler.notice(VerifyActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String response) {
                        progressDialog1.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean isSuccessful = jsonObject.getBoolean("IsSuccessful");
                            if (!isSuccessful) {
                                String errorMessage = jsonObject.getString("Message");
                                AlertHandler.notice(VerifyActivity.this, errorMessage, new AlertNoticeCallback() {
                                    @Override
                                    public void okay() {

                                    }
                                });
                            }

                            Toast.makeText(VerifyActivity.this, "تا لحظاتی دیگر رمز برای شما ارسال می\u200Cشود", Toast.LENGTH_SHORT).show();
                            String verificationCode = jsonObject.getString("VerificationCode");
                            SharedPrefManager.getInstance(VerifyActivity.this).setVerifyCode(verificationCode, phoneNumber);
                            startActivity(new Intent(VerifyActivity.this, VerifyActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String message = getResources().getString(R.string.problem_parse_response);
                            AlertHandler.notice(VerifyActivity.this, message, new AlertNoticeCallback() {
                                @Override
                                public void okay() {

                                }
                            });
                        }
                    }

                    @Override
                    public void onRequestError() {
                        progressDialog1.dismiss();
                        String message = getResources().getString(R.string.problem_request);
                        AlertHandler.notice(VerifyActivity.this, message, new AlertNoticeCallback() {
                            @Override
                            public void okay() {

                            }
                        });
                    }
                });
            }
        });

        builder.setNegativeButton("خیر", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    public void verify(final View v) {
        String userEnteredCode = verificationEditText.getText().toString();
        userEnteredCode = TimeAndString.changeFarsiDigitsToEnglishDigits(userEnteredCode);
        if(userEnteredCode.equals("")) {
            String message = getResources().getString(R.string.enter_verification_code);
            AlertHandler.notice(this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {
                    return;
                }
            });
            return;
        }

        if(SharedPrefManager.getInstance(VerifyActivity.this).isVerifyCodeValid(userEnteredCode)) {
            final ProgressDialog progressDialog = new ProgressDialog(VerifyActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.connecting_to_server));
            progressDialog.show();

            Map<String, String> params = new HashMap<>();
            params.put("phoneNumber", phoneNumber);

            RequestHandler.makeRequest(this, "POST", Urls.REGISTER_USER, params, new RequestCallback() {
                @Override
                public void onNoInternetAccess() {
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.no_internet_access);
                    AlertHandler.error(VerifyActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            verify(v);
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
                        Boolean error = jsonObject.getBoolean("error");
                        if(error) {
                            String errorMessage = jsonObject.getString("message");
                            AlertHandler.error(VerifyActivity.this, errorMessage, new AlertErrorCallback() {
                                @Override
                                public void tryAgain() {
                                    verify(v);
                                }

                                @Override
                                public void cancel() {
                                    finish();
                                }
                            });
                            return;
                        }

                        String token = jsonObject.getString("token");
                        String message = jsonObject.getString("message");
                        SharedPrefManager.getInstance(VerifyActivity.this).setToken(token);
                        Toast.makeText(VerifyActivity.this, message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(VerifyActivity.this, UserAccountActivity.class));
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String message = getResources().getString(R.string.problem_parse_response);
                        AlertHandler.error(VerifyActivity.this, message, new AlertErrorCallback() {
                            @Override
                            public void tryAgain() {
                                verify(v);
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
                    AlertHandler.error(VerifyActivity.this, message, new AlertErrorCallback() {
                        @Override
                        public void tryAgain() {
                            verify(v);
                        }

                        @Override
                        public void cancel() {
                            finish();
                        }
                    });
                }
            });

        } else {
            String message = "رمز وارد شده درست نیست!";
            AlertHandler.notice(this, message, new AlertNoticeCallback() {
                @Override
                public void okay() {

                }
            });
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
