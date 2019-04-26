package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.Map;

import ir.sajjadboodaghi.niraa.handlers.Interfaces.RequestCallback;

public class RequestHandler {
    public static boolean isDeviceConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void makeRequest(final Context context, final String methodName, final String url, final Map<String, String> params, final RequestCallback callback) {
        if(!isDeviceConnected(context)){
            callback.onNoInternetAccess();
            return;
        }

        int method = Request.Method.GET;
        // if(methodName.toUpperCase().equals("GET")) { method = Request.Method.GET; }
        if(methodName.toUpperCase().equals("POST")) { method = Request.Method.POST; }

        StringRequest stringRequest = new StringRequest(
                method,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onRequestError();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        RequestQueueHandler.getInstance(context).addToRequestQueue(stringRequest);
    }
}
