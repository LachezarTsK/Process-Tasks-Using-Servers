
package main
import "container/heap"

const CURRENTLY_NOT_PROCESSING_SERVER = 0

func assignTasks(servers []int, tasks []int) []int {
    var minHeapAvailableServers PriorityQueue = createMinHeapAvailableServers(servers)
    minHeapProcessingServers := PriorityQueue{}

    serverIndicesPerAssignedTask := make([]int, len(tasks))

    for taskStartTime := range tasks {
        makeAvailableServersWithCompletedTasks(taskStartTime, &minHeapProcessingServers, &minHeapAvailableServers)
        serverForNextTask := getServerForNextTask(&minHeapProcessingServers, &minHeapAvailableServers)
        serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index

        nextTaskCompletionTime :=
                int64(taskStartTime) +
                int64(tasks[taskStartTime]) +
                max(serverForNextTask.taskCompletionTime - int64(taskStartTime), 0)

        heap.Push(&minHeapProcessingServers, NewServer(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index))
    }

    return serverIndicesPerAssignedTask
}

func createMinHeapAvailableServers(servers []int) PriorityQueue {
    minHeap := PriorityQueue{}
    for i := range servers {
        heap.Push(&minHeap, NewServer(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i))
    }
    return minHeap
}

func getServerForNextTask(minHeapProcessingServers *PriorityQueue, minHeapAvailableServers *PriorityQueue) Server {
    if !minHeapAvailableServers.isEmpty() {
        return heap.Pop(minHeapAvailableServers).(Server)
    }
    return heap.Pop(minHeapProcessingServers).(Server)
}

func makeAvailableServersWithCompletedTasks(taskStartTime int, minHeapProcessingServers *PriorityQueue, minHeapAvailableServers *PriorityQueue) {
    for !minHeapProcessingServers.isEmpty() && minHeapProcessingServers.Peek().(Server).taskCompletionTime <= int64(taskStartTime) {
        freedServer := heap.Pop(minHeapProcessingServers).(Server)
        updatedFreedServer := NewServer(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index)
        heap.Push(minHeapAvailableServers, updatedFreedServer)
    }
}

type Server struct {
    taskCompletionTime int64
    weight             int
    index              int
}

func NewServer(taskCompletionTime int64, weight int, index int) Server {
    server := Server{
        taskCompletionTime: taskCompletionTime,
        weight:             weight,
        index:              index,
    }
    return server
}

func comparator(first Server, second Server) bool {
    if first.taskCompletionTime != second.taskCompletionTime {
        return first.taskCompletionTime < second.taskCompletionTime
    }
    if first.weight != second.weight {
        return first.weight < second.weight
    }
    return first.index < second.index
}

type PriorityQueue []Server

func (pq PriorityQueue) Len() int {
    return len(pq)
}

func (pq PriorityQueue) Less(first int, second int) bool {
    return comparator(pq[first], pq[second])
}

func (pq PriorityQueue) Swap(first int, second int) {
    pq[first], pq[second] = pq[second], pq[first]
}

func (pq *PriorityQueue) Push(object any) {
    server := object.(Server)
    *pq = append(*pq, server)
}

func (pq *PriorityQueue) Pop() any {
    item := (*pq)[pq.Len() - 1]
    *pq = (*pq)[0 : pq.Len() - 1]
    return item
}

func (pq PriorityQueue) Peek() any {
    return pq[0]
}

func (pq PriorityQueue) isEmpty() bool {
    return pq.Len() == 0
}
