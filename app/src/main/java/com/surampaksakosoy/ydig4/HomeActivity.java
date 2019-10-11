package com.surampaksakosoy.ydig4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.surampaksakosoy.ydig4.adapters.pagerAdapter;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupToolbar();
        setupTabLayout();
    }


    private void setupToolbar() {
        Toolbar toolbar_home = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        TextView titleBar = findViewById(R.id.titleBar);
        titleBar.setText(R.string.radio_streaming);
    }

    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tablayoutFragment);

        @SuppressLint("InflateParams") TextView customTab0 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab0.setText(R.string.radio_streaming);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(customTab0);

        @SuppressLint("InflateParams") TextView customTab1 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab1.setText(R.string.tentang_kami);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(customTab1);

        @SuppressLint("InflateParams") TextView customTab2 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab2.setText(R.string.profile);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(customTab2);

        setupViewPager(tabLayout);

    }

    private void setupViewPager(TabLayout tabLayout) {
        final ViewPager viewPager = findViewById(R.id.pagerAdapter);
        final PagerAdapter adapter = new pagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}
