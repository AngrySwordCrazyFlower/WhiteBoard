package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.Action.DeleteAction;
import com.example.crazyflower.whiteboard.Action.TransformAction;
import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.R;
import com.example.crazyflower.whiteboard.Vector2D;

import java.lang.ref.WeakReference;
import java.util.List;


public class ChosenRegionGestureListener extends GestureListener {

    private List<BasicElement> chosenElements;

    private int downPointerId;

    private float lastX, lastY;

    private PointF leftTop;
    private PointF leftBottom;
    private PointF rightTop;
    private PointF rightBottom;

    private float dx;
    private float dy;

    protected float scale;

    protected float rotate;

//    private int state;

    public static final int IDLE_STATE = 0;
    public static final int SCALING_STATE = 1;
    public static final int ROTATING_STATE = 2;
    public static final int TRANSLATING_STATE = 3;
    public static final int DELETING_STATE = 4;

    private Matrix transformMatrix;

    private Drawable deleteDrawable;
    private Drawable rotateDrawable;
    private Drawable scaleDrawable;

//    private final static int DELETE_NO_STATE = 0;
//    private final static int DELETE_DOWN_STATE = 1;

    private int state;

    private static final String TAG = "ChosenRegionGL";

    private PointF center;

    private PointF pointF;

    private WeakReference<ChooseGestureListener> chooseGestureListenerWeakReference;

//    public ChosenRegionGestureListener(DrawView drawView, List<BasicElement> chosenElements) {
//        this()
//        super(drawView);
//    }

