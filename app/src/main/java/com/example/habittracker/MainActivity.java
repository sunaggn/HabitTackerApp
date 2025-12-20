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
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new HabitTrackerDatabase(this);
        
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
        android.database.Cursor cursor = database.getUserProfile();
        if (cursor.moveToFirst()) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                TextView userName = headerView.findViewById(R.id.nav_user_name);
                TextView userEmail = headerView.findViewById(R.id.nav_user_email);
                
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String surname = cursor.getString(cursor.getColumnIndexOrThrow("surname"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                
                if (name != null && surname != null) {
                    userName.setText(name + " " + surname);
                }
                if (email != null) {
                    userEmail.setText(email);
                }
            }
        }
        cursor.close();
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
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else if (id == R.id.nav_customize) {
            fragment = new CustomizeFragment();
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

    public void openDrawer() {
        drawerLayout.openDrawer(navigationView);
    }

    public HabitTrackerDatabase getDatabase() {
        return database;
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
