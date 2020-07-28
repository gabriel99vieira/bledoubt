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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications {
    public static final String DEFAULT_CHANNEL_ID = "default_channel";



    private int next_notification_id = 0;
    private List<Integer> activeNotifications = new ArrayList<>();
    /**
     * Must be called once before any notfications can be produced on Android 8.0+
     * This is adapted from https://developer.android.com/training/notify-user/build-notification
     * @param context
     */
    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Creates a notification warning the user of a suspicious device.
     * @param context
     * @param deviceMetadata corresponding to the suspicious device
     */
    public void CreateSuspiciousDeviceNotification(Context context, DeviceMetadata deviceMetadata) {
        // Create intent to open InspectDeviceActivity
        Intent startInspectDeviceActivityIntent = new Intent(context, InspectDeviceActivity.class);
        startInspectDeviceActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startInspectDeviceActivityIntent.putExtra(InspectDeviceActivity.BLUETOOTH_ADDRESS_MESSAGE, deviceMetadata.bluetoothAddress);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startInspectDeviceActivityIntent, 0);

        // Build notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(context.getString(R.string.suspicious_notification_title))
                .setContentText( context.getString(R.string.suspicious_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, context.getString(R.string.suspicious_notification_accept), pendingIntent);
        Notification note = builder.build();
        NotificationManagerCompat.from(context).notify(next_notification_id, note);

        // Keep track of active notifications.
        next_notification_id += 1;
    }

    public void clearAllNotifications() {

    }
}
