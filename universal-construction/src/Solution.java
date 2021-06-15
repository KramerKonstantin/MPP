public class Solution implements AtomicCounter {
    private final Node root = new Node(0);
    private final ThreadLocal<Node> last = new ThreadLocal<>();

    public int getAndAdd(int x) {
        while (true) {
            if (last.get() == null) {
                last.set(root);
            }

            int answer = last.get().value;
            Node summaryValue = new Node(answer + x);
            last.set(last.get().next.decide(summaryValue));

            if (summaryValue.equals(last.get())) {
                return answer;
            }
        }
    }

    private static class Node {
        final int value;
        final Consensus<Node> next = new Consensus<>();

        Node(int value) {
            this.value = value;
        }
    }
}