import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    private class Node<T>(value: T?, val isRequest: Boolean) {
        var continuation: Continuation<Boolean>? = null
        var value = AtomicReference(value)
        var next: AtomicReference<Node<T>?> = AtomicReference(null)
    }

    private val dummy: Node<E> = Node(null, false)
    private val head: AtomicReference<Node<E>> = AtomicReference(dummy)
    private val tail: AtomicReference<Node<E>> = AtomicReference(dummy)

    override suspend fun send(element: E) {
        val node = Node(element, false)

        while (true) {
            val tail = this.tail.get()
            val head = this.head.get()
            if (head == tail || !tail.isRequest) {
                val res = suspendCoroutine<Boolean> sc@{ continuation ->
                    node.continuation = continuation
                    if (tail.next.compareAndSet(null, node)) {
                        this.tail.compareAndSet(tail, node)
                    } else {
                        continuation.resume(false)
                        return@sc
                    }
                }

                if (res) {
                    return
                }
            } else {
                val next = head.next.get()
                if (tail == this.tail.get() && head == this.head.get() && head != this.tail.get() && next != null) {
                    if (next.continuation !== null && next.isRequest && this.head.compareAndSet(head, next)) {
                        next.value.compareAndSet(null, element)
                        next.continuation!!.resume(true)
                        return
                    }
                }
            }
        }
    }

    override suspend fun receive(): E {
        val node: Node<E> = Node(null, true)

        while (true) {
            val tail = this.tail.get()
            val head = this.head.get()
            if (head == tail || tail.isRequest) {
                val res = suspendCoroutine<Boolean> sc@{ continuation ->
                    node.continuation = continuation
                    if (tail.next.compareAndSet(null, node)) {
                        this.tail.compareAndSet(tail, node)

                    } else {
                        continuation.resume(false)
                        return@sc
                    }
                }

                if (res) {
                    return node.value.get()!!
                }
            } else {
                val next = head.next.get()
                if (head != this.tail.get() && tail == this.tail.get() && head == this.head.get() && next != null) {
                    val element = next.value.get() ?: continue
                    if (next.continuation !== null && !next.isRequest && this.head.compareAndSet(head, next)) {
                        next.value.compareAndSet(element, null)
                        next.continuation!!.resume(true)
                        return element
                    }
                }
            }
        }
    }
}