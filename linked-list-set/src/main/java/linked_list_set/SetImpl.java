package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {
    private static class Node {
        int x;
        AtomicRef<Object> next;

        Node(int x, Object next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private static class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    private static class Removed {
        Node next;

        Removed(Node next) {
            this.next = next;
        }
    }

    private Node getNext(Node node) {
        Object nextValue = node.next.getValue();
        if (nextValue instanceof Node) {
            return (Node) nextValue;
        } else {
            return ((Removed) nextValue).next;
        }
    }

    /** Returns the {@link Window}, where cur.x < x <= next.x */
    private Window findWindow(int x) {
        loop: while (true) {
            Window w = new Window();
            w.cur = head;
            w.next = getNext(w.cur);

            while (w.next.x < x) {
                Object node = w.next.next.getValue();
                if (node instanceof Removed) {
                    Node next = ((Removed) node).next;
                    if (!w.cur.next.compareAndSet(w.next, next)) {
                        continue loop;
                    }

                    w.next = next;
                } else {
                    w.cur = w.next;
                    w.next = getNext(w.cur);
                }
            }

            Object node = w.next.next.getValue();
            if (!(node instanceof Removed)) {
                return w;
            }
            w.cur.next.compareAndSet(w.next, ((Removed) node).next);
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x == x) {
                return false;
            }

            if (w.cur.next.compareAndSet(w.next, new Node(x, w.next))) {
                return true;
            }
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x != x) {
                return false;
            }

            Node next = getNext(w.next);
            if (w.next.next.compareAndSet(next, new Removed(next))) {
                w.cur.next.compareAndSet(w.next, next);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        return findWindow(x).next.x == x;
    }
}