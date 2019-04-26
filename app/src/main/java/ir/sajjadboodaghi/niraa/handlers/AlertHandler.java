package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertNoticeCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertErrorCallback;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertYesOrNoCallback;
import ir.sajjadboodaghi.niraa.R;
import ir.sajjadboodaghi.niraa.handlers.Interfaces.AlertPayCallback;

/**
 * Created by Sajjad on 05/15/2018.
 */

public class AlertHandler {

    public static void yesOrNo(Context context, String message, final AlertYesOrNoCallback callback) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(context.getResources().getString(R.string.alert_dialog_title_danger));
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);

        alertBuilder.setPositiveButton(context.getResources().getString(R.string.alert_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.yes();
            }
        });

        alertBuilder.setNegativeButton(context.getResources().getString(R.string.alert_dialog_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.no();
            }
        });

        alertBuilder.show();
    }

    public static void error(Context context, String message, final AlertErrorCallback callback) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(context.getResources().getString(R.string.alert_dialog_title_error));
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);

        alertBuilder.setNeutralButton(context.getResources().getString(R.string.alert_dialog_button_try_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.tryAgain();
            }
        });

        alertBuilder.setNegativeButton(context.getResources().getString(R.string.alert_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.cancel();
            }
        });

        alertBuilder.show();
    }
    public static void notice(Context context, String message, final AlertNoticeCallback callback) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(context.getResources().getString(R.string.alert_dialog_title_notice));
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);

        alertBuilder.setNeutralButton(context.getResources().getString(R.string.alert_dialog_button_understand), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.okay();
            }
        });

        alertBuilder.show();
    }

    public static void pay(Context context, String message, final AlertPayCallback callback) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(context.getResources().getString(R.string.alert_dialog_title_error));
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);

        alertBuilder.setNeutralButton(context.getResources().getString(R.string.alert_dialog_button_try_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.tryAgain();
            }
        });

        alertBuilder.setNegativeButton(context.getResources().getString(R.string.alert_dialog_button_report), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.pay();
            }
        });

        alertBuilder.show();
    }


}
