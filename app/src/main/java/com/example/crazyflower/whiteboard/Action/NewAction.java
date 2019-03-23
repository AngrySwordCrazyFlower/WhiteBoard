package com.example.crazyflower.whiteboard.Action;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

public class NewAction extends Action {

    private BasicElement element;

    public NewAction(BasicElement element) {
        super(Action.ACTION_NEW);
        this.element = element;
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        basicElements.add(element);
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        basicElements.remove(element);
    }
}
