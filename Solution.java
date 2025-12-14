
import java.util.PriorityQueue;

public class Solution {

    private record Server(long taskCompletionTime, int weight, int index) {}

    private static final int CURRENTLY_NOT_PROCESSING_SERVER = 0;

    public int[] assignTasks(int[] servers, int[] tasks) {
        PriorityQueue<Server> minHeapAvailableServers = createMinHeapAvailableServers(servers);
        PriorityQueue<Server> minHeapProcessingServers = new PriorityQueue<>((first, second) -> comparator(first, second));

        int[] serverIndicesPerAssignedTask = new int[tasks.length];

        for (int taskStartTime = 0; taskStartTime < tasks.length; ++taskStartTime) {
            makeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers);
            Server serverForNextTask = getServerForNextTask(minHeapProcessingServers, minHeapAvailableServers);
            serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index;

            long nextTaskCompletionTime
                    = taskStartTime
                    + tasks[taskStartTime]
                    + Math.max(serverForNextTask.taskCompletionTime - taskStartTime, 0);

            minHeapProcessingServers.add(new Server(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index));
        }

        return serverIndicesPerAssignedTask;
    }

    private int comparator(Server first, Server second) {
        if (first.taskCompletionTime != second.taskCompletionTime) {
            return Long.compare(first.taskCompletionTime, second.taskCompletionTime);
        }
        if (first.weight != second.weight) {
            return first.weight - second.weight;
        }
        return first.index - second.index;
    }

    private PriorityQueue<Server> createMinHeapAvailableServers(int[] servers) {
        PriorityQueue<Server> minHeap = new PriorityQueue<>((first, second) -> comparator(first, second));
        for (int i = 0; i < servers.length; ++i) {
            minHeap.add(new Server(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i));
        }
        return minHeap;
    }

    private Server getServerForNextTask(PriorityQueue<Server> minHeapProcessingServers, PriorityQueue<Server> minHeapAvailableServers) {
        if (!minHeapAvailableServers.isEmpty()) {
            return minHeapAvailableServers.poll();
        }
        return minHeapProcessingServers.poll();
    }

    private void makeAvailableServersWithCompletedTasks(int taskStartTime, PriorityQueue<Server> minHeapProcessingServers, PriorityQueue<Server> minHeapAvailableServers) {
        while (!minHeapProcessingServers.isEmpty() && minHeapProcessingServers.peek().taskCompletionTime <= taskStartTime) {
            Server freedServer = minHeapProcessingServers.poll();
            minHeapAvailableServers.add(new Server(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index));
        }
    }
}
