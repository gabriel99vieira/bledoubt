package hawk.privacy.bledoubt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import hawk.privacy.bledoubt.ui.main.InspectDeviceFragment;

public class Notifications {
    public static final String DEFAULT_CHANNEL_ID = "default_channel";
    public static final String FOREGROUND_CHANNEL_ID = "foreground_channel";
    public static final int FOREGROUND_NOTIFICATION_ID = 1;
    public static final int SUSPICIOUS_NOTIFICATION_ID = 2;

    private static Notifications instance;

    public static Notifications getInstance() {
        if (instance == null) {
            instance = new Notifications();
        }
        return instance;
    }

    //private int next_notification_id = FOREGROUND_NOTIFICATION_ID + 1; // This will overflow eventually.
    //private List<Integer> activeNotifications = new ArrayList<>();

    /**
     * Must be called once before any notfications can be produced on Android 8.0+
     * This is adapted from https://developer.android.com/training/notify-user/build-notification
     * @param context
     */
    public static void createNotificationChannels(Context context) {
        // Create the NotificationChannels, but only on API 26+ because
        // the NotificationChannel class is not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    R.string.notification_channel_name,
                    R.string.notification_channel_desc,
                    context);

            createNotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    R.string.foreground_channel_name,
                    R.string.foreground_channel_desc,
                    context);
        }
    }

    /**
     * Create a new notification channel from string resources. Should be called once, when the
     * context is first created.
     *
     * @param nameId id of a String resource that names the channel
     * @param descId id of a String resource that describes the channel
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(String channelId, int nameId, int descId, Context context) {
        CharSequence name = context.getString(nameId);
        String description = context.getString(descId);
        NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Creates a notification warning the user of a suspicious device.
     * @param context
     * @param deviceMetadata corresponding to the suspicious device
     */
    public void createSuspiciousDeviceNotification(Context context, DeviceMetadata deviceMetadata) {
        // Create intent to open InspectDeviceActivity
        Intent startInspectDeviceActivityIntent = new Intent(context, InspectDeviceFragment.class);
        startInspectDeviceActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startInspectDeviceActivityIntent.putExtra(InspectDeviceFragment.BLUETOOTH_ADDRESS_MESSAGE, deviceMetadata.bluetoothAddress);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startInspectDeviceActivityIntent, 0);


        // Build notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.doubter_launcher0)
                .setContentTitle(context.getString(R.string.suspicious_notification_title))
                .setContentText( context.getString(R.string.suspicious_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.mipmap.doubter_launcher0, context.getString(R.string.suspicious_notification_accept), pendingIntent);
        Notification note = builder.build();
        NotificationManagerCompat.from(context).notify(SUSPICIOUS_NOTIFICATION_ID, note);
    }

    /**
     * Create a persistent notification to inform the user that the app is actively scanning
     * for BLE devices.
     * @param context
     */
    public static Notification getForegroundScanningNotification(Context context) {
        Intent intent = new Intent(context, RadarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_doubter)
            .setContentTitle(context.getString(R.string.foreground_scanning_tooltip))
            .setContentIntent(pendingIntent);

        return builder.build();
    }

    public void clearAllNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }
}
