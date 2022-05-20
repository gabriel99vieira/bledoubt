package hawk.privacy.bledoubt;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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
        Log.i("Notifications", "CREATE ONE");
        Intent startInspectDeviceFragIntent = new Intent(context, RadarActivity.class);
        startInspectDeviceFragIntent.setAction(Intent.ACTION_MAIN);
        startInspectDeviceFragIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startInspectDeviceFragIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startInspectDeviceFragIntent.putExtra(RadarActivity.BLUETOOTH_ADDRESS_MESSAGE, deviceMetadata.bluetoothAddress);
        startInspectDeviceFragIntent.putExtra(RadarActivity.SUSPICIOUS_DEVICE_REQUEST_KEY, RadarActivity.INSPECT_ONE_DEVICE_EXTRA_CODE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startInspectDeviceFragIntent, PendingIntent.FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);

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
     * Creates a notification warning the user of multiple suspicious devices.
     * @param context
     * @param deviceCount number of suspicious devices detected.
     */
    public void createSuspiciousMultiDeviceNotification(Context context, int deviceCount) {
        // Create intent to open suspicious device list
        Log.i("Notifications", "CREATE ONE");
        Intent openSusDeviceListIntent = new Intent(context, RadarActivity.class);
        openSusDeviceListIntent.setAction(Intent.ACTION_MAIN);
        openSusDeviceListIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        openSusDeviceListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openSusDeviceListIntent.putExtra(RadarActivity.SUSPICIOUS_DEVICE_REQUEST_KEY, RadarActivity.INSPECT_SUSPICIOUS_DEVICES_EXTRA_CODE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openSusDeviceListIntent, PendingIntent.FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);

        // Build notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.doubter_launcher0)
                .setContentTitle(context.getString(R.string.suspicious_notification_title))
                .setContentText( context.getString(R.string.multi_suspicious_notification_text, deviceCount))
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
                context, 0, intent, FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
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
