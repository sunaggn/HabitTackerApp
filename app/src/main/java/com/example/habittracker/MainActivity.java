package com.example.habittracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewPager2 viewPager;
    private DayPagerAdapter pagerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private HabitTrackerDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme preference before setting content view
        android.content.SharedPreferences preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        String themeMode = preferences.getString("theme_mode", "Light");
        int nightMode;
        if ("Dark".equals(themeMode)) {
            nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.background));
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = HabitTrackerDatabase.getInstance(this);
        
        // Reschedule all alarms when app starts (in case device was rebooted)
        AlarmHelper.rescheduleAllAlarms(this);
        
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        viewPager = findViewById(R.id.view_pager);

        // Setup navigation drawer
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(this);

        // Setup ViewPager2 for day navigation
        pagerAdapter = new DayPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1000, false); // Start at middle (today)

        // Load user profile data
        loadUserProfile();
    }

    public void loadUserProfile() {
        android.database.Cursor cursor = null;
        try {
            cursor = database.getUserProfile();
            if (cursor != null && cursor.moveToFirst()) {
                View headerView = navigationView.getHeaderView(0);
                if (headerView != null) {
                    TextView userName = headerView.findViewById(R.id.nav_user_name);
                    TextView userEmail = headerView.findViewById(R.id.nav_user_email);
                    ShapeableImageView profileImage = headerView.findViewById(R.id.nav_profile_image);
                    
                    int nameIdx = cursor.getColumnIndex("name");
                    int surnameIdx = cursor.getColumnIndex("surname");
                    int emailIdx = cursor.getColumnIndex("email");
                    int pathIdx = cursor.getColumnIndex("profile_image_path");
                    
                    String name = nameIdx != -1 ? cursor.getString(nameIdx) : null;
                    String surname = surnameIdx != -1 ? cursor.getString(surnameIdx) : null;
                    String email = emailIdx != -1 ? cursor.getString(emailIdx) : null;
                    String profileImagePath = pathIdx != -1 ? cursor.getString(pathIdx) : null;
                    
                    if (name != null && surname != null) {
                        userName.setText(name + " " + surname);
                    }
                    if (email != null) {
                        userEmail.setText(email);
                    }
                    
                    // Load profile image if exists
                    if (profileImage != null) {
                        if (profileImagePath != null && !profileImagePath.isEmpty()) {
                            java.io.File imageFile = new java.io.File(profileImagePath);
                            if (imageFile.exists()) {
                                Glide.with(this)
                                        .load(imageFile)
                                        .circleCrop()
                                        .placeholder(android.R.drawable.ic_menu_myplaces)
                                        .error(android.R.drawable.ic_menu_myplaces)
                                        .into(profileImage);
                            } else {
                                // File doesn't exist, show default icon
                                profileImage.setImageResource(android.R.drawable.ic_menu_myplaces);
                            }
                        } else {
                            // No profile image path, show default icon
                            profileImage.setImageResource(android.R.drawable.ic_menu_myplaces);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error loading user profile", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_profile) {
            fragment = new UserProfileFragment();
        } else if (id == R.id.nav_habits) {
            fragment = new HabitsListFragment();
        } else if (id == R.id.nav_activity) {
            fragment = new ActivityFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        }

        if (fragment != null) {
            viewPager.setVisibility(android.view.View.GONE);
            findViewById(R.id.fragment_container).setVisibility(android.view.View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    public void showViewPager() {
        viewPager.setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.fragment_container).setVisibility(android.view.View.GONE);
    }

    public void showWeeklyView() {
        WeeklyViewFragment fragment = new WeeklyViewFragment();
        viewPager.setVisibility(android.view.View.GONE);
        findViewById(R.id.fragment_container).setVisibility(android.view.View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void showMonthlyView() {
        showMonthlyView(null);
    }

    public void showMonthlyView(String weekStartDate) {
        viewPager.setVisibility(android.view.View.GONE);
        findViewById(R.id.fragment_container).setVisibility(android.view.View.VISIBLE);
        
        MonthlyCalendarFragment fragment = MonthlyCalendarFragment.newInstance(weekStartDate);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void openDrawer() {
        drawerLayout.openDrawer(navigationView);
    }

    public HabitTrackerDatabase getDatabase() {
        return database;
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public void updateTheme(String theme) {
        // Theme is now handled by Material3 DayNight
        // This method is kept for compatibility but no longer needed
        android.content.SharedPreferences preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        preferences.edit().putString("app_mode", theme).apply();
        
        // Recreate activity to apply theme changes
        recreate();
    }

    public void navigateToDate(String date) {
        // Calculate the position for the given date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            Date targetDate = sdf.parse(date);
            Calendar targetCal = Calendar.getInstance();
            targetCal.setTime(targetDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            
            long diff = targetCal.getTimeInMillis() - todayCal.getTimeInMillis();
            int daysDiff = (int) (diff / (1000 * 60 * 60 * 24));
            
            viewPager.setCurrentItem(1000 + daysDiff, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshTodayFragment() {
        android.util.Log.d("MainActivity", "refreshTodayFragment called");
        // Refresh all added TodayFragment instances without recreating them
        try {
            androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
            List<Fragment> fragments = fm.getFragments();
            boolean refreshed = false;
            for (Fragment fragment : fragments) {
                if (fragment instanceof TodayFragment && fragment.isAdded()) {
                    android.util.Log.d("MainActivity", "Refreshing TodayFragment instance: " + fragment.hashCode());
                    ((TodayFragment) fragment).onRefresh();
                    refreshed = true;
                }
            }
            
            // If the current fragment wasn't in the fragment manager (unlikely with ViewPager2)
            // or if we want to ensure the adapter is aware of changes
            if (!refreshed && viewPager != null && pagerAdapter != null) {
                android.util.Log.d("MainActivity", "No TodayFragment found to refresh, notifying adapter");
                // Since we can't easily get the fragment from the adapter's implementation details 
                // without internal hacks, we rely on the fragments having been found above.
                // If they WEREN'T found, we fallback to notifyItemChanged but only as a last resort.
                // However,fm.getFragments() usually finds them in ViewPager2.
                pagerAdapter.notifyItemChanged(viewPager.getCurrentItem());
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in refreshTodayFragment", e);
        }
    }

    private static class DayPagerAdapter extends FragmentStateAdapter {
        private static final int TOTAL_DAYS = 2000; // 1000 days before and after today

        public DayPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, position - 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = sdf.format(calendar.getTime());
            
            return TodayFragment.newInstance(date);
        }

        @Override
        public int getItemCount() {
            return TOTAL_DAYS;
        }
    }
}
