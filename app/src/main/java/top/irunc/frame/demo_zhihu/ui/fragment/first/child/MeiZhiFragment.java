package top.irunc.frame.demo_zhihu.ui.fragment.first.child;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.gridviewimage.view.acitvity.MaxPictureActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;
import top.irunc.frame.R;
import top.irunc.frame.demo_zhihu.MainActivity;
import top.irunc.frame.demo_zhihu.adapter.FirstHomeAdapter;
import top.irunc.frame.demo_zhihu.entity.Article;
import top.irunc.frame.demo_zhihu.event.TabSelectedEvent;
import top.irunc.frame.demo_zhihu.helper.DetailTransition;
import top.irunc.frame.demo_zhihu.listener.OnItemClickListener;
import top.irunc.frame.demo_zhihu.meizhi.GridActivity;
import top.irunc.frame.demo_zhihu.meizhi.GridAdapter;
import top.irunc.frame.demo_zhihu.meizhi.Meizi;
import top.irunc.frame.demo_zhihu.meizhi.MyOkhttp;
import top.irunc.frame.demo_zhihu.meizhi.SnackbarUtil;

/**
 * Created by yokeyword on 16/6/5.
 */
public class MeiZhiFragment extends SupportFragment {
    private static RecyclerView recyclerview;
    private GridAdapter mAdapter;
    private ArrayList<Meizi> meizis = new ArrayList<Meizi>();
    private ArrayList<String> photos = new ArrayList<String>();
    private StaggeredGridLayoutManager mLayoutManager;
//    private GridLayoutManager mLayoutManager;
    private int lastVisibleItem;
    private int page=1;
    private ItemTouchHelper itemTouchHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar mToolbar;

    public static MeiZhiFragment newInstance() {

        Bundle args = new Bundle();

        MeiZhiFragment fragment = new MeiZhiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_grid, container, false);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        Picasso.with(_mActivity).setIndicatorsEnabled(true);
        initView(view);
        setListener();
        new GetData().execute("http://gank.io/api/data/福利/10/1");
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        recyclerview=(RecyclerView)view.findViewById(R.id.grid_recycler);
        mLayoutManager=new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        mLayoutManager=new GridLayoutManager(_mActivity,2, GridLayoutManager.VERTICAL,false);
        recyclerview.setLayoutManager(mLayoutManager);

        swipeRefreshLayout=(SwipeRefreshLayout) view.findViewById(R.id.grid_swipe_refresh) ;
        //调整SwipeRefreshLayout的位置
        swipeRefreshLayout.setProgressViewOffset(false, 0,  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mToolbar.setTitle(R.string.home);

        mAdapter = new GridAdapter(_mActivity,meizis);
        mAdapter.setOnItemClickListener(new GridAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view) {
                int position=recyclerview.getChildAdapterPosition(view);
                photos.clear();
                for (Meizi m :meizis)
                {
                    photos.add(m.getUrl());
                }
                Intent in = new Intent();
                in.setClass(_mActivity, MaxPictureActivity.class);
                //Will pass, I click for the current position
                in.putExtra("pos", position);
                //Will pass,Photos to show the pictures of the collection address
                in.putStringArrayListExtra("imageAddress", photos);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(in, ActivityOptions.makeSceneTransitionAnimation
                            (_mActivity).toBundle());
                } else {
                    startActivity(in);
                }
            }

            @Override
            public void onItemLongClick(View view) {
                itemTouchHelper.startDrag(recyclerview.getChildViewHolder(view));
            }
        });
        recyclerview.setAdapter(mAdapter);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0;
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager || recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                }
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Meizi moveItem = meizis.get(from);
                meizis.remove(from);
                meizis.add(to, moveItem);
                mAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerview);
    }

    private void scrollToTop() {
        recyclerview.smoothScrollToPosition(0);
    }

    private void setListener() {

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                meizis.clear();
                new GetData().execute("http://gank.io/api/data/福利/10/1");
            }
        });
        //recyclerview滚动监听
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余少于两个item时，自动加载下一页
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 2 >= mLayoutManager.getItemCount()) {
                    new GetData().execute("http://gank.io/api/data/福利/10/" + (++page));
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                获取加载的最后一个可见视图在适配器的位置。
                int[] positions= mLayoutManager.findLastVisibleItemPositions(null);
                lastVisibleItem = Math.max(positions[0],positions[1]);
//                  lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            }
        });
    }
    private class GetData extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //设置swipeRefreshLayout为刷新状态
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {

            return MyOkhttp.get(params[0]);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!TextUtils.isEmpty(result)){

                JSONObject jsonObject;
                Gson gson=new Gson();
                String jsonData=null;

                try {
                    jsonObject = new JSONObject(result);
                    jsonData = jsonObject.getString("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(meizis==null||meizis.size()==0){
                    meizis= gson.fromJson(jsonData, new TypeToken<List<Meizi>>() {}.getType());
                }else{
                    List<Meizi> more= gson.fromJson(jsonData, new TypeToken<List<Meizi>>() {}.getType());
                    meizis.addAll(more);
                }
                mAdapter.setDatas(meizis);
                mAdapter.notifyDataSetChanged();
            }
            //停止swipeRefreshLayout加载动画
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 选择tab事件
     */
    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        if (event.position != MainActivity.FIRST) return;
        scrollToTop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }
}
