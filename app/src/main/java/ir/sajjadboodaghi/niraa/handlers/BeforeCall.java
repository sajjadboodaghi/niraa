package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import ir.sajjadboodaghi.niraa.handlers.Interfaces.BeforeCallBack;

/**
 * Created by Sajad on 02/09/2019.
 */


public class BeforeCall {

    public static void show(Context context, final BeforeCallBack beforeCallBack) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("هشدارها");
        alertDialogBuilder.setMessage(
                "۱- نیرا هیچ مسؤلیتی در برابر آگهی\u200Cها ندارد." + "\n" +
                        "۲- معامله را به صورت حضوری انجام دهید." + "\n" +
                        "۳- قبل از دریافت کالا یا خدمت هیچ مبلغی پرداخت نکنید."
        );
        alertDialogBuilder.setNegativeButton("انصراف", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialogBuilder.setPositiveButton("متوجه شدم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                beforeCallBack.onOK();
            }
        });
        alertDialogBuilder.show();
    }
}
