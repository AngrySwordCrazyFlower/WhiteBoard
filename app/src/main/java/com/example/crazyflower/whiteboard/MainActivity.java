package com.example.crazyflower.whiteboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.crazyflower.whiteboard.Action.ActionHistoryManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AppBarLayout appBarLayout;

    private TextView titleTextView;

    DrawViewRecyclerAdapter drawViewRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        DrawViewInfoManager drawViewInfoManager = DrawViewInfoManager.getInstance();
        File folder = getFilesDir(), infoFile;
        ActionHistoryManager actionHistoryManager;
        drawViewRecyclerAdapter = new DrawViewRecyclerAdapter();
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.draw_view_items));
        recyclerView.setAdapter(drawViewRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        JSONObject info;
        String title;
        long lastChangeTime;
        for (String s : folder.list()) {
            try {
                infoFile = new File(folder.getPath() + File.separator + s + File.separator + "info.json");
                if (infoFile.exists()) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(infoFile));
                    StringBuilder infoFromFile = new StringBuilder();
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = bufferedInputStream.read(bytes)) != -1) {
                        infoFromFile.append(Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes, 0, length)));
                    }
                    info = new JSONObject(infoFromFile.toString());
                    actionHistoryManager = ActionHistoryManager.generateByActionJSONArray(info.getJSONArray("actions"));
                    Log.d(TAG, "onCreate: " + actionHistoryManager.getActionJSONArray());
                    title = info.getString("title");
                    lastChangeTime = infoFile.lastModified();
                    drawViewRecyclerAdapter.addItem(new DrawViewInfo(s, title, lastChangeTime, actionHistoryManager));
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        info = null;
        title = null;
        actionHistoryManager = null;

        drawViewRecyclerAdapter.setRecyclerViewItemChildViewClickListener(new RecyclerViewItemChildViewClickListener() {
            @Override
            public void onChildViewClick(View view, int adapterPosition) {
                Log.d(TAG, "onChildViewClick: " + view.getId());
                switch (view.getId()) {
                    case R.id.draw_view_item_container:
                        Log.d(TAG, "onChildViewClick: " + adapterPosition);
                        Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(DrawActivity.DRAW_VIEW_INFO, drawViewRecyclerAdapter.getData().get(adapterPosition));
                        intent.putExtra(DrawActivity.DRAW_VIEW_INFO, bundle);
                        startActivityForResult(intent, DrawActivity.DRAW_REQUEST_CODE);
                        break;
                }
            }
        });

//        recyclerView.addOnItemTouchListener(new DrawViewRecyclerAdapter.DrawViewRVItemTouchListener());
//        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            private static final String TAG = "OnItemTouchListener";
//
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
////                View view = rv.findChildViewUnder(e.getX(), e.getY());
////                if (null == view)
////                    return false;
////                Log.d(TAG, "onInterceptTouchEvent: " + e.getAction() + " " + rv.getChildAdapterPosition(view));
//                Log.d(TAG, "onInterceptTouchEvent: " + e.getAction());
//                boolean result;
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        result = false;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        result = false;
//                        break;
//                    default:
//                        result = false;
//                        break;
//                }
//                return result;
//            }
//
//            @Override
//            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//                Log.d(TAG, "onTouchEvent: " + e.getAction());
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//        });

        appBarLayout = ((AppBarLayout) findViewById(R.id.app_bar));
        titleTextView = (TextView) findViewById(R.id.activity_title);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                titleTextView.setAlpha(1 + verticalOffset / (float) appBarLayout.getTotalScrollRange());
            }
        });

        findViewById(R.id.add_draw_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = String.valueOf(createFolder());
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DrawActivity.DRAW_VIEW_INFO, new DrawViewInfo(folderName));
                intent.putExtra(DrawActivity.DRAW_VIEW_INFO, bundle);
                startActivityForResult(intent, DrawActivity.DRAW_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + (data == null));
        if (resultCode == RESULT_OK && data != null) {
            drawViewRecyclerAdapter.updateItem((DrawViewInfo) data.getParcelableExtra(DrawActivity.DRAW_VIEW_INFO));
        }
    }

    private long createFolder() {
        File file;
        long time;
        do {
            time = System.currentTimeMillis();
            file = new File(getFilesDir().getPath() + File.separator + time);
        } while (!file.mkdirs());
        return time;
    }
}