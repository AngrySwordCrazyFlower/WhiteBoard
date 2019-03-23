package com.example.crazyflower.whiteboard.Action;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

/**
 * 用于管理动作的类，采用命令模式（应该是的吧）
 * 对于动作的记录，采用双向链表，存有一个变量指向当前节点，撤销/重做的时候，访问前一个/后一个节点
 * 当撤销到某一节点，同时有新动作产生的时候，会链接到新的节点上，从而放弃原来链中后面的部分。
 * 举例：A->B->C->D(current)，当从D撤销到B的时候A->B(current)->C->D，新动作E产生了，则链表变为A->B->E(current)
 * 不过有个问题是C->D被舍弃了，不过是双向链表，无法被GC回收，考虑是否修改为WeakReference的双向引用
 */
public class ActionHistoryManager {

    Node first;
    Node current;

    public ActionHistoryManager() {
        first = null;
        current = null;
    }

    /**
     * 当有新动作产生的时候，调用这个函数。
     * @param action 新的动作
     */
    public void addAction(Action action) {
        Node temp = new Node(action);
        if (null != current) {
            temp.pre = current;
            current.next = temp;
        }
        if (null == first)
            first = temp;
        current = temp;
    }

    /**
     * 恢复。根据历史记录，以及当前所在步骤节点，重做下一步（如果有的话）
     * @param elements 用于进行操作的元素列表，比如新增一个元素，则该元素应该新增在这个列表中。
     *                 考虑是否换为DrawView
     */
    public void redo(List<BasicElement> elements) {
        if (canRedo()) {
            if (null == current)
                current = first;
            else
                current = current.next;
            current.action.redo(elements);
        }
    }

    /**
     * 重做。根据历史记录，以及当前所在步骤节点，撤销当前步骤（如果有的话）
     * @param elements 用于进行操作的元素列表，比如新增一个元素，则该元素应该新增在这个列表中。
     *                 考虑是否换为DrawView
     */
    public void undo(List<BasicElement> elements) {
        if (canUndo()) {
            current.action.undo(elements);
            current = current.pre;
        }
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

}
