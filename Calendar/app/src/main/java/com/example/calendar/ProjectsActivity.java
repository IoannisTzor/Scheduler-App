package com.example.calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;

public class ProjectsActivity extends AppCompatActivity
{
    private static final String[] SORT_LABELS = { "Date", "Importance", "Difficulty" };

    private RecyclerView projectsRecyclerView;
    private TextView emptyProjectsTV;
    private Spinner sortSpinner;
    private int currentSort = 0; // 0=date, 1=importance, 2=difficulty

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView);
        emptyProjectsTV = findViewById(R.id.emptyProjectsTV);
        sortSpinner = findViewById(R.id.sortSpinner);
        setupSortSpinner();
        applyActiveNav(R.id.navProjectsBtn);
    }

    public void monthlyAction(View view)
    {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void weeklyAction(View view)
    {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

    public void dailyAction(View view)
    {
        startActivity(new Intent(this, DailyCalendarActivity.class));
    }

    public void projectsAction(View view) {}

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

    private void setupSortSpinner()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, SORT_LABELS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                currentSort = position;
                loadProjects();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        loadProjects();
    }

    private void loadProjects()
    {
        ArrayList<Event> projects = Event.projectsSortedByDate();
        if (currentSort == 1)
            projects.sort(Comparator.comparingInt(Event::getImportance).reversed());
        else if (currentSort == 2)
            projects.sort(Comparator.comparingInt(Event::getDifficulty).reversed());

        if (projects.isEmpty())
        {
            emptyProjectsTV.setVisibility(View.VISIBLE);
            projectsRecyclerView.setVisibility(View.GONE);
        }
        else
        {
            emptyProjectsTV.setVisibility(View.GONE);
            projectsRecyclerView.setVisibility(View.VISIBLE);
            projectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            projectsRecyclerView.setAdapter(new ProjectAdapter(projects));
        }
    }

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder>
    {
        private final ArrayList<Event> projects;

        ProjectAdapter(ArrayList<Event> projects) { this.projects = projects; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            Event project = projects.get(position);
            holder.nameTV.setText(project.getName());
            holder.dueDateTV.setText("Due: " + CalendarUtils.formattedDate(project.getDate()));
            holder.importanceTV.setText("Importance: " + project.getImportance() + "/10");
            holder.difficultyTV.setText("Difficulty: " + project.getDifficulty() + "/10");
            holder.itemView.setOnClickListener(v -> {
                int index = Event.indexOf(project);
                if (index >= 0)
                {
                    Intent intent = new Intent(ProjectsActivity.this, EventEditActivity.class);
                    intent.putExtra(EventEditActivity.EXTRA_EVENT_INDEX, index);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() { return projects.size(); }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView nameTV, dueDateTV, importanceTV, difficultyTV;

            ViewHolder(View itemView)
            {
                super(itemView);
                nameTV = itemView.findViewById(R.id.projectNameTV);
                dueDateTV = itemView.findViewById(R.id.projectDueDateTV);
                importanceTV = itemView.findViewById(R.id.projectImportanceTV);
                difficultyTV = itemView.findViewById(R.id.projectDifficultyTV);
            }
        }
    }
}
