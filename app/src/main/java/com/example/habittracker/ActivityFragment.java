package com.example.habittracker;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        LineChart weeklyChart = view.findViewById(R.id.weeklyChart);
        setupWeeklyChart(weeklyChart);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).showViewPager();
            }
        });

        return view;
    }

    private void setupWeeklyChart(LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4));
        entries.add(new Entry(1, 5));
        entries.add(new Entry(2, 3));
        entries.add(new Entry(3, 6));
        entries.add(new Entry(4, 7));
        entries.add(new Entry(5, 5));
        entries.add(new Entry(6, 8));

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Activity");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.primary));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);

        // Add a gradient fill
        dataSet.setDrawFilled(true);
        Drawable fillDrawable = ContextCompat.getDrawable(getContext(), R.drawable.chart_gradient);
        dataSet.setFillDrawable(fillDrawable);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.divider));
        leftAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        chart.getAxisRight().setEnabled(false);

        chart.invalidate(); // refresh
    }
}
