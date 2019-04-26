package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Sajjad on 02/12/2018.
 */

public class RequestQueueHandler {
    private static RequestQueueHandler mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private RequestQueueHandler(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized RequestQueueHandler getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueHandler(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}