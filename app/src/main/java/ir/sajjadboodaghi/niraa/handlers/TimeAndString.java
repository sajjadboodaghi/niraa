package ir.sajjadboodaghi.niraa.handlers;

import android.content.Context;
/**
 * Created by Sajjad on 01/20/2019.
 */

public class TimeAndString {
    public static String makeReadableTime(Context context, int timestamp) {
        double difference = SharedPrefManager.getInstance(context).getNowTimestamp() - timestamp;
        String readableTime = "";

        if(difference / (60 * 60 * 24 * 365) >= 1) {
            int years = (int) difference / (60 * 60 * 24 * 365);
            readableTime = years + " سال پیش";
        } else if(difference / (60 * 60 * 24 * 30) >= 1) {
            int months = (int) difference / (60 * 60 * 24 * 30);
            readableTime =  months + " ماه پیش";
        } else if(difference / (60 * 60 * 24 * 7) >= 2) {
            int weeks = (int) difference / (60 * 60 * 24 * 7);
            readableTime = weeks + " هفته پیش";
        } else if(difference / (60 * 60 * 24) >= 1) {
            int days = (int) difference / (60 * 60 * 24);
            readableTime = days + " روز پیش";
        } else if(difference / (60 * 60) >= 1) {
            int hours = (int) difference / (60 * 60);
            readableTime = hours + " ساعت پیش";
        } else if(difference / (60) >= 2) {
            int minutes = (int) (difference / 60);
            readableTime = minutes + " دقیقه پیش";
        } else {
            readableTime = "چند لحظه پیش";
        }

        return changeEnglishDigitsToFarsiDigits(readableTime);
    }

    public static String changeFarsiDigitsToEnglishDigits(String input) {
        String output = input.replace("۰", "0")
                .replace("۱", "1")
                .replace("۲", "2")
                .replace("۳", "3")
                .replace("٤‎", "4")
                .replace("۴", "4")
                .replace("٥‎", "5")
                .replace("۵", "5")
                .replace("٦‎", "6")
                .replace("۶", "6")
                .replace("۷", "7")
                .replace("۸", "8")
                .replace("۹", "9");
        return output;
    }

    public static String changeEnglishDigitsToFarsiDigits(String input) {
        String output = input.replace('0', '۰')
                        .replace('1', '۱')
                        .replace('2', '۲')
                        .replace('3', '۳')
                        .replace('4', '۴')
                        .replace('5', '۵')
                        .replace('6', '۶')
                        .replace('7', '۷')
                        .replace('8', '۸')
                        .replace('9', '۹');
        return output;
    }

}
