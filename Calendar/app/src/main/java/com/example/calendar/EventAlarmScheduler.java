package com.example.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class EventAlarmScheduler
{
    static final String EXTRA_EVENT_ID = "EVENT_ID";

    public static void schedule(Context context, Event event)
    {
        if (event.getReminderMinutes() < 0) return;

        LocalDate nextDate = nextOccurrenceFrom(event, LocalDate.now());
        if (nextDate == null) return;

        LocalDateTime triggerDT = nextDate.atTime(event.getStartTime())
            .minusMinutes(event.getReminderMinutes());
        long triggerMillis = triggerDT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (triggerMillis <= System.currentTimeMillis()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, event.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms())
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerMillis, 60_000, pi);
        else
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
    }

    public static void cancel(Context context, int eventId)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, eventId);
        alarmManager.cancel(pi);
        pi.cancel();
    }

    public static void rescheduleAll(Context context)
    {
        for (Event event : Event.eventsList)
            schedule(context, event);
    }

    // Finds the next date >= `from` on which the event occurs.
    static LocalDate nextOccurrenceFrom(Event event, LocalDate from)
    {
        LocalDate check = from.isBefore(event.getDate()) ? event.getDate() : from;
        for (int i = 0; i <= 366 * 5; i++, check = check.plusDays(1))
        {
            if (event.occursOn(check)) return check;
        }
        return null;
    }

    private static PendingIntent buildPendingIntent(Context context, int eventId)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        return PendingIntent.getBroadcast(
            context, eventId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
