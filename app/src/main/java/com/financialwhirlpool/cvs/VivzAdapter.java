package com.financialwhirlpool.cvs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.financialwhirlpool.cvs.Class.Store;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

/**
 * Created by an vo on 6/6/2016.
 */
public class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.MyViewHolder> {
    private Context context;
    List<Store> store = Collections.emptyList();
    private LayoutInflater inflater;

    public VivzAdapter(Context context, List<Store> store) {
        this.context = context;
        this.store = store;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String[] tempArr = store.get(position).getAddress().split(" Quận");
        tempArr = tempArr[0].split(",");
        holder.textStore.setText(tempArr[0]);

        holder.imageStore.setImageResource(R.drawable.circlek);

        double tempDistance= store.get(position).getDistance()/1000.0;
        holder.textDictance.setText(String.format("%.2g",tempDistance)+" km");
    }

    @Override
    public int getItemCount() {
        if(store.size() > 7){
            return 7;
        }else {
            return store.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textStore;
        ImageView imageStore;
        TextView textDictance;

        public MyViewHolder(View itemView) {
            super(itemView);

            textStore = (TextView) itemView.findViewById(R.id.textStore);
            imageStore = (ImageView) itemView.findViewById(R.id.imageStore);
            textDictance = (TextView) itemView.findViewById(R.id.textDistance);
            RelativeLayout ln = (RelativeLayout) itemView.findViewById(R.id.frame);
            ln.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new PositionClickMessageEvent(getLayoutPosition()));
            EventBus.getDefault().post(new MessageEvent("Floating Button Clicked"));
        }
    }
    public class MessageEvent{
        public final String message;

        public MessageEvent(String message){
            this.message=message;
        }
    }
    // EVENT BUS
    public class PositionClickMessageEvent {
        public final int message;

        public PositionClickMessageEvent(int message) {
            this.message = message;
        }
    }

    // Add decor -- Line between items.
    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{android.R.attr.listDividerAlertDialog};

        private Drawable mDivider;

        public DividerItemDecoration(Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            mDivider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        public DividerItemDecoration(Drawable divider) {
            mDivider = divider;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            if (parent.getChildAdapterPosition(view) == 0) {
                return;
            }

            outRect.top = mDivider.getIntrinsicHeight();
        }

        @Override
        public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            int dividerLeft = parent.getPaddingLeft();
            int dividerRight = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount - 1; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int dividerTop = child.getBottom() + params.bottomMargin;
                int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

                mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                mDivider.draw(canvas);
            }
        }
    }
}
