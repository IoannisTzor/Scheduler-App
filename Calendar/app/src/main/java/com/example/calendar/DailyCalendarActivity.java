package com.example.calendar;

import static com.example.calendar.CalendarUtils.selectedDate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
        monthDayText = findViewById(R.id.monthDayText);
        dayOfWeekTV = findViewById(R.id.dayOfWeekTV);
        hourLabelsColumn = findViewById(R.id.hourLabelsColumn);
        eventGrid = findViewById(R.id.eventGrid);
        applyActiveNav(R.id.navDailyBtn);
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
        dayOfWeekTV.setText(selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        buildDayGrid();
    }

    private void buildDayGrid()
    {
        hourLabelsColumn.removeAllViews();
        eventGrid.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        final int pxPerHour = (int)(60 * density);

        // Alternating full-height row backgrounds — no thin lines, no sub-pixel flicker.
        // Full rectangles tile the canvas with zero gaps so there is nothing thin to flicker.
        final Paint evenPaint = new Paint();
        evenPaint.setColor(getResources().getColor(R.color.bgDark, null));
        final Paint oddPaint = new Paint();
        oddPaint.setColor(getResources().getColor(R.color.surfaceDark, null));

        View gridBackground = new View(this)
        {
            @Override
            protected void onDraw(Canvas canvas)
            {
                for (int h = 0; h < 24; h++)
                {
                    float top = h * pxPerHour;
                    float bottom = (h + 1) * pxPerHour;
                    canvas.drawRect(0, top, getWidth(), bottom, h % 2 == 0 ? evenPaint : oddPaint);
                }
            }
        };
        gridBackground.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 24 * pxPerHour
        ));
        eventGrid.addView(gridBackground);

        // Hour labels
        for (int hour = 0; hour < 24; hour++)
        {
            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, pxPerHour
            );
            label.setLayoutParams(labelParams);
            label.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
            label.setText(CalendarUtils.formattedShortTime(LocalTime.of(hour, 0)));
            label.setTextColor(getResources().getColor(R.color.textSecondary, null));
            label.setTextSize(11);
            hourLabelsColumn.addView(label);
        }

        // Events — each spans its full duration
        ArrayList<Event> events = Event.eventsForDate(selectedDate);
        for (Event event : events)
        {
            int startMins = event.getStartTime().getHour() * 60 + event.getStartTime().getMinute();
            int endMins   = event.getEndTime().getHour()   * 60 + event.getEndTime().getMinute();
            int durationMins = Math.max(endMins - startMins, 30);

            TextView eventView = new TextView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (int)(durationMins * density)
            );
            params.topMargin = (int)(startMins * density);
            int pad = (int)(6 * density);
            eventView.setLayoutParams(params);
            eventView.setPadding(pad, pad, pad, pad);
            eventView.setText(event.getName()
                + "\n" + CalendarUtils.formattedShortTime(event.getStartTime())
                + " – " + CalendarUtils.formattedShortTime(event.getEndTime()));
            eventView.setBackgroundColor(event.getColor());
            eventView.setTextColor(getResources().getColor(R.color.white, null));
            eventView.setTextSize(12);

            final int index = Event.indexOf(event);
            eventView.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventEditActivity.class);
                intent.putExtra(EventEditActivity.EXTRA_EVENT_INDEX, index);
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
        startActivity(new Intent(this, EventEditActivity.class));
    }

    public void monthlyAction(View view)
    {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void weeklyAction(View view)
    {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

    public void dailyAction(View view) {}

    public void projectsAction(View view)
    {
        startActivity(new Intent(this, ProjectsActivity.class));
    }

    private void applyActiveNav(int activeId)
    {
        int[] ids = {R.id.navMonthlyBtn, R.id.navWeeklyBtn, R.id.navDailyBtn, R.id.navProjectsBtn};
        for (int id : ids)
        {
            Button btn = findViewById(id);
            if (id == activeId)
            {
                btn.setBackgroundResource(R.drawable.nav_pill_active);
                btn.setTextColor(getColor(R.color.bgDark));
            }
            else
            {
                btn.setBackgroundResource(R.drawable.nav_pill_inactive);
                btn.setTextColor(getColor(R.color.accent));
            }
        }
    }
}
