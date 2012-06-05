package com.geertvanderploeg.kiekeboek.app;

import java.util.ArrayList;
import java.util.List;

import com.geertvanderploeg.kiekeboek.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Holder and convenience class for notification messages about background tasks etc.
 */
public class Notifications {

  private static final int NOTIFICATION_ID = 1;

  /**
   * Domain class
   */
  public static class NotificationMessage {
    private String message;
    private String data;

    public NotificationMessage(String notificationMessage, String data) {
      this.message = notificationMessage;
      this.data = data;
    }
  }

  /**
   * Holder field
   */
  private static final List<NotificationMessage> notifications = new ArrayList<NotificationMessage>();

  /**
   * Add a new notification to the queue
   * @param notificationMessage
   * @param data
   * @return
   */
  public static NotificationMessage addNotification(Context c, String notificationMessage, String data) {
    final NotificationMessage notification = new NotificationMessage(notificationMessage, data);
    notifications.add(notification);
    addToStatusbar(c, notification);
    return notification;
  }

  private static void addToStatusbar(Context c, NotificationMessage n) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) c.getSystemService(ns);
    int icon = R.drawable.stat_sys_warning;
    CharSequence tickerText = n.message;
    long when = System.currentTimeMillis();

    Notification notification = new Notification(icon, tickerText, when);

    Context context = c.getApplicationContext();
    CharSequence contentTitle = n.message;
    CharSequence contentText = n.data;
    Intent notificationIntent = new Intent(c, NotificationMessageView.class);
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

    mNotificationManager.notify(NOTIFICATION_ID, notification);
  }
}
