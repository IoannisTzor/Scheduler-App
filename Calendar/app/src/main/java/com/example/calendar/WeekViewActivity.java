package com.example.calendar;

import static com.example.calendar.CalendarUtils.daysInWeekArray;
import static com.example.calendar.CalendarUtils.monthYearFromDate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener
{
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);
        initWidgets();
        setWeekView();
        applyActiveNav(R.id.navWeeklyBtn);
    }

    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        eventListView = findViewById(R.id.eventListView);
    }

    private void setWeekView()
    {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        setEventAdapter();
    }

    public void previousWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onItemClick(int position, LocalDate date)
    {
        CalendarUtils.selectedDate = date;
        setWeekView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setEventAdapter();
    }

    private void setEventAdapter()
    {
        ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);
        EventAdapter eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents);
        eventListView.setAdapter(eventAdapter);
        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            int index = Event.indexOf(dailyEvents.get(position));
            Intent intent = new Intent(this, EventEditActivity.class);
            intent.putExtra(EventEditActivity.EXTRA_EVENT_INDEX, index);
            startActivity(intent);
        });
    }

    public void newEventAction(View view)
    {
        startActivity(new Intent(this, EventEditActivity.class));
    }

    public void monthlyAction(View view)
    {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void weeklyAction(View view) {}

    public void dailyAction(View view)
    {
        startActivity(new Intent(this, DailyCalendarActivity.class));
    }

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
