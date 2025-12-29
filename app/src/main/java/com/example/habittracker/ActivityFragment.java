package com.example.habittracker;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ActivityFragment extends Fragment {
    private ViewPager2 weekViewPager;
    private WeekPagerAdapter pagerAdapter;
    private TextView weekTitle;
    private HabitTrackerDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        database = HabitTrackerDatabase.getInstance(requireContext());
        weekViewPager = view.findViewById(R.id.week_view_pager);
        weekTitle = view.findViewById(R.id.week_title);

        // Setup ViewPager2 for week navigation
        pagerAdapter = new WeekPagerAdapter(requireActivity());
        weekViewPager.setAdapter(pagerAdapter);
        weekViewPager.setCurrentItem(1000, false); // Start at middle (current week)

        // Update title when page changes
        weekViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateWeekTitle(position);
            }
        });

        // Set initial title
        updateWeekTitle(1000);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });

        return view;
    }

    private void updateWeekTitle(int position) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
        cal.add(Calendar.WEEK_OF_YEAR, position - 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM", Locale.getDefault());
        Calendar weekEnd = (Calendar) cal.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
        
        String weekRange = sdf.format(cal.getTime()) + " - " + sdf.format(weekEnd.getTime());
        weekTitle.setText(weekRange);
    }

    private static class WeekPagerAdapter extends FragmentStateAdapter {
        private static final int TOTAL_WEEKS = 2000; // 1000 weeks before and after current week

        public WeekPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Calendar cal = Calendar.getInstance();
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            cal.add(Calendar.WEEK_OF_YEAR, position - 1000);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String weekStartDate = sdf.format(cal.getTime());
            
            Calendar weekEnd = (Calendar) cal.clone();
            weekEnd.add(Calendar.DAY_OF_YEAR, 6);
            String weekEndDate = sdf.format(weekEnd.getTime());
            
            return WeekChartFragment.newInstance(weekStartDate, weekEndDate);
        }

        @Override
        public int getItemCount() {
            return TOTAL_WEEKS;
        }
    }

    public static class WeekChartFragment extends Fragment {
        private static final String ARG_START_DATE = "start_date";
        private static final String ARG_END_DATE = "end_date";
        
        private String startDate;
        private String endDate;
        private HabitTrackerDatabase database;

        public static WeekChartFragment newInstance(String startDate, String endDate) {
            WeekChartFragment fragment = new WeekChartFragment();
            Bundle args = new Bundle();
            args.putString(ARG_START_DATE, startDate);
            args.putString(ARG_END_DATE, endDate);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                startDate = getArguments().getString(ARG_START_DATE);
                endDate = getArguments().getString(ARG_END_DATE);
            }
            database = HabitTrackerDatabase.getInstance(requireContext());
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_week_chart, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            
            LineChart weeklyChart = view.findViewById(R.id.weeklyChart);
            setupWeeklyChart(weeklyChart);
        }

        private void setupWeeklyChart(LineChart chart) {
            // Get habit counts for the week
            int[] counts = database.getHabitCountsForWeek(startDate, endDate);
            
            // Create entries for each day of the week (Monday=0 to Sunday=6)
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                entries.add(new Entry(i, counts[i]));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Tamamlanan Alışkanlıklar");
            dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary));
            dataSet.setCircleRadius(5f);
            dataSet.setDrawCircleHole(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(true);
            dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            dataSet.setValueTextSize(10f);

            // Add a gradient fill
            dataSet.setDrawFilled(true);
            Drawable fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient);
            if (fillDrawable != null) {
                dataSet.setFillDrawable(fillDrawable);
            }

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);

            // Customize chart appearance
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(false); // Disable drag to allow ViewPager2 to handle swipes
            chart.setScaleEnabled(false);
            chart.setPinchZoom(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            xAxis.setValueFormatter(new ValueFormatter() {
                private final String[] days = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
                
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < days.length) {
                        return days[index];
                    }
                    return "";
                }
            });
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.divider));
            leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            leftAxis.setAxisMinimum(0f);
            leftAxis.setGranularity(1f);

            chart.getAxisRight().setEnabled(false);

            chart.invalidate(); // refresh
        }
    }
}
