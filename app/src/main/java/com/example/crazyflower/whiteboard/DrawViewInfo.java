package com.example.crazyflower.whiteboard;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.crazyflower.whiteboard.Action.ActionHistoryManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DrawViewInfo implements Parcelable {

    private static final String TAG = "DrawViewInfo";

    private static final String JSON_HISTORY = "action_history";

    private static final String JSON_TITLE = "draw_view_title";

    ActionHistoryManager actionHistoryManager;

    String title;

    String drawViewFolderPath;

//    public DrawViewInfo() {
//        actionHistoryManager = new ActionHistoryManager();
//        title = "无名";
//        drawViewFolderPath = null;
//    }

//    public DrawViewInfo(String drawViewFolderPath) {
//        actionHistoryManager = new ActionHistoryManager();
//        title = "无名";
//        this.drawViewFolderPath = drawViewFolderPath;
//    }



    private DrawViewInfo(String drawViewFolderPath) {
        this.drawViewFolderPath = drawViewFolderPath;
        this.title = "无名";
        this.actionHistoryManager = new ActionHistoryManager();
    }

    private DrawViewInfo(String drawViewFolderPath, String title, ActionHistoryManager actionHistoryManager) {
        this.drawViewFolderPath = drawViewFolderPath;
        this.title = title;
        this.actionHistoryManager = actionHistoryManager;
    }

    protected DrawViewInfo(Parcel in) {
//        actionHistoryManager = in.readParcelable(ActionHistoryManager.class.getClassLoader());
        actionHistoryManager = new ActionHistoryManager(in);
        drawViewFolderPath = in.readString();
        title = in.readString();
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

    public String getDrawViewFolderPath() {
        return drawViewFolderPath;
    }

    public String getTitle() {
        return title;
    }

    public void saveToFile() {
        File folder = new File(drawViewFolderPath);
        try {
            if (!folder.exists() || !folder.isDirectory()) {
                // 是否要在这里新建？
                throw new IOException("The folder doesn't exist");
            } else {
                File infoFile = new File(folder, "info.json");
                if (!infoFile.exists())
                    if (!infoFile.createNewFile())
                        throw new IOException("Can't create info.json");

                JSONObject infoJSON = this.toJSONObject(folder);

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(infoFile));
                bufferedWriter.write(infoJSON.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        actionHistoryManager.writeToParcel(dest, flags);
//        dest.writeParcelable(actionHistoryManager, flags);
        dest.writeString(drawViewFolderPath);
        dest.writeString(title);
    }

    @Override
    public int hashCode() {
        return drawViewFolderPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof DrawViewInfo) {
            DrawViewInfo drawViewInfo = (DrawViewInfo) obj;
            return this.drawViewFolderPath.equals(drawViewInfo.drawViewFolderPath);
        }
        return false;
    }

    public JSONObject toJSONObject(File drawViewFolder) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(JSON_TITLE, title);
        result.put(JSON_HISTORY, actionHistoryManager.toJSONObject(drawViewFolder));

        return result;
    }

    public static DrawViewInfo generateByDrawViewFolder(File drawViewFolder) {
        if (!drawViewFolder.exists() || !drawViewFolder.isDirectory())
            return null;

        String title = null;
        String drawViewFolderPath = drawViewFolder.getPath();
        ActionHistoryManager actionHistoryManager = null;

        try {
            File infoFile = new File(drawViewFolder, "info.json");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(infoFile));

            StringBuilder infoFromFile = new StringBuilder();
            byte[] bytes = new byte[1024];
            int length;
            while ((length = bufferedInputStream.read(bytes)) != -1) {
                infoFromFile.append(Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes, 0, length)));
            }
            JSONObject info = new JSONObject(infoFromFile.toString());
            title = info.getString(JSON_TITLE);
            actionHistoryManager = ActionHistoryManager.generateByJSONObject(info.getJSONObject(JSON_HISTORY), drawViewFolder);
        } catch (IOException e) {
            e.printStackTrace();
            return new DrawViewInfo(drawViewFolderPath);
        } catch (JSONException e) {
            e.printStackTrace();
            return new DrawViewInfo(drawViewFolderPath);
        }

        return new DrawViewInfo(drawViewFolderPath, title, actionHistoryManager);

//        DrawViewInfo result = null;
//        String title = jsonObject.getString(JSON_TITLE);
//        ActionHistoryManager actionHistoryManager = ActionHistoryManager.generateByDrawViewFolder(jsonObject.getJSONObject(JSON_HISTORY), file);
//        result = new DrawViewInfo(file.getName(), title, actionHistoryManager);
    }

}
