package com.example.crazyflower.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener, DragHelperView.DragHelperViewListener {

    private static final String TAG = "DrawActivity";

    public static final int DRAW_REQUEST_CODE = 15;

    public static final int ADD_DRAW_REQUEST_CODE = 16;

    public static final String DRAW_VIEW_INFO = "draw_view_info";

    public static final String DRAW_VIEW_ADAPTER_POSITION = "draw_view_adapter_position_in_data";

    DrawView drawView;

    DrawViewInfo drawViewInfo;

    ImageView iconPaint;
    ImageView iconChoose;
    ImageView iconScaleTranslating;
    ImageView iconActionBack;
    ImageView iconActionGo;
    ImageView iconPicturePick;

    DragHelperView dragHelperView;

    private static final int PICK_LOCAL_PICTURE_REQUEST_CODE = 1000;
    private static final int CLIP_PICTURE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        iconPicturePick = findViewById(R.id.picture_pick);

        drawView = (DrawView) findViewById(R.id.board);
        iconPaint = findViewById(R.id.mode_paint);
        iconChoose = findViewById(R.id.mode_choose);
        iconScaleTranslating = findViewById(R.id.mode_scale_translate);
        iconActionBack = findViewById(R.id.action_back);
        iconActionGo = findViewById(R.id.action_go);
        dragHelperView = findViewById(R.id.drag_helper);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(DrawActivity.DRAW_VIEW_INFO);
        if (bundle == null)
            finish();
        else {
            drawViewInfo = bundle.getParcelable(DRAW_VIEW_INFO);
            if (null == drawViewInfo)
                finish();
            else
                drawView.setActionHistoryManager(drawViewInfo.getActionHistoryManager());
        }
//        drawView.setActionHistoryManager(new ActionHistoryManager());

    }

    @Override
    protected void onStart() {
        super.onStart();
        iconPaint.setOnClickListener(this);
        iconChoose.setOnClickListener(this);
        iconScaleTranslating.setOnClickListener(this);
        iconActionBack.setOnClickListener(this);
        iconActionGo.setOnClickListener(this);
        dragHelperView.setDragHelperViewListener(this);
        iconPicturePick.setOnClickListener(this);
    }

    @Override
    public void finish() {
        Intent data = null;
        if (drawView.getActionHistoryManager().isChanged()) {
            data = new Intent();
            data.putExtra(DrawActivity.DRAW_VIEW_INFO, drawViewInfo);
        }
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToFile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        iconPaint.setOnClickListener(null);
        iconChoose.setOnClickListener(null);
        iconScaleTranslating.setOnClickListener(null);
        iconActionBack.setOnClickListener(null);
        iconActionGo.setOnClickListener(null);
        iconPicturePick.setOnClickListener(null);
        dragHelperView.setDragHelperViewListener(null);
    }

    private void saveToFile() {
        drawViewInfo.saveToFile();
        Intent data = new Intent();
        data.putExtra(DRAW_VIEW_INFO, drawViewInfo);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mode_paint:
                if (DrawView.PAINT_MODE == drawView.getMode()) {
                    new PaintChoosePopupWindow().show();
                } else {
                    drawView.setMode(DrawView.PAINT_MODE);
                    iconPaint.setActivated(true);
                    iconChoose.setActivated(false);
                    iconScaleTranslating.setActivated(false);
                }
                break;
            case R.id.mode_choose:
                drawView.setMode(DrawView.CHOOSE_MODE);
                iconPaint.setActivated(false);
                iconChoose.setActivated(true);
                iconScaleTranslating.setActivated(false);
                break;
            case R.id.picture_pick:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PICK_LOCAL_PICTURE_REQUEST_CODE);
                break;
            case R.id.mode_scale_translate:
                drawView.setMode(DrawView.SCALE_TRANSLATING_MODE);
                iconPaint.setActivated(false);
                iconChoose.setActivated(false);
                iconScaleTranslating.setActivated(true);
                break;
            case R.id.action_back:
                drawView.undo();
                break;
            case R.id.action_go:
                drawView.redo();
                break;
        }
    }

    @Override
    public void onMove(float dx, float dy) {
        if (null != drawView)
            drawView.scroll(dx, dy, true);
    }

    private class PaintChoosePopupWindow implements View.OnClickListener, MySeekBar.OnSeekBarProgressListener, PopupWindow.OnDismissListener {

        LinearLayout contentView;
        PopupWindow popupWindow;

        PaintChoosePopupWindow() {
            Activity outer = DrawActivity.this;
            contentView = (LinearLayout) outer.getLayoutInflater().inflate(R.layout.popupwindow_paint_choose, (ViewGroup) outer.getWindow().getDecorView(), false);

            MySeekBar mySeekBar = contentView.findViewById(R.id.line_weight_seek_bar);
            mySeekBar.setProgress((drawView.getPathStrokeWidth() - DrawViewUtil.PAINT_MIN_STROKE_WIDTH) / (DrawViewUtil.PAINT_MAX_STROKE_WIDTH - DrawViewUtil.PAINT_MIN_STROKE_WIDTH) * 100);
            mySeekBar.setColor(drawView.getPathColor());
            mySeekBar.setOnSeekBarProgressListener(this);

            contentView.findViewById(R.id.line_color_aqua).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_black).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_blue).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_green).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_purple).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_red).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_white).setOnClickListener(this);
            contentView.findViewById(R.id.line_color_yellow).setOnClickListener(this);

            View view = contentView.findViewById(R.id.line_weight_show);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (int) (DrawViewUtil.PAINT_MIN_STROKE_WIDTH + (DrawViewUtil.PAINT_MAX_STROKE_WIDTH - DrawViewUtil.PAINT_MIN_STROKE_WIDTH) * mySeekBar.getProgress() * 0.01f);
            view.setLayoutParams(layoutParams);
            GradientDrawable background = (GradientDrawable) view.getBackground();
            background.setColor(drawView.getPathColor());

            popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setClippingEnabled(false);
            popupWindow.setBackgroundDrawable(null);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(false);

            popupWindow.setOnDismissListener(this);

        }


        @Override
        public void onClick(View v) {
            View view = contentView.findViewById(R.id.line_weight_show);
            GradientDrawable background = (GradientDrawable) view.getBackground();
            switch (v.getId()) {
                case R.id.line_color_aqua:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(0, 255, 255));
                    background.setColor(Color.rgb(0, 255, 255));
                    break;
                case R.id.line_color_black:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(0, 0, 0));
                    background.setColor(Color.rgb(0, 0, 0));
                    break;
                case R.id.line_color_blue:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(0, 0, 255));
                    background.setColor(Color.rgb(0, 0, 255));
                    break;
                case R.id.line_color_green:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(0, 255, 0));
                    background.setColor(Color.rgb(0, 255, 0));
                    break;
                case R.id.line_color_purple:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(255, 0, 255));
                    background.setColor(Color.rgb(255, 0, 255));
                    break;
                case R.id.line_color_red:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(255, 0, 0));
                    background.setColor(Color.rgb(255, 0, 0));
                    break;
                case R.id.line_color_white:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(255, 255, 255));
                    background.setColor(Color.rgb(255, 255, 255));
                    break;
                case R.id.line_color_yellow:
                    ((MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar)).setColor(Color.rgb(255, 255, 0));
                    background.setColor(Color.rgb(255, 255, 0));
                    break;
            }
        }

        @Override
        public void onSeekBarProgressChanged(MySeekBar mySeekBar) {
            Log.d(TAG, "onSeekBarProgressChanged: ");
            View view = contentView.findViewById(R.id.line_weight_show);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (int) (DrawViewUtil.PAINT_MIN_STROKE_WIDTH + (DrawViewUtil.PAINT_MAX_STROKE_WIDTH - DrawViewUtil.PAINT_MIN_STROKE_WIDTH) * mySeekBar.getProgress() * 0.01f);
            view.setLayoutParams(layoutParams);
            drawView.setPathColor(mySeekBar.getColor());
        }

        @Override
        public void onDismiss() {
            MySeekBar mySeekBar = (MySeekBar) contentView.findViewById(R.id.line_weight_seek_bar);
            drawView.setPathColor(mySeekBar.getColor());
            drawView.setPathStrokeWidth(DrawViewUtil.PAINT_MIN_STROKE_WIDTH + (DrawViewUtil.PAINT_MAX_STROKE_WIDTH - DrawViewUtil.PAINT_MIN_STROKE_WIDTH) * mySeekBar.getProgress() * 0.01f);
        }

        public void show() {
            int[] outLocation = new int[2];
            drawView.getLocationInWindow(outLocation);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.AT_MOST));

            popupWindow.showAtLocation(drawView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, drawView.getHeight() - contentView.getMeasuredHeight() + outLocation[1]);
        }

    }

    /**
     * 抄的，是针对Android 7.0以上的
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            return;
        switch (requestCode) {
            case PICK_LOCAL_PICTURE_REQUEST_CODE:
                if (null != data) {
                    cropPic(data.getData());
                }
                break;
            case CLIP_PICTURE_REQUEST_CODE:
                // 裁剪时,这样设置 cropIntent.putExtra("return-data", true); 处理方案如下
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Bitmap bitmap = bundle.getParcelable("data");
                        drawView.addBitmapElement(bitmap);
                    }
                }
                break;
        }
    }

    /**
     * 抄的，是针对Android 7.0以上的
     * @param data
     */
    private void cropPic(Uri data) {
        if (data == null) {
            return;
        }
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(data, "image/*");

        // 开启裁剪：打开的Intent所显示的View可裁剪
        cropIntent.putExtra("crop", "true");
        // 裁剪宽高比
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // 裁剪输出大小
        cropIntent.putExtra("outputX", 320);
        cropIntent.putExtra("outputY", 320);
        cropIntent.putExtra("scale", true);
        /**
         * return-data
         * 这个属性决定我们在 onActivityResult 中接收到的是什么数据，
         * 如果设置为true 那么data将会返回一个bitmap
         * 如果设置为false，则会将图片保存到本地并将对应的uri返回，当然这个uri得有我们自己设定。
         * 系统裁剪完成后将会将裁剪完成的图片保存在我们所这设定这个uri地址上。我们只需要在裁剪完成后直接调用该uri来设置图片，就可以了。
         */
        cropIntent.putExtra("return-data", true);
        // 当 return-data 为 false 的时候需要设置这句
//        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // 图片输出格式
//        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 头像识别 会启动系统的拍照时人脸识别
//        cropIntent.putExtra("noFaceDetection", true);
        startActivityForResult(cropIntent, CLIP_PICTURE_REQUEST_CODE);
    }


}
