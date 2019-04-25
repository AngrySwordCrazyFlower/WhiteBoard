package com.example.crazyflower.whiteboard.Action;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 用于管理动作的类，采用命令模式（应该是的吧）
 * 对于动作的记录，采用双向链表，存有一个变量指向当前节点，撤销/重做的时候，访问前一个/后一个节点
 * 当撤销到某一节点，同时有新动作产生的时候，会链接到新的节点上，从而放弃原来链中后面的部分。
 * 举例：A->B->C->D(current)，当从D撤销到B的时候A->B(current)->C->D，新动作E产生了，则链表变为A->B->E(current)
 * 不过有个问题是C->D被舍弃了，不过是双向链表，无法被GC回收，考虑是否修改为WeakReference的双向引用
 */
public class ActionHistoryManager implements Parcelable {

    private static final String TAG = "ActionHistoryManager";
    // 所有的元素，包括被删除的
//    private LinkedHashMap<UUID, BasicElement> allBasicElements;
    // 还在画布上的元素，没被删除的元素
    private ArrayList<BasicElement> onCanvasBasicElements;

    Node first;
    Node current;

    private boolean changed;

    public ActionHistoryManager() {
        first = null;
        current = null;
        onCanvasBasicElements = new ArrayList<>();
        changed = true;
//        allBasicElements = new LinkedHashMap<>();
    }

    public ActionHistoryManager(Parcel source) {
        first = null;
        current = null;
        onCanvasBasicElements = new ArrayList<>();
        changed = false;
        int count = source.readInt();
//        Log.d(TAG, "ActionHistoryManager: " + count);
        Action action;
        HashMap<UUID, BasicElement> hashMap = new HashMap<UUID, BasicElement>();
        for (int i = 0; i < count; i++) {
//            Log.d(TAG, "ActionHistoryManager: " + i);
//            action = in.readParcelable(Action.class.getClassLoader());
            action = Action.createFromParcel(source, hashMap);
            if (null != action)
                addAction(action);
        }
    }

    public static final Parcelable.Creator<ActionHistoryManager> CREATOR = new Parcelable.Creator<ActionHistoryManager>() {
        @Override
        public ActionHistoryManager createFromParcel(Parcel in) {
            return new ActionHistoryManager(in);
        }

        @Override
        public ActionHistoryManager[] newArray(int size) {
            return new ActionHistoryManager[size];
        }
    };

    /**
     * 当有新动作产生的时候，调用这个函数。
     * 新动作添加的时候还未执行，执行。
     * @param action 新的动作
     */
    public void addAction(Action action) {
        Node temp = new Node(action);
        if (current == null)
            first = temp;
        else {
            current.next = temp;
            temp.pre = current;
        }
        redo();
        Log.d(TAG, "addAction: " + onCanvasBasicElements.size());
    }

    /**
     * 恢复。根据历史记录，以及当前所在步骤节点，重做下一步（如果有的话）
     */
    public void redo() {
        if (canRedo()) {
            if (null == current)
                current = first;
            else
                current = current.next;
            current.action.redo(onCanvasBasicElements);
        }
        changed = true;
    }

    /**
     * 撤销。根据历史记录，以及当前所在步骤节点，撤销当前步骤（如果有的话）
     */
    public void undo() {
        if (canUndo()) {
            current.action.undo(onCanvasBasicElements);
            current = current.pre;
        }
        changed = true;
    }

    /**
     * 是否可以进行撤销操作，即当前步骤是否已经在第一步之前，已经无法撤销
     * @return 是否可以进行撤销操作
     */
    public boolean canRedo() {
        if (null == current)
            return null != first;
        return null != current.next;
    }

    /**
     * 是否可以进行重做操作，即当前步骤是否已经是最后一步、已经无法重做
     * @return 是否可以进行重做操作
     */
    public boolean canUndo() {
        return null != current;
    }

    public boolean isChanged() {
        return changed;
    }

//    public void onNewElement(BasicElement basicElement) {
//        addAction(new NewAction(basicElement));
////        allBasicElements.put(basicElement.getUuid(), basicElement);
//    }
//
//    public void onDeleteElement(List<BasicElement> elements) {
//        addAction(new DeleteAction(elements));
//    }
//
//    public void onRotateElement(List<BasicElement> elements, float angle, float x, float y) {
//        addAction(new RotateAction(elements, angle, x, y));
//    }
//
//    public void onScaleElement(List<BasicElement> elements, float scale, float x, float y) {
//        addAction(new ScaleAction(elements, scale, x, y));
//    }
//
//    public void onTranslateElement(List<BasicElement> elements, float dx, float dy) {
//        addAction(new TranslateAction(elements, dx, dy));
//    }

    public List<BasicElement> getOnCanvasBasicElements() {
        return onCanvasBasicElements;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Node temp = first;
        int count = 0;
        while (temp != null) {
            count++;
            temp = temp.next;
        }
        dest.writeInt(count);
        temp = first;
        while (temp != null) {
            temp.action.writeToParcel(dest, flags);
            temp = temp.next;
        }
    }

    static private class Node {

        private Node pre;
        private Node next;

        Action action;

        private Node(Action action) {
            this.action = action;
            pre = null;
            next = null;
        }
    }

    /**
     * 将这个历史转变成为JSON对象
     * @return 用JSON描述的操作记录
     * @throws JSONException
     */
    public JSONArray getActionJSONArray() throws JSONException {
        JSONArray actions = new JSONArray();
        if (first != null) {
            Node pNode = first;
            while (pNode != null) {
                actions.put(Action.toJSONObject(pNode.action));
                pNode = pNode.next;
            }
        }
        return actions;
    }

//    public ActionHistoryManager(JSONObject jsonObject) {
//
//    }

    public static ActionHistoryManager generateByActionJSONArray(JSONArray actions) throws JSONException {
        ActionHistoryManager actionHistoryManager = new ActionHistoryManager();
        HashMap<UUID, BasicElement> map = new HashMap<UUID, BasicElement>();
        for (int i = 0, length = actions.length(); i < length; i++) {
            actionHistoryManager.addAction(Action.generateByJSONObject(actions.getJSONObject(i), map));
        }
        return actionHistoryManager;
    }

    public void save() {
        changed = false;
    }

}
