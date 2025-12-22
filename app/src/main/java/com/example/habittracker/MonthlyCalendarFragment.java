package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyCalendarFragment extends Fragment {
    private static final String ARG_WEEK_START_DATE = "week_start_date";
    
    private ViewPager2 monthViewPager;
    private MonthPagerAdapter monthPagerAdapter;
    private ImageButton menuButton;
    private ImageButton btnBack;
    private String weekStartDate;

    public static MonthlyCalendarFragment newInstance(String weekStartDate) {
        MonthlyCalendarFragment fragment = new MonthlyCalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WEEK_START_DATE, weekStartDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            weekStartDate = getArguments().getString(ARG_WEEK_START_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        applyThemeBackground(view);
        
        monthViewPager = view.findViewById(R.id.month_view_pager);
        menuButton = view.findViewById(R.id.menu_button);
        btnBack = view.findViewById(R.id.btn_back);
        
        setupButtons();
        
        monthPagerAdapter = new MonthPagerAdapter(requireActivity());
        monthViewPager.setAdapter(monthPagerAdapter);
        
        // Calculate month position based on weekStartDate or current date
        Calendar cal = Calendar.getInstance();
        if (weekStartDate != null && !weekStartDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                cal.setTime(sdf.parse(weekStartDate));
            } catch (Exception e) {
                e.printStackTrace();
                cal = Calendar.getInstance();
            }
        }
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Calculate position: start from current year (2024) as position 1000
        Calendar baseYear = Calendar.getInstance();
        baseYear.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        baseYear.set(Calendar.MILLISECOND, 0);
        
        long monthsDiff = (cal.get(Calendar.YEAR) - baseYear.get(Calendar.YEAR)) * 12L + 
                         (cal.get(Calendar.MONTH) - baseYear.get(Calendar.MONTH));
        int monthsFromStart = (int) (1000 + monthsDiff);
        monthViewPager.setCurrentItem(monthsFromStart, false);
    }

    private void setupButtons() {
        menuButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showWeeklyView();
            }
        });
    }

    private static class MonthPagerAdapter extends FragmentStateAdapter {
        private static final int TOTAL_MONTHS = 2000; // 1000 months before and after current month

        public MonthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Base year is 2024 (position 1000 corresponds to January 2024)
            Calendar baseYear = Calendar.getInstance();
            baseYear.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
            baseYear.set(Calendar.MILLISECOND, 0);
            
            Calendar calendar = (Calendar) baseYear.clone();
            calendar.add(Calendar.MONTH, position - 1000); // Add months offset from base

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String monthStartDate = sdf.format(calendar.getTime());

            return MonthlyCalendarContentFragment.newInstance(monthStartDate);
        }

        @Override
        public int getItemCount() {
            return TOTAL_MONTHS;
        }
    }

    private void applyThemeBackground(View view) {
        android.content.SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE);
        String theme = preferences.getString("app_mode", "Purple");
        int backgroundRes;
        if ("Green".equals(theme)) {
            backgroundRes = R.drawable.gradient_background_green;
        } else {
            backgroundRes = R.drawable.gradient_background_vibrant;
        }
        View rootView = view.getRootView();
        if (rootView != null) {
            rootView.setBackgroundResource(backgroundRes);
        }
        view.setBackgroundResource(backgroundRes);
    }
}
