
// const {PriorityQueue} = require('@datastructures-js/priority-queue');
/*
 PriorityQueue is internally included in the solution file on leetcode.
 When running the code on leetcode it should stay commented out. 
 It is mentioned here just for information about the external library 
 that is applied for this data structure.
 */

/**
 * @param {number[]} servers
 * @param {number[]} tasks
 * @return {number[]}
 */
var assignTasks = function (servers, tasks) {
    const minHeapAvailableServers = createMinHeapAvailableServers(servers);
    const minHeapProcessingServers = new PriorityQueue((first, second) => comparator(first, second));

    const serverIndicesPerAssignedTask = new Array(tasks.length);

    for (let taskStartTime = 0; taskStartTime < tasks.length; ++taskStartTime) {
        makeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers);
        const serverForNextTask = getServerForNextTask(minHeapProcessingServers, minHeapAvailableServers);
        serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index;

        const nextTaskCompletionTime =
                taskStartTime
                + tasks[taskStartTime]
                + Math.max(serverForNextTask.taskCompletionTime - taskStartTime, 0);

        minHeapProcessingServers.enqueue(new Server(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index));
    }

    return serverIndicesPerAssignedTask;
};

const CURRENTLY_NOT_PROCESSING_SERVER = 0;

class Server {

    /**
     * @param {number} taskCompletionTime
     * @param {number} weight
     * @param {number}index
     */
    constructor(taskCompletionTime, weight, index) {
        this.taskCompletionTime = taskCompletionTime;
        this.weight = weight;
        this.index = index;
    }
}

/**
 * @param {Server} first
 * @param {Server} second
 * @return {number}
 */
function comparator(first, second) {
    if (first.taskCompletionTime !== second.taskCompletionTime) {
        return first.taskCompletionTime - second.taskCompletionTime;
    }
    if (first.weight !== second.weight) {
        return first.weight - second.weight;
    }
    return first.index - second.index;
}

/**
 * @param {number[]} servers
 * @return {PriorityQueue<Server>}
 */
function createMinHeapAvailableServers(servers) {
    const minHeap = new PriorityQueue((first, second) => comparator(first, second));
    for (let i = 0; i < servers.length; ++i) {
        minHeap.enqueue(new Server(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i));
    }
    return minHeap;
}

/**
 * @param {PriorityQueue<Server>} minHeapProcessingServers
 * @param {PriorityQueue<Server>} minHeapAvailableServers
 * @return {Server}
 */
function  getServerForNextTask(minHeapProcessingServers, minHeapAvailableServers) {
    if (!minHeapAvailableServers.isEmpty()) {
        return minHeapAvailableServers.dequeue();
    }
    return minHeapProcessingServers.dequeue();
}

/**
 * @param {number} taskStartTime
 * @param {PriorityQueue<Server>} minHeapProcessingServers
 * @param {PriorityQueue<Server>} minHeapAvailableServers
 * @return {void}
 */
function makeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers) {
    while (!minHeapProcessingServers.isEmpty() && minHeapProcessingServers.front().taskCompletionTime <= taskStartTime) {
        const freedServer = minHeapProcessingServers.dequeue();
        minHeapAvailableServers.enqueue(new Server(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index));
    }
}
