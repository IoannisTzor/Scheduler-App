package com.example.calendar;

import static com.example.calendar.CalendarUtils.selectedDate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class DailyCalendarActivity extends AppCompatActivity
{
    private TextView monthDayText;
    private TextView dayOfWeekTV;
    private LinearLayout hourLabelsColumn;
    private FrameLayout eventGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_calendar);
        initWidgets();
    }

    private void initWidgets()
    {
        monthDayText = findViewById(R.id.monthDayText);
        dayOfWeekTV = findViewById(R.id.dayOfWeekTV);
        hourLabelsColumn = findViewById(R.id.hourLabelsColumn);
        eventGrid = findViewById(R.id.eventGrid);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setDayView();
    }

    private void setDayView()
    {
        monthDayText.setText(CalendarUtils.monthDayFromDate(selectedDate));
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        dayOfWeekTV.setText(dayOfWeek);
        buildDayGrid();
    }

    private void buildDayGrid()
    {
        hourLabelsColumn.removeAllViews();
        eventGrid.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int pxPerHour = (int)(60 * density);

        for (int hour = 0; hour < 24; hour++)
        {
            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, pxPerHour
            );
            label.setLayoutParams(labelParams);
            label.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
            label.setText(CalendarUtils.formattedShortTime(LocalTime.of(hour, 0)));
            label.setTextColor(getResources().getColor(R.color.gray, null));
            label.setTextSize(11);
            hourLabelsColumn.addView(label);

            View divider = new View(this);
            FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, Math.max(1, (int)(1 * density))
            );
            dividerParams.topMargin = hour * pxPerHour;
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(getResources().getColor(R.color.lightGray, null));
            eventGrid.addView(divider);
        }

        ArrayList<Event> events = Event.eventsForDate(selectedDate);
        for (Event event : events)
        {
            int startMinutes = event.getStartTime().getHour() * 60 + event.getStartTime().getMinute();
            int endMinutes = event.getEndTime().getHour() * 60 + event.getEndTime().getMinute();
            int durationMinutes = Math.max(endMinutes - startMinutes, 30);

            TextView eventView = new TextView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (int)(durationMinutes * density)
            );
            params.topMargin = (int)(startMinutes * density);
            int pad = (int)(6 * density);
            eventView.setLayoutParams(params);
            eventView.setPadding(pad, pad, pad, pad);
            eventView.setText(event.getName()
                + "\n" + CalendarUtils.formattedShortTime(event.getStartTime())
                + " – " + CalendarUtils.formattedShortTime(event.getEndTime()));
            eventView.setBackgroundColor(event.getColor());
            eventView.setTextColor(getResources().getColor(R.color.white, null));
            eventView.setTextSize(12);

            final int index = Event.eventsList.indexOf(event);
            eventView.setOnClickListener(v -> {
                Intent intent = new Intent(this, event_edit.class);
                intent.putExtra(event_edit.EXTRA_EVENT_INDEX, index);
                startActivity(intent);
            });

            eventGrid.addView(eventView);
        }
    }

    public void previousDayAction(View view)
    {
        selectedDate = selectedDate.minusDays(1);
        setDayView();
    }

    public void nextDayAction(View view)
    {
        selectedDate = selectedDate.plusDays(1);
        setDayView();
    }

    public void newEventAction(View view)
    {
        startActivity(new Intent(this, event_edit.class));
    }
}
