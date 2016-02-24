package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.DateFormat;
import java.util.Calendar;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;

public class MainActivity extends BaseActivity {
    private static final int PAGE_COUNT = 7;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutResID = R.layout.activity_main;

        super.onCreate(savedInstanceState);

        TabLayout tabs = (TabLayout) findViewById(R.id.main_pager_tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        // 设置ViewPager幕后item的缓存数目
        viewPager.setOffscreenPageLimit(7);

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter); // 给ViewPager设置适配器
        tabs.setupWithViewPager(viewPager); // 将TabLayout和ViewPager关联起来

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_pick_date);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareIntent(PortalActivity.class);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return prepareIntent(PrefsActivity.class);
            case R.id.action_go_to_search:
                return prepareIntent(SearchActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean prepareIntent(Class clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
        return true;
    }

    // 注意 FragmentPagerAdapter与FragmentStatePagerAdapter区别。FragmentStatePagerAdapter和ListView
    // 有点类似，会保存当前页面，以及下一个界面和上一个界面（如果有），最多保存3个，其他会被销毁掉。
    private class MainPagerAdapter extends FragmentStatePagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            Fragment newFragment = new NewsListFragment();

            Calendar dateToGetUrl = Calendar.getInstance();
            dateToGetUrl.add(Calendar.DAY_OF_YEAR, 1 - i);
            String date = Constants.Date.simpleDateFormat.format(dateToGetUrl.getTime());

            bundle.putBoolean("first_page?", i == 0);
            bundle.putBoolean("single?", false);
            bundle.putString("date", date);

            newFragment.setArguments(bundle);
            return newFragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar displayDate = Calendar.getInstance();
            displayDate.add(Calendar.DAY_OF_YEAR, -position);

            return (position == 0 ? getString(R.string.zhihu_daily_today) + " " : "")
                    + DateFormat.getDateInstance().format(displayDate.getTime());
        }
    }
}