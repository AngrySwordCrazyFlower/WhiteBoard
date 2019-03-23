package com.example.crazyflower.whiteboard.Action;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

public class DeleteAction extends Action {

    private List<BasicElement> elements;

    public DeleteAction(List<BasicElement> elements) {
        super(Action.ACTION_DELETE);
        this.elements = elements;
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        basicElements.removeAll(elements);
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        basicElements.addAll(elements);
    }
}
