package com.example.crazyflower.whiteboard.Action;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;
import java.util.UUID;

public abstract class Action {

    /**
     * 动作Id,暂时没有用，未来图画的本地化可能会用到。
     */
    private UUID id;


    /**
     * 动作类型，应该是ACTION_NEW、ACTION_TRANSLATE、ACTION_ROTATE、ACTION_DELETE中的一种，未来可能有新的种类。
     */
    protected int type;

    /**
     * 新增元素动作类型值
     */
    protected final static int ACTION_NEW = 0;

    /**
     * 移动元素动作类型值
     */
    protected final static int ACTION_TRANSLATE = 1;

    /**
     * 旋转元素动作类型值
     */
    protected final static int ACTION_ROTATE = 2;

    /**
     * 删除元素动作类型值
     */
    protected final static int ACTION_DELETE = 3;

    protected Action(int type) {
        this.id = UUID.randomUUID();
        this.type = type;
    }

    /**
     * 恢复。根据该动作的记录，重做该动作。
     * @param basicElements 用于进行操作的元素列表，比如新增一个元素，则该元素应该新增在这个列表中。
     *                      考虑是否换为DrawView
     */
    public abstract void redo(List<BasicElement> basicElements);

    /**
     * 撤销。根据该动作的记录，撤销该动作。
     * @param basicElements 用于进行操作的元素列表，比如新增一个元素，则该元素应该从这个列表中删除。
     *                      考虑是否换为DrawView
     */
    public abstract void undo(List<BasicElement> basicElements);

}
