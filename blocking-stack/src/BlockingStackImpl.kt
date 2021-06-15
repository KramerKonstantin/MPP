import java.util.concurrent.atomic.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BlockingStackImpl<E> : BlockingStack<E> {

    // ==========================
    // Segment Queue Synchronizer
    // ==========================

    private val dummy = Receiver<E>(null)
    private val enqIdx = AtomicReference<Receiver<E>>(dummy)
    private val deqIdx = AtomicReference<Receiver<E>>(dummy)

    private suspend fun suspend(): E {
        return suspendCoroutine sc@{ cont ->
            val newTail = Receiver(cont)
            while (true) {
                val oldTail = deqIdx.get()
                if (oldTail.next.compareAndSet(null, newTail)) {
                    deqIdx.compareAndSet(oldTail, newTail)
                    break
                }
            }
        }
    }

    private fun resume(element: E) {
        while (true) {
            val oldHead = enqIdx.get()
            if (oldHead != deqIdx.get() && oldHead.next.get() != null) {
                val newHead = oldHead.next.get()
                if (enqIdx.compareAndSet(oldHead, newHead)) {
                    newHead.action!!.resume(element)
                    return
                }
            }
        }
    }

    private class Receiver<E>(val action: Continuation<E>?) {
        val next = AtomicReference<Receiver<E>>(null)
    }

    // ==============
    // Blocking Stack
    // ==============

    private val head = AtomicReference<Node<E>?>(null)
    private val elements = AtomicInteger()

    override fun push(element: E) {
        val elements = this.elements.getAndIncrement()
        if (elements >= 0) {
            // push the element to the top of the stack
            while (true) {
                val oldHead = head.get()
                if (oldHead?.element != SUSPENDED) {
                    val newHead = Node(element, oldHead)
                    if (head.compareAndSet(oldHead, newHead)) {
                        break
                    }
                } else {
                    val newHead = oldHead.next.get()
                    if (head.compareAndSet(oldHead, newHead)) {
                        resume(element)
                        return
                    }
                }
            }
        } else {
            // resume the next waiting receiver
            resume(element)
        }
    }

    override suspend fun pop(): E {
        val elements = this.elements.getAndDecrement()
        if (elements > 0) {
            // remove the top element from the stack
            while (true) {
                val oldHead = head.get()
                if (oldHead != null) {
                    val newHead = oldHead.next.get()
                    if (head.compareAndSet(oldHead, newHead)) {
                        return oldHead.element as E
                    }
                } else {
                    val newHead = Node<E>(SUSPENDED)
                    if (head.compareAndSet(oldHead, newHead)) {
                        return suspend()
                    }
                }
            }
        } else {
            return suspend()
        }
    }
}

private class Node<E>(val element: Any?, n: Node<E>? = null) {
    val next = AtomicReference(n)
}

private val SUSPENDED = Any()