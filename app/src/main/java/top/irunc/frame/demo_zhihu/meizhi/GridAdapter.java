package top.irunc.frame.demo_zhihu.meizhi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import top.irunc.frame.R;

/**
 * Created by 赵晨璞 on 2016/6/19.
 *
 */

public  class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private ArrayList<Meizi> datas;

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view);
        void onItemLongClick(View view);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    public GridAdapter(Context context, ArrayList<Meizi> data) {
        mContext=context;
        this.datas=data;
    }

    public void setDatas(ArrayList<Meizi> data)
    {
        this.datas = data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if(viewType==0){
            View view = LayoutInflater.from(mContext
                    ).inflate(R.layout.grid_meizi_item, parent,
                    false);
            MyViewHolder holder = new MyViewHolder(view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            return holder;
        }else{
            MyViewHolder2 holder2=new MyViewHolder2(LayoutInflater.from(
                    mContext).inflate(R.layout.page_item, parent,
                    false));
            return holder2;
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){
            Picasso.with(mContext).load(datas.get(position).getUrl()).into(((MyViewHolder) holder).iv,
                    new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            ((MyViewHolder) holder).pb.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            ((MyViewHolder) holder).pb.setVisibility(View.GONE);
                        }
                    });
        }else if(holder instanceof MyViewHolder2){
            ((MyViewHolder2) holder).tv.setText(datas.get(position).getPage()+"页");
        }

    }

    @Override
    public int getItemCount()
    {
        return datas.size();
    }

    @Override
    public int getItemViewType(int position) {

        //判断item是图还是显示页数（图片有URL）
        if (!TextUtils.isEmpty(datas.get(position).getUrl())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {

            mOnItemClickListener.onItemClick(v);
        }

    }


    @Override
    public boolean onLongClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemLongClick(v);
        }
        return false;
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView iv;
        private ProgressBar pb;

        public MyViewHolder(View view)
        {
            super(view);
            iv = (ImageView) view.findViewById(R.id.iv);
            pb = (ProgressBar) view.findViewById(R.id.progressBar2);
        }
    }
    class MyViewHolder2 extends RecyclerView.ViewHolder
    {
        private TextView tv;

        public MyViewHolder2(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.tv);
        }
    }

}
