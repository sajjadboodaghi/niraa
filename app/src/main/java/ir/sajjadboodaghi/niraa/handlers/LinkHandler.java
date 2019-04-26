package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import ir.sajjadboodaghi.niraa.R;

/**
 * Created by Sajad on 03/08/2019.
 */

public class LinkHandler {
    public static void open(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, context.getResources().getString(R.string.link_is_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    public static String verifyUrl(String url) {
        if(!url.trim().isEmpty()) {
            if(url.indexOf("http") != 0) {
                return "http://" + url;
            }
            return url;
        }
        return "";
    }
}
