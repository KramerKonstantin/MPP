package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private final int ZERO_VALUE = 0;

    private static class Node {
        final int x;
        AtomicRef<Node> next;

        Node(int x) {
            this.x = x;
            next = new AtomicRef<>(null);
        }
    }

    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(ZERO_VALUE);
        this.head = new AtomicRef<>(dummy);
        this.tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x);

        while (true) {
            Node tail = this.tail.getValue();
            Node next = tail.next.getValue();

            if (tail == this.tail.getValue()) {
                if (next == null) {
                    if (this.tail.getValue().next.compareAndSet(null, newTail)) {
                        this.tail.compareAndSet(tail, newTail);
                        break;
                    }
                } else {
                    this.tail.compareAndSet(tail, next);
                }
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node head = this.head.getValue();
            Node tail = this.tail.getValue();
            Node nextHead = head.next.getValue();

            if (head == this.head.getValue()) {
                if (head == tail) {
                    if (nextHead == null) {
                        return Integer.MIN_VALUE;
                    }

                    this.tail.compareAndSet(tail, nextHead);
                } else {
                    if (this.head.compareAndSet(head, nextHead)) {
                        return nextHead.x;
                    }
                }
            }
        }
    }

    @Override
    public int peek() {
        while (true) {
            Node head = this.head.getValue();
            Node tail = this.tail.getValue();
            Node nextHead = head.next.getValue();

            if (head == this.head.getValue()) {
                if (head == tail) {
                    if (nextHead == null) {
                        return Integer.MIN_VALUE;
                    }

                    this.tail.compareAndSet(tail, nextHead);
                } else {
                    if (nextHead != null) {
                        if (this.head.getValue().next.compareAndSet(nextHead, nextHead)) {
                            return nextHead.x;
                        }
                    }
                }
            }
        }
    }
}
