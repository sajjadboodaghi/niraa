package ir.sajjadboodaghi.niraa.handlers.Interfaces;

/**
 * Created by Sajjad on 10/08/2018.
 */

public interface RequestCallback {
    void onNoInternetAccess();
    void onSuccess(String response);
    void onRequestError();
}