package com.example.asus.cashbuddy.Activity.User;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.asus.cashbuddy.Fragment.User.UserCreateSplitBillFragment;
import com.example.asus.cashbuddy.Fragment.User.UserReceiveSplitBillFragment;
import com.example.asus.cashbuddy.Fragment.User.UserSentSplitBillFragment;
import com.example.asus.cashbuddy.Fragment.User.UserSplitBillFragment;
import com.example.asus.cashbuddy.R;

import java.util.ArrayList;

public class UserBillsActivity extends AppCompatActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager mViewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bills);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new UserCreateSplitBillFragment(), "Create");
        viewPagerAdapter.addFragments(new UserSplitBillFragment(), "Bills");
        viewPagerAdapter.addFragments(new UserReceiveSplitBillFragment(), "Received");
        viewPagerAdapter.addFragments(new UserSentSplitBillFragment(), "Sent");

        mViewPager.setAdapter(viewPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> tabTitles = new ArrayList<>();

        public void addFragments (Fragment fragments,String tabTitles){
            this.fragments.add(fragments);
            this.tabTitles.add(tabTitles);
        }

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }
    }

}
