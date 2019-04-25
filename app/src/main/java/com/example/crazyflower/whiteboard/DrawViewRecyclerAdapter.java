package com.example.crazyflower.whiteboard;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class DrawViewRecyclerAdapter extends RecyclerView.Adapter<DrawViewRecyclerAdapter.DrawViewHolder> implements View.OnClickListener {

    private static final String TAG = "DrawViewRecyclerAdapter";

    List<DrawViewInfo> drawViewInfos;

    DateFormat dateFormat;

    RecyclerViewItemChildViewClickListener recyclerViewItemChildViewClickListener;

    public DrawViewRecyclerAdapter() {
        drawViewInfos = new ArrayList<>();
        dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public DrawViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.draw_view_item, parent, false);
        return new DrawViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrawViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        DrawViewInfo drawViewInfo = drawViewInfos.get(position);
        holder.drawView.setActionHistoryManager(drawViewInfo.actionHistoryManager);
        holder.titleTextView.setText(drawViewInfo.title);
        holder.lastModifiedTextView.setText(dateFormat.format(new Date(drawViewInfo.lastChangeTime)));
        holder.container.setTag(position);
        holder.container.setOnClickListener(this);
        holder.titleTextView.setTag(position);
        holder.titleTextView.setOnClickListener(this);
        holder.moreDetailImageView.setTag(position);
        holder.moreDetailImageView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return drawViewInfos.size();
    }

    @Override
    public void onClick(View v) {
        if (null == recyclerViewItemChildViewClickListener)
            return;
        Object tag = v.getTag();
        if (tag instanceof Integer) {
            int position = (int) tag;
            recyclerViewItemChildViewClickListener.onChildViewClick(v, position);
        }
    }

    public void setRecyclerViewItemChildViewClickListener(RecyclerViewItemChildViewClickListener recyclerViewItemChildViewClickListener) {
        this.recyclerViewItemChildViewClickListener = recyclerViewItemChildViewClickListener;
    }

    public static class DrawViewHolder extends RecyclerView.ViewHolder {

        View container;
        DrawView drawView;
        TextView titleTextView;
        TextView lastModifiedTextView;
        ImageView moreDetailImageView;

        public DrawViewHolder(View itemView) {
            super(itemView);
            container = (CardView) itemView;
            drawView = (DrawView) itemView.findViewById(R.id.draw_view_sketch);
            titleTextView = (TextView) itemView.findViewById(R.id.draw_view_title);
            lastModifiedTextView = (TextView) itemView.findViewById(R.id.draw_view_last_change);
            moreDetailImageView = (ImageView) itemView.findViewById(R.id.draw_view_detail_more);
        }

    }

//    public static class DrawViewRVItemTouchListener implements RecyclerView.OnItemTouchListener {
//
//        private static final String TAG = "DrawViewRVItemTouch";
//        /**
//         * Count 32 bits from 0 to 31, mapping from right to left.
//         * E.g. If the 4th right most is 0, it means the finger whose id is 4 is on down.
//         * If the 3rd right most is 1, it means the finger whose id is 3 has moved.
//         * If the finger whose id is 4 don't happen, then the 4th bit is nonsensical.
//         */
//        private int fingerState = 0;
//
//        @Override
//        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//            int id = e.getPointerId(e.getActionIndex());
//            int temp = 1 >> id;
//            boolean intercept = false;
//            Log.d(TAG, "onInterceptTouchEvent: actionMask:" + e.getActionMasked() + " id: " + id);
//            switch (e.getActionMasked()) {
//                case MotionEvent.ACTION_DOWN:
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    fingerState &= ~temp;
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    fingerState |= temp;
//                    break;
//                case MotionEvent.ACTION_POINTER_UP:
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_CANCEL:
//                    if ((fingerState & temp) == 0) {
//                        Log.d(TAG, "onInterceptTouchEvent: " + rv.findChildViewUnder(e.getX(), e.getY()).getClass().getName());
//                        fingerState &= ~(1 >> id);
//                        intercept = true;
//                    }
//                    break;
//            }
//            return intercept;
//        }
//
//        @Override
//        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//        }
//
//        @Override
//        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//        }
//    }

    public List<DrawViewInfo> getData() {
        return drawViewInfos;
    }

    public void updateItem(DrawViewInfo drawViewInfo) {
        int index = drawViewInfos.indexOf(drawViewInfo);
        Log.d(TAG, "updateItem: " + index);
        if (index != -1) {
            drawViewInfos.remove(index);
            drawViewInfos.add(0, drawViewInfo);
            notifyItemRangeChanged(0, index + 1);
        }
    }

    public void addItem(DrawViewInfo drawViewInfo) {
        drawViewInfos.add(drawViewInfo);
        try {
            Log.d(TAG, "addItem: " + drawViewInfo.folderName + " " + drawViewInfo.getActionHistoryManager().getActionJSONArray().toString() + " " + drawViewInfo.title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyItemInserted(drawViewInfos.size() - 1);
    }

}
