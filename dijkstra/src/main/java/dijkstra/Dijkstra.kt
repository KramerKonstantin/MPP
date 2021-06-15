package dijkstra

import java.util.concurrent.Phaser
import kotlinx.atomicfu.atomic
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.Comparator
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    start.distance = 0
    val q = MultiQueue(workers, NODE_DISTANCE_COMPARATOR)
    q.add(start)
    val onFinish = Phaser(workers + 1)
    var activeNodes = 1
    repeat(workers) {
        thread {
            while (true) {
                val cur: Node? = q.poll()
                if (cur == null) {
                    activeNodes--
                    if (q.size() <= 0 && activeNodes <= 0) {
                        break
                    }

                    continue
                }

                for (e in cur.outgoingEdges) {
                    while(true) {
                        val oldDistance = e.to.distance
                        val distance = cur.distance + e.weight
                        if (oldDistance > distance) {
                            if (e.to.casDistance(oldDistance, distance)) {
                                q.add(e.to)
                                activeNodes++
                                break
                            }
                            continue
                        }
                        break
                    }
                }
                activeNodes--
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

class MultiQueue(private val workers: Int, comparator: Comparator<Node>) {

    private val size = atomic(0)

    private val locks = Array(workers) {
        ReentrantLock()
    }

    private val priorityQueues = Array(workers) {
        PriorityQueue(comparator)
    }

    fun size() :Int {
        return size.value
    }

    fun poll(): Node? {
        val index1 = Random().nextInt(workers)
        val index2 = Random().nextInt(workers)
        val queue1 = priorityQueues[index1]
        val queue2 = priorityQueues[index2]
        val lock1 = locks[index1]
        val lock2 = locks[index2]

        var node: Node? = null
        if (lock1.tryLock()) {
            if (lock2.tryLock()) {
                val head1 = queue1.peek()
                val head2 = queue2.peek()

                if (head1 == null) {
                    node = queue2.poll()
                } else if (head2 == null) {
                    node = queue1.poll()
                } else if (head1.distance > head2.distance) {
                    node = queue2.poll()
                } else {
                    node = queue2.poll()
                }

                lock2.unlock()
            }
            lock1.unlock()
        }

        if (node != null) {
            size.decrementAndGet()
        }

        return node
    }

    fun add(node: Node) {
        while (true) {
            val index = Random().nextInt(workers)
            if (locks[index].tryLock()) {
                priorityQueues[index].add(node)
                size.incrementAndGet()
                locks[index].unlock()
                break
            }
        }
    }
}