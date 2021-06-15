package stack;

import kotlinx.atomicfu.AtomicArray;
import kotlinx.atomicfu.AtomicRef;

import java.util.Random;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);

    private final int ARRAY_SIZE = 10;
    private final int SPIN_WAIT = 4;

    private AtomicArray<Node> eliminationArray = new AtomicArray<>(ARRAY_SIZE);
    private Random random = new Random();

    @Override
    public void push(int x) {
        int randomIndex  = random.nextInt(ARRAY_SIZE);
        Node newNode = new Node(x, null);

        for (int i = 0; i < SPIN_WAIT; i++, randomIndex++) {
            while (randomIndex == ARRAY_SIZE) {
                randomIndex = random.nextInt(ARRAY_SIZE);
            }

            AtomicRef<Node> node = eliminationArray.get(randomIndex);

            if (node.compareAndSet(null, newNode)) {
                for (int j = 0; j < SPIN_WAIT; j++) {
                    if (node.getValue() != newNode) {
                        return;
                    }
                }

                if (node.compareAndSet(newNode, null)) {
                    break;
                }

                return;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            Node newHead = new Node(x, curHead);

            if (head.compareAndSet(curHead, newHead)) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        int randomIndex  = random.nextInt(ARRAY_SIZE);

        for (int i = 0; i < SPIN_WAIT; i++, randomIndex++) {
            while (randomIndex == ARRAY_SIZE) {
                randomIndex = random.nextInt(ARRAY_SIZE);
            }

            AtomicRef<Node> node = eliminationArray.get(randomIndex);
            Node value = node.getValue();

            if (value != null && node.compareAndSet(value, null)) {
                return value.x;
            }
        }

        while (true) {
            Node curHead = head.getValue();

            if (curHead == null) {
                return Integer.MIN_VALUE;
            }

            if (head.compareAndSet(curHead, curHead.next.getValue())) {
                return curHead.x;
            }
        }
    }
}