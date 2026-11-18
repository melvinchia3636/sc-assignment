package dev.melvinchia3636.airngreen.services.alerts;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import dev.melvinchia3636.airngreen.R;

/**
 * Observer that posts threshold crossings as system notifications.
 */
public final class NotificationAlertObserver implements AlertObserver {

    public static final String CHANNEL_ID = "pollution_alerts";

    private final Context context;

    public NotificationAlertObserver(Context context) {
        this.context = context.getApplicationContext();
    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.alert_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.alert_channel_description));
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (!manager.areNotificationsEnabled()) {
            return;
        }

        String level = context.getString(alert.level().labelRes());
        String body = context.getString(R.string.alert_notification_text,
                Math.round(alert.aqi()), level);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.alert_notification_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(launchIntent())
                .build();
        manager.notify((int) (alert.epochMillis() % Integer.MAX_VALUE), notification);
    }

    private PendingIntent launchIntent() {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (intent == null) {
            intent = new Intent();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
