package com.example.crazyflower.whiteboard;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Consumer;

public class CompareBaseLinkedList<E extends Comparable<E>> implements Iterable<E> {

    private int size;

    private Node first;

    private boolean ascend;

    public CompareBaseLinkedList() {
        this(true);
    }

    public CompareBaseLinkedList(boolean ascend) {
        size = 0;
        first = null;
        this.ascend = ascend;
    }

    public int size() {
        return size;
    }

    public int add(@NonNull E e) {
        Node current = first;
        Node last = null;
        int index = 0;

        while (current != null) {
            if (e.compareTo(first.e) > 0 ^ ascend) {
                last = current;
                current = current.next;
                index++;
            } else
                break;
        }


        Node newNode = new Node();
        newNode.e = e;
        if (last == null) { // 说明加在最前面
            newNode.next = first;
            first = newNode;
        } else if (current == null) { // 说明加在最后面
            last.next = newNode;
        } else { // 加在中间
            last.next = newNode;
            newNode.next = current.next;
        }

        size++;
        return index;
    }

    public int indexOf(E e) {
        Node current = first;
        int index = 0;
        while (current != null) {
            if (current.e == e)
                break;
            current = current.next;
            index++;
        }
        return current == null ? -1 : index;
    }

    public E get(int index) {
        if (index < 0 || index >= size)
            return null;
        Node current = first;
        while (index > 0) {
            current = current.next;
            index--;
        }
        return current.e;
    }

    public int remove(E e) {
        Node current = first;
        Node last = null;
        int index = 0;

        while (current != null) {
            if (current.e.equals(e))
                break;
            else {
                index++;
                last = current;
                current = current.next;
            }
        }

        if (current != null) {
            if (last == null) {
                first = current.next;
            }
            else {
                last.next = current.next;
                current.next = null;
                current.e = null;
            }
            size--;
            return index;
        } else
            return -1;
    }

    @NonNull
    @Override
    public java.util.Iterator<E> iterator() {
        return new Iterator(first);
    }

    private class Iterator implements java.util.Iterator<E> {

        private Node current;

        private Iterator(Node node) {
            current = node;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            Node temp = current;
            current = current.next;
            return temp.e;
        }

        @Override
        public void remove() {

        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {

        }
    }

    private class Node {
        E e;
        Node next;
    }

}
