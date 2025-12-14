
using System;
using System.Collections.Generic;

public class Solution
{
    private record Server(long taskCompletionTime, int weight, int index) { }

    private static readonly int CURRENTLY_NOT_PROCESSING_SERVER = 0;

    public int[] AssignTasks(int[] servers, int[] tasks)
    {
        PriorityQueue<Server, Server> minHeapAvailableServers = CreateMinHeapAvailableServers(servers);
        PriorityQueue<Server, Server> minHeapProcessingServers = new(Comparer<Server>.Create((first, second) => Comparator(first, second)));

        int[] serverIndicesPerAssignedTask = new int[tasks.Length];

        for (int taskStartTime = 0; taskStartTime < tasks.Length; ++taskStartTime)
        {
            MakeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers);
            Server serverForNextTask = GetServerForNextTask(minHeapProcessingServers, minHeapAvailableServers);
            serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index;

            long nextTaskCompletionTime
                    = taskStartTime
                    + tasks[taskStartTime]
                    + Math.Max(serverForNextTask.taskCompletionTime - taskStartTime, 0);

            Server updateServerForNextTask = new(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index);

            minHeapProcessingServers.Enqueue(updateServerForNextTask, updateServerForNextTask);
        }

        return serverIndicesPerAssignedTask;
    }

    private int Comparator(Server first, Server second)
    {
        if (first.taskCompletionTime != second.taskCompletionTime)
        {
            return first.taskCompletionTime.CompareTo(second.taskCompletionTime);
        }
        if (first.weight != second.weight)
        {
            return first.weight - second.weight;
        }
        return first.index - second.index;
    }

    private PriorityQueue<Server, Server> CreateMinHeapAvailableServers(int[] servers)
    {
        PriorityQueue<Server, Server> minHeap = new(Comparer<Server>.Create((first, second) => Comparator(first, second)));

        for (int i = 0; i < servers.Length; ++i)
        {
            Server server = new Server(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i);
            minHeap.Enqueue(server, server);
        }
        return minHeap;
    }

    private Server GetServerForNextTask(PriorityQueue<Server, Server> minHeapProcessingServers, PriorityQueue<Server, Server> minHeapAvailableServers)
    {
        if (minHeapAvailableServers.Count > 0)
        {
            return minHeapAvailableServers.Dequeue();
        }
        return minHeapProcessingServers.Dequeue();
    }

    private void MakeAvailableServersWithCompletedTasks(int taskStartTime, PriorityQueue<Server, Server> minHeapProcessingServers, PriorityQueue<Server, Server> minHeapAvailableServers)
    {
        while (minHeapProcessingServers.Count > 0 && minHeapProcessingServers.Peek().taskCompletionTime <= taskStartTime)
        {
            Server freedServer = minHeapProcessingServers.Dequeue();
            Server updatedServer = new(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index);
            minHeapAvailableServers.Enqueue(updatedServer, updatedServer);
        }
    }
}
