package com.example.crazyflower.whiteboard;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.List;

public class Vector2D {

    float x;
    float y;

    public Vector2D(PointF pointF) {
        this.x = pointF.x;
        this.y = pointF.y;
    }

    public Vector2D(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Vector2D(PointF from, PointF to) {
        this.x = to.x - from.x;
        this.y = to.y - from.y;
    }

    public Vector2D(Point from, Point to) {
        this.x = to.x - from.x;
        this.y = to.y - from.y;
    }

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector2D) {
        this.x = vector2D.x;
        this.y = vector2D.y;
    }

    public float getEdgeRotateAngle() {
        double result =  Math.asin(y / length());
        if (x < 0) {
            result = Math.PI - result;
        }
        return (float) result;
    }

    public float dot(Vector2D vector2D) {
        return this.x * vector2D.x + this.y * vector2D.y;
    }

    public float cross(Vector2D vector2D) {
        return this.x * vector2D.y - this.y - vector2D.x;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

}
