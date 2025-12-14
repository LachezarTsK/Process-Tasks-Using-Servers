
#include <span>
#include <queue>
#include <vector>
#include <algorithm>
using namespace std;

class Solution {

    struct Server {
        long long taskCompletionTime{};
        int weight{};
        int index{};

        Server() = default;
        Server(long long taskCompletionTime, int weight, int index) :
            taskCompletionTime{ taskCompletionTime }, weight{ weight }, index{ index } {};
    };

    struct Comparator {
        bool operator()(const Server& first, const Server& second) const {
            if (first.taskCompletionTime != second.taskCompletionTime) {
                return first.taskCompletionTime > second.taskCompletionTime;
            }
            if (first.weight != second.weight) {
                return first.weight > second.weight;
            }
            return first.index > second.index;
        };
    };

    using MinHeap = priority_queue<Server, vector<Server>, Comparator>;
    inline static const int CURRENTLY_NOT_PROCESSING_SERVER = 0;

public:
    vector<int> assignTasks(const vector<int>& servers, const vector<int>& tasks) const {
        MinHeap minHeapAvailableServers = createMinHeapAvailableServers(servers);
        MinHeap minHeapProcessingServers;

        vector<int> serverIndicesPerAssignedTask(tasks.size());

        for (int taskStartTime = 0; taskStartTime < tasks.size(); ++taskStartTime) {
            makeAvailableServersWithCompletedTasks(taskStartTime, minHeapProcessingServers, minHeapAvailableServers);
            Server serverForNextTask = getServerForNextTask(minHeapProcessingServers, minHeapAvailableServers);
            serverIndicesPerAssignedTask[taskStartTime] = serverForNextTask.index;

            long long nextTaskCompletionTime =
                             taskStartTime
                             + tasks[taskStartTime]
                             + max(serverForNextTask.taskCompletionTime - taskStartTime, 0LL);

            minHeapProcessingServers.emplace(nextTaskCompletionTime, serverForNextTask.weight, serverForNextTask.index);
        }

        return serverIndicesPerAssignedTask;
    }

private:
    MinHeap createMinHeapAvailableServers(span<const int> servers) const {
        MinHeap minHeap;
        for (int i = 0; i < servers.size(); ++i) {
            minHeap.emplace(CURRENTLY_NOT_PROCESSING_SERVER, servers[i], i);
        }
        return minHeap;
    }

    Server getServerForNextTask(MinHeap& minHeapProcessingServers, MinHeap& minHeapAvailableServers) const {
        Server serverForNextTask;
        if (!minHeapAvailableServers.empty()) {
            serverForNextTask = minHeapAvailableServers.top();
            minHeapAvailableServers.pop();
        }
        else {
            serverForNextTask = minHeapProcessingServers.top();
            minHeapProcessingServers.pop();
        }
        return serverForNextTask;
    }

    void makeAvailableServersWithCompletedTasks(int taskStartTime, MinHeap& minHeapProcessingServers, MinHeap& minHeapAvailableServers) const {
        while (!minHeapProcessingServers.empty() && minHeapProcessingServers.top().taskCompletionTime <= taskStartTime) {
            Server freedServer = minHeapProcessingServers.top();
            minHeapProcessingServers.pop();
            minHeapAvailableServers.emplace(CURRENTLY_NOT_PROCESSING_SERVER, freedServer.weight, freedServer.index);
        }
    }
};
