package ir.sajjadboodaghi.niraa.handlers;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.Urls;
import ir.sajjadboodaghi.niraa.activities.LoginActivity;

/**
 * Created by Sajjad on 02/12/2018.
 */

public class SharedPrefManager {
    private static SharedPrefManager mInstance;
    private static Context mContext;

    private static final String SHARED_PREF_NAME = "myPreferences";

    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_TARGET_ACTIVITY = "targetActivityName";


    private static final String KEY_TOKEN = "token";
    private static final String KEY_VERIFY_CODE = "verificationCode";
    private static final String KEY_LAST_REFRESH_TIME = "lastRefreshTime";
    private static final String KEY_NOW_TIMESTAMP = "nowTimestamp";

    private static final String SALT1 = "060509bd09a726f18ccd7f8574b52353";
    private static final String SALT2 = "254bb964f62b2507c1ba812d28df226d";

    private SharedPrefManager(Context context) {
        mContext = context;
    }
    public static synchronized SharedPrefManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }
    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public boolean clear() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    public void setTargetActivity(String activityName) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TARGET_ACTIVITY, activityName);
        editor.apply();
    }

    public String getTargetActivity() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TARGET_ACTIVITY, null);
    }

    public boolean isLogin() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.getString(KEY_TOKEN, null) != null) {
            return true;
        }
        return false;
    }
    public boolean setToken(String token) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        token = md5(md5(token + SALT1) + SALT2);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
        return true;
    }
    public String getToken() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    public void setVerifyCode(String code, String phoneNumber) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_VERIFY_CODE, code);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }
    public String getVerifyCode() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_VERIFY_CODE, null);
    }
    public boolean hasVerifyCode() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.getString(KEY_VERIFY_CODE, null) != null) {
            return true;
        }
        return false;
    }
    public boolean isVerifyCodeValid(String userEnteredCode) {
        return getVerifyCode().equals(userEnteredCode);
    }

    public void setNowTimestamp(int nowTimestamp) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOW_TIMESTAMP, nowTimestamp);
        editor.apply();
    }
    public int getNowTimestamp() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_NOW_TIMESTAMP, 0);
    }

    public String getPhoneNumber() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null);
    }

    public String userImageUrlMaker(String userImageName) {
        return Urls.USERS_IMAGES_DIR + userImageName;
    }

    public void updateLastRefreshTime() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // get current time
        Long currentTime = Calendar.getInstance().getTime().getTime();
        editor.putLong(KEY_LAST_REFRESH_TIME, currentTime);
        editor.apply();
    }
    public boolean isLastRefreshTimeVeryOld() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Long lastRefreshTime = sharedPreferences.getLong(KEY_LAST_REFRESH_TIME, 0);
        Long currentTime = Calendar.getInstance().getTime().getTime();
        if(lastRefreshTime < (currentTime - (1000 * 60 * 60 * 3))) {
            return true;
        } else {
            return false;
        }
    }
}
