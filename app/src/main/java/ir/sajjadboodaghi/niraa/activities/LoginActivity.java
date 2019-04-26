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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.AlertHandler;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;
import ir.sajjadboodaghi.niraa.handlers.RequestHandler;
import ir.sajjadboodaghi.niraa.handlers.SharedPrefManager;
import ir.sajjadboodaghi.niraa.Urls;

public class LoginActivity extends AppCompatActivity {


    Button sendPassButton;
    EditText phoneNumberEditText;
    Toolbar loginToolbar;

    ProgressDialog progressDialog1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(SharedPrefManager.getInstance(this).hasVerifyCode()){
            startActivity(new Intent(this, VerifyActivity.class));
            return;
        }

        loginToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(loginToolbar);

        sendPassButton = (Button) findViewById(R.id.sendPassButton);
        phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        progressDialog1 = new ProgressDialog(this);

        sendPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber = phoneNumberEditText.getText().toString();
                if(phoneNumber.trim().length() != 11 || !phoneNumber.substring(0,2).equals("09")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("خطا");
                    builder.setMessage("شماره همراه وارد شده معتبر نیست!");
                    builder.setNeutralButton("متوجه شدم", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("توجه");
                    builder.setMessage("ما می\u200Cخواهیم یک رمز به شماره «" + phoneNumber + "»  پیامک کنیم. آیا این شماره به شما تعلق دارد؟");
                    builder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            progressDialog1.setMessage(getResources().getString(R.string.connecting_to_server));
                            progressDialog1.setCancelable(false);
                            progressDialog1.show();

                            Map<String, String> params = new HashMap<>();
                            params.put("phoneNumber", phoneNumber);

                            RequestHandler.makeRequest(LoginActivity.this, "POST", Urls.SEND_VERIFICATION_CODE, params, new RequestCallback() {
                                @Override
                                public void onNoInternetAccess() {
                                    progressDialog1.dismiss();
                                    String message = getResources().getString(R.string.no_internet_access);
                                    AlertHandler.notice(LoginActivity.this, message, new AlertNoticeCallback() {
                                        @Override
                                        public void okay() {}
                                    });
                                }

                                @Override
                                public void onSuccess(String response) {
                                    progressDialog1.dismiss();
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        Boolean isSuccessful = jsonObject.getBoolean("IsSuccessful");
                                        if(!isSuccessful) {
                                            String errorMessage = jsonObject.getString("Message");
                                            AlertHandler.notice(LoginActivity.this, errorMessage, new AlertNoticeCallback() {
                                                @Override
                                                public void okay() {

                                                }
                                            });
                                        }

                                        Toast.makeText(LoginActivity.this, "تا لحظاتی دیگر رمز برای شما ارسال می\u200Cشود", Toast.LENGTH_SHORT).show();
                                        String verificationCode = jsonObject.getString("VerificationCode");
                                        SharedPrefManager.getInstance(LoginActivity.this).setVerifyCode(verificationCode, phoneNumber);
                                        startActivity(new Intent(LoginActivity.this, VerifyActivity.class));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        String message = getResources().getString(R.string.problem_parse_response);
                                        AlertHandler.notice(LoginActivity.this, message, new AlertNoticeCallback() {
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
                                    AlertHandler.notice(LoginActivity.this, message, new AlertNoticeCallback() {
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
