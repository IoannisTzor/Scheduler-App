package com.example.calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.time.LocalDate;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        EventStorage.load(context);

        int eventId = intent.getIntExtra(EventAlarmScheduler.EXTRA_EVENT_ID, -1);
        if (eventId == -1) return;

        Event event = Event.findById(eventId);
        if (event == null) return;

        postNotification(context, event);

        // For recurring events, schedule the next occurrence
        if (event.getRecurrenceType() != RecurrenceType.NONE)
        {
            LocalDate nextDate = EventAlarmScheduler.nextOccurrenceFrom(
                event, LocalDate.now().plusDays(1)
            );
            if (nextDate != null)
                EventAlarmScheduler.schedule(context, event);
        }
    }

    private void postNotification(Context context, Event event)
    {
        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
            context, event.getId(), openApp,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String timeRange = CalendarUtils.formattedShortTime(event.getStartTime())
            + " – " + CalendarUtils.formattedShortTime(event.getEndTime());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
            context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(event.getName())
            .setContentText(timeRange)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true);

        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(event.getId(), builder.build());
    }
}
