package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;

import com.example.crazyflower.whiteboard.Action.Action;

public class ActionWrapper {

    protected Action action;

    protected boolean needInvalidate;

    protected boolean holdOn;

    protected float canvasTranslateDx;
    protected float canvasTranslateDy;

    protected float canvasScale;
    protected float canvasScalePointX;
    protected float canvasScalePointY;

    public ActionWrapper() {
        this(null, false);
    }

    public ActionWrapper(Action action) {
        this(action, false);
    }

    public ActionWrapper(boolean needInvalidate) {
        this(null, needInvalidate);
    }

    public ActionWrapper(Action action, boolean needInvalidate) {
        this.action = action;
        this.needInvalidate = needInvalidate;
        this.holdOn = true;
        this.canvasTranslateDx = 0;
        this.canvasTranslateDy = 0;
        this.canvasScale = 1;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setNeedInvalidate(boolean needInvalidate) {
        this.needInvalidate = needInvalidate;
    }

    public boolean isNeedInvalidate() {
        return needInvalidate;
    }

    public void setHoldOn(boolean holdOn) {
        this.holdOn = holdOn;
    }

    public boolean isHoldOn() {
        return holdOn;
    }

    public void setCanvasScale(float canvasScale, float canvasScalePointX, float canvasScalePointY) {
        this.canvasScale = canvasScale;
        this.canvasScalePointX = canvasScalePointX;
        this.canvasScalePointY = canvasScalePointY;
    }

    public float getCanvasScale() {
        return canvasScale;
    }

    public float getCanvasScalePointX() {
        return canvasScalePointX;
    }

    public float getCanvasScalePointY() {
        return canvasScalePointY;
    }

    public void setCanvasTranslateDx(float canvasTranslateDx) {
        this.canvasTranslateDx = canvasTranslateDx;
    }

    public float getCanvasTranslateDx() {
        return canvasTranslateDx;
    }

    public void setCanvasTranslateDy(float canvasTranslateDy) {
        this.canvasTranslateDy = canvasTranslateDy;
    }

    public float getCanvasTranslateDy() {
        return canvasTranslateDy;
    }
}
