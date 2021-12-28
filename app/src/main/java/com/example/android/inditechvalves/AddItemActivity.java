package com.example.android.inditechvalves;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

public class AddItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        getSupportActionBar().setElevation(0);
        ViewPager viewPager=findViewById(R.id.pager);
        AddItemFragmentPagerAdapter addItemFragmentPagerAdapter=new AddItemFragmentPagerAdapter(this,getSupportFragmentManager());
        viewPager.setAdapter(addItemFragmentPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}