package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;
import io.github.izzyleung.zhihudailypurify.ui.fragment.PickDateFragment;

public class PortalActivity extends BaseActivity
        implements PickDateFragment.PickDateListener {
    private static final int ACTION_PREVIOUS_DAY = 0, ACTION_NEXT_DAY = 1;

    private String dateForFragment;

    private Calendar calendar = Calendar.getInstance();
    private MenuItem prev, next;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 给左上角图标的左边加上一个返回的图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showPickDateFragment();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            for (int i = getSupportFragmentManager().getBackStackEntryCount(); i > 0; i--) {
                getSupportFragmentManager().popBackStack();
            }

            mToolBar.setTitle(R.string.action_pick_date);
            prev.setVisible(false);
            next.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_date, menu);
        prev = menu.findItem(R.id.back);
        next = menu.findItem(R.id.forward);

        prev.setVisible(false);
        next.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.forward:
                if (isSameDay(Calendar.getInstance(), calendar)) {
                    showToast(R.string.this_is_today);
                    return true;
                }
                updateFields(ACTION_NEXT_DAY);
                updateView();
                return true;
            case R.id.back:
                if (isSameDay(Constants.Date.birthday, calendar.getTime())) {
                    showToast(R.string.this_is_birthday);
                    return true;
                }
                updateFields(ACTION_PREVIOUS_DAY);
                updateView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPickDateFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("date", Constants.Date.simpleDateFormat.format(calendar.getTime()));

        Fragment displayFragment = new PickDateFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_frame, displayFragment)
                .commit();

        mToolBar.setTitle(R.string.action_pick_date);
    }

    private void updateFields(int action) {
        if (action == ACTION_NEXT_DAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        } else if (action == ACTION_PREVIOUS_DAY) {
            dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void updateView() {
        Bundle bundle = new Bundle();

        bundle.putBoolean("auto_refresh?", true);
        bundle.putBoolean("single?", true);
        bundle.putString("date", dateForFragment);
        bundle.putBoolean("first_page?", isSameDay(calendar, Calendar.getInstance()));

        Fragment displayFragment = new NewsListFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_frame, displayFragment)
                .addToBackStack(null)
                .commit();

        String displayDate = DateFormat.getDateInstance().format(calendar.getTime());

        mToolBar.setTitle(displayDate);
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)) &&
                (first.get(Calendar.MONTH) == second.get(Calendar.MONTH)) &&
                (first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isSameDay(Date first, Date second) {
        return first.equals(second);
    }

    @Override
    public void onValidDateSelected(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        prev.setVisible(true);
        next.setVisible(true);

        updateView();
    }

    @Override
    public void onInvalidDateSelected(Date date) {
        if (date.after(new Date())) {
            showToast(R.string.not_coming);
        } else {
            showToast(R.string.not_born);
        }
    }

    @Override
    public Date getDate() {
        return calendar.getTime();
    }

    private void showToast(int resId) {
        Toast.makeText(PortalActivity.this, getString(resId), Toast.LENGTH_SHORT).show();
    }
}