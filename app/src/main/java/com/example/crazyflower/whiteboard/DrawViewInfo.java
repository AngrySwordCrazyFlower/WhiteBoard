package com.example.crazyflower.whiteboard;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.crazyflower.whiteboard.Action.ActionHistoryManager;

public class DrawViewInfo implements Parcelable {

    private static final String TAG = "DrawViewInfo";

    ActionHistoryManager actionHistoryManager;

    String title;

    long lastChangeTime;

    String folderName;

    public DrawViewInfo() {
        actionHistoryManager = new ActionHistoryManager();
        title = "无名";
        lastChangeTime = System.currentTimeMillis();
        folderName = String.valueOf(lastChangeTime);
    }

    public DrawViewInfo(String folderName) {
        actionHistoryManager = new ActionHistoryManager();
        title = "无名";
        lastChangeTime = System.currentTimeMillis();
        this.folderName = folderName;
    }

    public DrawViewInfo(String folderName, String title, long lastChangeTime, ActionHistoryManager actionHistoryManager) {
        this.folderName = folderName;
        this.title = title;
        this.lastChangeTime = lastChangeTime;
        this.actionHistoryManager = actionHistoryManager;
    }

    protected DrawViewInfo(Parcel in) {
//        actionHistoryManager = in.readParcelable(ActionHistoryManager.class.getClassLoader());
        actionHistoryManager = new ActionHistoryManager(in);
        folderName = in.readString();
        title = in.readString();
        lastChangeTime = in.readLong();
    }

    public static final Parcelable.Creator<DrawViewInfo> CREATOR = new Parcelable.Creator<DrawViewInfo>() {
        @Override
        public DrawViewInfo createFromParcel(Parcel in) {
            return new DrawViewInfo(in);
        }

        @Override
        public DrawViewInfo[] newArray(int size) {
            return new DrawViewInfo[size];
        }
    };

    public ActionHistoryManager getActionHistoryManager() {
        return actionHistoryManager;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        actionHistoryManager.writeToParcel(dest, flags);
//        dest.writeParcelable(actionHistoryManager, flags);
        dest.writeString(folderName);
        dest.writeString(title);
        dest.writeLong(lastChangeTime);
    }

    @Override
    public int hashCode() {
        return folderName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof DrawViewInfo) {
            DrawViewInfo drawViewInfo = (DrawViewInfo) obj;
            return this.folderName.equals(drawViewInfo.folderName);
        }
        return false;
    }
}
