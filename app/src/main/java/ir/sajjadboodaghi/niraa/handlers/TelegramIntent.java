package ir.sajjadboodaghi.niraa.handlers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Sajjad on 12/23/2018.
 */

public class TelegramIntent {
    public static void start(Context context, String telegramId)
    {
        Uri uri = Uri.parse("https://t.me/" + telegramId);
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
        likeIng.setPackage("org.telegram.messenger");
        try
        {
            context.startActivity(likeIng);
        }
        catch (ActivityNotFoundException e)
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + telegramId)));
        }
    }
}
