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

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AppBarLayout appBarLayout;

    private TextView titleTextView;

    DrawViewRecyclerAdapter drawViewRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        drawViewRecyclerAdapter = new DrawViewRecyclerAdapter();
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.draw_view_items));
        recyclerView.setAdapter(drawViewRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        File appDataFolder = getFilesDir();
        File drawViewFolder;
        DrawViewInfo drawViewInfo;
        for (String drawViewId : appDataFolder.list()) {
            drawViewFolder = new File(appDataFolder, drawViewId);
            drawViewInfo = DrawViewInfo.generateByDrawViewFolder(drawViewFolder);
            if (drawViewInfo != null)
                drawViewRecyclerAdapter.addItem(drawViewInfo);
        }

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
                try {
                    Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DrawActivity.DRAW_VIEW_INFO, DrawViewInfo.generateByDrawViewFolder(createDrawViewFolder()));
                    intent.putExtra(DrawActivity.DRAW_VIEW_INFO, bundle);
                    startActivityForResult(intent, DrawActivity.ADD_DRAW_REQUEST_CODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + (data == null));
        switch (requestCode) {
            case DrawActivity.ADD_DRAW_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    drawViewRecyclerAdapter.addItem((DrawViewInfo) data.getParcelableExtra(DrawActivity.DRAW_VIEW_INFO));
                }
                break;
            case DrawActivity.DRAW_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    drawViewRecyclerAdapter.updateItem((DrawViewInfo) data.getParcelableExtra(DrawActivity.DRAW_VIEW_INFO));
                }
                break;
        }
    }

    private File createDrawViewFolder() throws IOException {
        File file = new File(getFilesDir(), String.valueOf(System.currentTimeMillis()));
        if (!file.exists())
            if (file.mkdirs())
                return file;
        throw new IOException("Can't create new draw view folder");
    }
}