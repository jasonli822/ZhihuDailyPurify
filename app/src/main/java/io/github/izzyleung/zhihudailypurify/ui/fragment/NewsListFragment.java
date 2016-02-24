package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.task.AccelerateGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.BaseGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.OriginalGetNewsTask;

public class NewsListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, BaseGetNewsTask.UpdateUIListener {
    private List<DailyNews> newsList = new ArrayList<>();

    private NewsAdapter mAdapter;
    private String date;
    private boolean isAutoRefresh;
    private boolean isToday;
    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // onAttach 该方法在fragment被加入到activity中时由系统调用
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // THREAD_POOL_EXECUTOR 是一个corePoolSize为5的线程池，也就是说最多只有5个线程同时运行，超过
        // 5个的就要等待，所以使用executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)就跟2.3版本
        // 的AsyncTask.execute()效果是一样的
        new RecoverNewsListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            date = bundle.getString("date");
            isToday = bundle.getBoolean("first_page?");
            isSingle = bundle.getBoolean("single?");

            // 在onCreate()方法中调用了setRetainInstance(true)后，Fragment恢复时会跳过onCreate()
            // 和onDestroy()方法，因此不能在onCreate()中放置一些初始化逻辑
            setRetainInstance(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        assert view != null;
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.news_list);
        // setHasFixedSize() is used to let the RecyclerView keep the same size
        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(!isToday);

        // 设置布局管理器
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        // 设置adapter
        mAdapter = new NewsAdapter(newsList);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        // 设置手势滑动监听器
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // 设置刷新时动画的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        refreshIf((isToday || isSingle) && isAutoRefresh && !isRefreshed);
    }

    // setUserVisibleHint 可实现在fragment可见时才进行数据加载操作，即Fragment的懒加载
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refreshIf(isVisibleToUser && isAutoRefresh && !isRefreshed);
    }

    private void refreshIf(boolean prerequisite) {
        if (prerequisite) {
            doRefresh();
        }
    }

    private void doRefresh() {
        if (isToday) {
            new OriginalGetNewsTask(date, this).execute();
        } else {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                new AccelerateGetNewsTask(date, this).execute();
            } else {
                new OriginalGetNewsTask(date, this).execute();
            }
        }
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    @Override
    public void beforeTaskStart() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame) {
        mSwipeRefreshLayout.setRefreshing(false);
        isRefreshed = true;

        if (isRefreshSuccess) {
            if (!isContentSame) {
                newsList = resultList;

                mAdapter.updateNewsList(newsList);
            }
        } else if (isAdded()) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    private class RecoverNewsListTask extends AsyncTask<Void, Void, List<DailyNews>> {

        // 后台线程执行，比较耗时的操作都可以放在这里
        @Override
        protected List<DailyNews> doInBackground(Void... params) {
            return ZhihuDailyPurifyApplication.getDataSource().newsOfTheDay(date);
        }

        // 主线程执行，在这里可以使用在doInBackground得到的结果处理操作UI
        @Override
        protected void onPostExecute(List<DailyNews> newsListRecovered) {
            if (newsListRecovered != null) {
                newsList = newsListRecovered;

                for (DailyNews news : newsList) {
                    news.setDate(date);
                }

                mAdapter.updateNewsList(newsList);
            }
        }
    }
}