    public ChosenRegionGestureListener(Context context, List<BasicElement> chosenElements) {
        super(context);
//        this(rectF.left, rectF.top, rectF.right, rectF.bottom, chosenElements, deleteDrawable, rotateDrawable, scaleDrawable);
        this.chosenElements = chosenElements;
        lastX = 0;
        lastY = 0;
        state = IDLE_STATE;

        Region region = new Region();
        Rect dst = new Rect();
        for (BasicElement basicElement : chosenElements) {
            basicElement.getBound().roundOut(dst);
            region.op(dst, Region.Op.UNION);
        }

        Rect bound = region.getBounds();

        float left = bound.left, top = bound.top, right = bound.right, bottom = bound.bottom;
        float centerX = (left + right) * 0.5f;
        float centerY = (top + bottom) * 0.5f;

        center = new PointF(centerX, centerY);
        transformMatrix = new Matrix();
        initTransformMatrix();
        for (BasicElement basicElement : chosenElements)
            basicElement.setChosen(transformMatrix);

        left = centerX + (left - centerX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        right = centerX + (right - centerX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        top = centerY + (top - centerY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        bottom = centerY + (bottom - centerY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;

        leftTop = new PointF(left, top);
        rightTop = new PointF(right, top);
        rightBottom = new PointF(right, bottom);
        leftBottom = new PointF(left, bottom);

//        leftTop = new PointF(bound.left, bound.top);
//        rightTop = new PointF(bound.right, bound.top);
//        rightBottom = new PointF(bound.right, bound.bottom);
//        leftBottom = new PointF(bound.left, bound.bottom);

        deleteDrawable = context.getResources().getDrawable(R.drawable.close);
        rotateDrawable = context.getResources().getDrawable(R.drawable.rotate);
        scaleDrawable = context.getResources().getDrawable(R.drawable.scale);

        state = IDLE_STATE;

        pointF = new PointF();
    }

    private void initTransformMatrix() {
        transformMatrix.reset();
        transformMatrix.postScale(DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, center.x, center.y);
    }

//    public ChosenRegionGestureListener(float left, float top, float right, float bottom, List<BasicElement> chosenElements, Drawable deleteDrawable, Drawable rotateDrawable, Drawable scaleDrawable) {
//        this.chosenElements = chosenElements;
//        downPointF = new PointF();
//        scrollPointF = new PointF();
//        state = IDLE_STATE;
//
//        float getCenterX = (left + right) * 0.5f;
//        float getCenterY = (top + bottom) * 0.5f;
//        left = getCenterX - (getCenterX - left) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        right = getCenterX + (right - getCenterX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        top = getCenterY - (getCenterY - top) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        bottom = getCenterY + (bottom - getCenterY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        leftTop = new PointF(left, top);
//        leftBottom = new PointF(left, bottom);
//        rightTop = new PointF(right, top);
//        rightBottom = new PointF(right, bottom);
//
//
//        this.state = IDLE_STATE;
//        this.canvasScale = 1;
//    }
    @Override
    public ActionWrapper onTouch(List<BasicElement> elements, MotionEvent motionEvent, Matrix matrix) {
//        Log.d(TAG, "onTouch: ");
//        Log.d(TAG, "onTouch: " + leftBottom.x + " " + leftBottom.y + " " + motionEvent.getX() + " " + motionEvent.getY());
        ActionWrapper result = null;
        float x, y;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (IDLE_STATE == state) {
//                    result = onDown(motionEvent);

                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    pointF.x = motionEvent.getX();
                    pointF.y = motionEvent.getY();
                    DrawViewUtil.transformPoint(pointF, matrix);
                    lastX = pointF.x;
                    lastY = pointF.y;
                    if (isPressScale(lastX, lastY)) {
                        startScaling();
                    } else if (isPressRotate(lastX, lastY)) {
                        startRotating();
                    } else if (isPressDelete(lastX, lastY)) {
                        startDeleting();
                    } else if (isInRect(lastX, lastY)) {
                        startTranslating();
                    } else {
                        // 既不在矩形内，也没有点击三个操作按钮
                        return null;
                    }
                    result = new ActionWrapper(false);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (IDLE_STATE != state) {


                        downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                        pointF.x = motionEvent.getX();
                        pointF.y = motionEvent.getY();
                        DrawViewUtil.transformPoint(pointF, matrix);
                        x = pointF.x;
                        y = pointF.y;
                        switch (state) {
                            case SCALING_STATE:
                                scaling(x, y);
                                break;
                            case ROTATING_STATE:
                                rotating(x, y);
                                break;
                            case TRANSLATING_STATE:
                                translating(x - lastX, y - lastY);
                                lastX = x;
                                lastY = y;
                                break;
                        }

                        result = new ActionWrapper(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (IDLE_STATE != state) {
//                        result = onScrollEnd(motionEvent);

                        if (state == TRANSLATING_STATE || state == ROTATING_STATE || state == SCALING_STATE) {
                            result = new ActionWrapper();
                            result.setAction(new TransformAction(chosenElements, getTransformMatrix()));
                            result.setNeedInvalidate(true);
                        } else if (state == DELETING_STATE && isPressDelete(motionEvent.getX(), motionEvent.getY())) {
                            for (BasicElement element : chosenElements)
                                element.cancelChosen();
                            result = new ActionWrapper();
                            result.setAction(new DeleteAction(chosenElements));
                            result.setNeedInvalidate(true);
                            result.setHoldOn(false);
                        }

                        state = IDLE_STATE;
                        initTransformMatrix();

                    }
                }
                break;

        }
        return result;
    }

//    @Override
//    public void onDraw(Canvas canvas) {
//        Log.d(TAG, "onDraw: ");
//        chooseElement.draw(canvas);
//    }

    private ActionWrapper onDown(MotionEvent motionEvent) {
        downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
        lastX = motionEvent.getX();
        lastY = motionEvent.getY();
//                    drawView.transformEventCoordinateToCanvas(motionEvent, downPointF);
        if (isPressScale(lastX, lastY)) {
            startScaling();
        } else if (isPressRotate(lastX, lastY)) {
            startRotating();
        } else if (isPressDelete(lastX, lastY)) {
            startDeleting();
        } else if (isInRect(lastX, lastY)) {
            startTranslating();
        } else {
            // 既不在矩形内，也没有点击三个操作按钮
            return null;
        }

        return new ActionWrapper(false);
    }

    private ActionWrapper onScroll(MotionEvent motionEvent) {
        float x = motionEvent.getX(), y = motionEvent.getY();
        switch (state) {
            case SCALING_STATE:
                scaling(x, y);
                break;
            case ROTATING_STATE:
                rotating(x, y);
                break;
            case TRANSLATING_STATE:
                translating(x - lastX, y - lastY);
                lastX = x;
                lastY = y;
                break;
        }
        ActionWrapper result = new ActionWrapper(true);
        return result;
    }

    private ActionWrapper onScrollEnd(MotionEvent motionEvent) {
        ActionWrapper result = null;

        if (state == TRANSLATING_STATE || state == ROTATING_STATE || state == SCALING_STATE) {
            result = new ActionWrapper();
            result.setAction(new TransformAction(chosenElements, getTransformMatrix()));
            result.setNeedInvalidate(true);
        } else if (state == DELETING_STATE && isPressDelete(motionEvent.getX(), motionEvent.getY())) {
            for (BasicElement element : chosenElements)
                element.cancelChosen();
            result = new ActionWrapper();
            result.setAction(new DeleteAction(chosenElements));
            result.setNeedInvalidate(true);
            result.setHoldOn(false);
        }

        state = IDLE_STATE;
        initTransformMatrix();

        return result;
    }

    private Matrix getTransformMatrix() {
        Matrix matrix = new Matrix();
        switch (state) {
            case TRANSLATING_STATE:
                matrix.postTranslate(dx, dy);
                break;
            case ROTATING_STATE:
                matrix.postRotate((float) Math.toDegrees(rotate), getCenterX(), getCenterY());
                break;
            case SCALING_STATE:
                matrix.postScale(scale, scale, getCenterX(), getCenterY());
                break;
        }
        return matrix;
    }

    @Override
    public ActionWrapper onCancel() {
        ActionWrapper result = new ActionWrapper();
        result.setNeedInvalidate(true);
        result.setHoldOn(false);

        if (state == TRANSLATING_STATE || state == ROTATING_STATE || state == SCALING_STATE) {
            result.setAction(new TransformAction(chosenElements, new Matrix(transformMatrix)));
        }
        for (BasicElement element : chosenElements)
            element.cancelChosen();

        initTransformMatrix();
        state = IDLE_STATE;
        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "draw: " + leftTop.x + " " + leftTop.y);

        int width = 100, height = 100;

        // draw close icon at top-right.
//        width = (int) (deleteDrawable.getIntrinsicWidth() / scale);
//        height = (int) (deleteDrawable.getIntrinsicHeight() / scale);
        Log.d(TAG, "draw: " + width + " " + height);
        deleteDrawable.setBounds((int) (rightTop.x - width * 0.5), (int) (rightTop.y - height * 0.5), (int) (rightTop.x + width * 0.5), (int) (rightTop.y + height * 0.5));
        deleteDrawable.draw(canvas);

        // draw rotate icon at left-bottom.
//        width = (int) (rotateDrawable.getIntrinsicWidth() / scale);
//        height = (int) (rotateDrawable.getIntrinsicHeight() / scale);
        rotateDrawable.setBounds((int) (leftBottom.x - width * 0.5), (int) (leftBottom.y - height * 0.5), (int) (leftBottom.x + width * 0.5), (int) (leftBottom.y + height * 0.5));
        rotateDrawable.draw(canvas);

        // draw scale icon at right-bottom.
//        width = (int) (scaleDrawable.getIntrinsicWidth() / scale);
//        height = (int) (scaleDrawable.getIntrinsicHeight() / scale);
        scaleDrawable.setBounds((int) (rightBottom.x - width * 0.5), (int) (rightBottom.y - height * 0.5), (int) (rightBottom.x + width * 0.5), (int) (rightBottom.y + height * 0.5));
        scaleDrawable.draw(canvas);
    }

//    @Override
//    public void onScaleChanged(float scale, float oldScale) {
//
//    }

    public float getCenterX() {
        return center.x;
    }

    public float getCenterY() {
        return center.y;
    }

    public int getState() {
        return state;
    }

    public boolean isInRect(float x, float y) {
        return isInRect(new PointF(x, y));
    }

    public boolean isInRect(PointF p) {
        return cross(leftTop, leftBottom, p) * cross(rightBottom, rightTop, p) >= 0
                && cross(leftBottom, rightBottom, p) * cross(rightTop, leftTop, p) >= 0;
    }

    private float cross(PointF p1, PointF p2, PointF p3) {
        return (p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y);
    }

    public boolean startScaling() {
        if (IDLE_STATE == state) {
            this.state = SCALING_STATE;
            this.scale = 1;
            return true;
        }
        return false;
    }

    public boolean scaling(float x, float y) {
        if (SCALING_STATE == state) {
            Vector2D width = new Vector2D(leftTop, rightTop);
            Vector2D height = new Vector2D(leftTop, leftBottom);

            float centerX = getCenterX();
            float centerY = getCenterY();

            Vector2D offset = new Vector2D(x - centerX, y - centerY);

            float widthOffset = width.dot(offset) / (float) Math.pow(width.length(), 2) * 2;
            float heightOffset = height.dot(offset) / (float) Math.pow(height.length(), 2) * 2;

            Log.d(TAG, "scaling: " + widthOffset + " " + heightOffset);

            float scale = Math.abs(widthOffset) > Math.abs(heightOffset) ? widthOffset : heightOffset;
            this.scale *= scale;

            transformMatrix.postScale(scale, scale, centerX, centerY);

            leftTop.x = centerX + (leftTop.x - centerX) * scale;
            leftTop.y = centerY + (leftTop.y - centerY) * scale;
            rightTop.x = centerX + (rightTop.x - centerX) * scale;
            rightTop.y = centerY + (rightTop.y - centerY) * scale;
            rightBottom.x = centerX + (rightBottom.x - centerX) * scale;
            rightBottom.y = centerY + (rightBottom.y - centerY) * scale;
            leftBottom.x = centerX + (leftBottom.x - centerX) * scale;
            leftBottom.y = centerY + (leftBottom.y - centerY) * scale;

            return true;
        }
        return false;
    }

    public boolean startTranslating() {
        if (IDLE_STATE == state) {
            this.dx = 0;
            this.dy = 0;
            state = TRANSLATING_STATE;
            return true;
        }
        return false;
    }

    public boolean translating(float dx, float dy) {
        if (TRANSLATING_STATE == state) {

            this.dx += dx;
            this.dy += dy;

            leftTop.x += dx;
            leftTop.y += dy;

            rightTop.x += dx;
            rightTop.y += dy;

            rightBottom.x += dx;
            rightBottom.y += dy;

            leftBottom.x += dx;
            leftBottom.y += dy;

            center.x += dx;
            center.y += dy;

            transformMatrix.postTranslate(dx, dy);

            return true;
        }
        return false;
    }

    public boolean startRotating() {
        if (IDLE_STATE == state) {
            this.rotate = 0;
            state = ROTATING_STATE;
        }
        return false;
    }

    public boolean rotating(float x, float y) {
        if (ROTATING_STATE == state) {
            float centerX = getCenterX();
            float centerY = getCenterY();

            Vector2D oldVector2D = new Vector2D(leftBottom.x - centerX, leftBottom.y - centerY);
            Vector2D newVector2D = new Vector2D(x - centerX, y - centerY);
            float rotate = newVector2D.getEdgeRotateAngle() - oldVector2D.getEdgeRotateAngle();
            this.rotate += rotate;

            Vector2D vector2D;
            float angle, length;
            vector2D = new Vector2D(leftTop.x - centerX, leftTop.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            leftTop.x = (float) (centerX + length * Math.cos(angle));
            leftTop.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(rightTop.x - centerX, rightTop.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            rightTop.x = (float) (centerX + length * Math.cos(angle));
            rightTop.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(rightBottom.x - centerX, rightBottom.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            rightBottom.x = (float) (centerX + length * Math.cos(angle));
            rightBottom.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(leftBottom.x - centerX, leftBottom.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            leftBottom.x = (float) (centerX + length * Math.cos(angle));
            leftBottom.y = (float) (centerY + length * Math.sin(angle));

            transformMatrix.postRotate((float) Math.toDegrees(rotate), center.x, center.y);

            return true;
        }
        return false;
    }

    public boolean startDeleting() {
        if (IDLE_STATE == state) {
            state = DELETING_STATE;
            return true;
        }
        return false;
    }

//    private double getIncludedAngle(PointF start, PointF center, PointF end) {
//        return getIncludedAngle(start.x, start.y, center.x, center.y, end.x, end.y)
//    }
//
//    private float getIncludedAngle(float startX, float startY, float getCenterX, float getCenterY, float endX, float endY) {
//        double a = Math.sqrt(Math.pow(startX - getCenterX, 2) + Math.pow(startY - getCenterY, 2));
//        double b = Math.sqrt(Math.pow(endX - getCenterX, 2) + Math.pow(endY - getCenterY, 2));
//        double c = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
//
//        float x1 = startX - getCenterY;
//        float y1 = startX - getCenterY;
//        float x2 = endX - getCenterY;
//        float y2 = endX - getCenterY;
//
//        if (x1 * y2 - x2 * y1 >= 0)
//            return (float) Math.acos((a * a + b * b - c * c) / (2 * a * b));
//        else
//            return (float) -Math.acos((a * a + b * b - c * c) / (2 * a * b));
//    }

    public boolean isPressScale(float x, float y) {
//        int width = (int) (scaleDrawable.getIntrinsicWidth() / canvasScale);
        int width = 100;
        return (Math.pow(rightBottom.x - x, 2) + Math.pow(rightBottom.y - y, 2) <= Math.pow(width * 0.5, 2));
    }

    public boolean isPressDelete(float x, float y) {
//        int width = (int) (deleteDrawable.getIntrinsicWidth() / canvasScale);
        int width = 100;
        return (Math.pow(rightTop.x - x, 2) + Math.pow(rightTop.y - y, 2) <= Math.pow(width * 0.5, 2));
    }

    public boolean isPressRotate(float x, float y) {
//        int width = (int) (scaleDrawable.getIntrinsicWidth() / canvasScale);
        Log.d(TAG, "isPressRotate: " + leftBottom.x + " " + leftBottom.y + " " + x + " " + y);
        int width = 100;
        boolean result = (Math.pow(leftBottom.x - x, 2) + Math.pow(leftBottom.y - y, 2) <= Math.pow(width * 0.5, 2));
        Log.d(TAG, "isPressRotate: " + result);
        return result;
    }

    public List<BasicElement> getChosenElements() {
        return chosenElements;
    }

}



