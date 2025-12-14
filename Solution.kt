
import kotlin.math.max

class Solution {

    private data class Server(val taskCompletionTime: Long, val weight: Int, val index: Int) {}

    private companion object {
        const val CURRENTLY_NOT_PROCESSING_SERVER: Long = 0
    }

    fun assignTasks(servers: IntArray, tasks: IntArray): IntArray {
        val minHeapAvailableServers: PriorityQueue<Server> = createMinHeapAvailableServers(servers)
        val minHeapProcessingServers = PriorityQueue<Server>() { first, second -> comparator(first, second) }

        val serverIndicesPerAssignedTask = IntArray(tasks.size)

        for (taskStartTime in 0..<tasks.size) {
            makeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers)
            val serverForNextTask = getServerForNextTask(minHeapProcessingServers, minHeapAvailableServers)
            serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index

            val nextTaskCompletionTime =
                        taskStartTime +
                        tasks[taskStartTime] +
                        max(serverForNextTask.taskCompletionTime - taskStartTime, 0)

            minHeapProcessingServers.add(Server(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index))
        }

        return serverIndicesPerAssignedTask
    }

    private fun comparator(first: Server, second: Server): Int {
        if (first.taskCompletionTime != second.taskCompletionTime) {
            return first.taskCompletionTime.compareTo(second.taskCompletionTime)
        }
        if (first.weight != second.weight) {
            return first.weight - second.weight
        }
        return first.index - second.index
    }

    private fun createMinHeapAvailableServers(servers: IntArray): PriorityQueue<Server> {
        val minHeap = PriorityQueue<Server>() { first, second -> comparator(first, second) }
        for (i in servers.indices) {
            minHeap.add(Server(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i))
        }
        return minHeap
    }

    private fun getServerForNextTask(minHeapProcessingServers: PriorityQueue<Server>, minHeapAvailableServers: PriorityQueue<Server>): Server {
        if (!minHeapAvailableServers.isEmpty()) {
            return minHeapAvailableServers.poll()
        }
        return minHeapProcessingServers.poll()
    }

    private fun makeAvailableServersWithCompletedTasks(taskStartTime: Int, minHeapProcessingServers: PriorityQueue<Server>, minHeapAvailableServers: PriorityQueue<Server>) {
        while (!minHeapProcessingServers.isEmpty() && minHeapProcessingServers.peek().taskCompletionTime <= taskStartTime) {
            val freedServer = minHeapProcessingServers.poll()
            minHeapAvailableServers.add(Server(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index))
        }
    }
}
