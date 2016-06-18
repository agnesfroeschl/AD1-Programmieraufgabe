package ad1.ss16.pa;


import java.util.*;

@SuppressWarnings(value = "unchecked")
public class Network {

    private int numberOfNodes;
    private TreeSet<Integer>[] nodeConnections;
    private int numberOfConnections;
    private int numberOfComponents;
    private boolean[] discovered;
    private boolean hasCycle;
    private int disct[];
    private int low[];
    private int dsfCounter = -1;
    private int parent[];
    private List<Integer> critical = new LinkedList<>();
    private int[] edgeTo;
    private int[] distTo;


    public Network(int n) {
        this.numberOfNodes = n;
        this.nodeConnections = new TreeSet[n];
        this.numberOfConnections = 0;
        this.numberOfComponents = n;
        this.discovered = new boolean[n];
        this.hasCycle = false;
        disct = new int[n];
        low = new int[n];
        parent = new int[n];
        initNodes();
    }

    private void initNodes() {
        for (int i = 0; i < numberOfNodes; i++) {
            nodeConnections[i] = new TreeSet<>();
        }

    }

    public int numberOfNodes() {
        return numberOfNodes;
    }

    public int numberOfConnections() {
        return numberOfConnections;
    }

    public void addConnection(int v, int w) {
        if (v != w) {
            int num = nodeConnections[v].size();
            nodeConnections[v].add(w);
            nodeConnections[w].add(v);
            int num2 = nodeConnections[v].size();
            if (num2 > num) {
                numberOfConnections++;
            }
        }

    }

    public void addAllConnections(int v) {
        for (int i = 0; i < numberOfNodes; i++) {
            if (v != i) {
                addConnection(v, i);
            }
        }
    }

    public void deleteConnection(int v, int w) {
        if (v != w) {
            int num = nodeConnections[v].size();
            nodeConnections[v].remove(w);
            nodeConnections[w].remove(v);
            int num2 = nodeConnections[v].size();
            if (num2 < num) {
                numberOfConnections--;
            }

        }
    }

    public void deleteAllConnections(int v) {
        while (!nodeConnections[v].isEmpty()) {
            deleteConnection(v, nodeConnections[v].iterator().next());
        }
        nodeConnections[v] = new TreeSet<>();
    }

    public int numberOfComponents() {
        if (numberOfConnections == 0) {
            return numberOfNodes;
        }
        numberOfComponents = calculateConnectedComponents();
        return numberOfComponents;
    }

    private int calculateConnectedComponents() {
        resetDiscovered();
        int count = 0;
        for (int i = 0; i < numberOfNodes; i++) {
            if (!discovered[i]) {
                count += 1;
                depthFirstSearch(i);
            }
        }
        return count;
    }

    private void depthFirstSearch(int v) {
        discovered[v] = true;
        if (nodeConnections[v].size() != 0) { //checks if the node has any connections
            TreeSet<Integer> listV = nodeConnections[v];
            Iterator<Integer> iter = listV.iterator();
            while (iter.hasNext()) {
                int node = iter.next();
                if (!discovered[node]) {
                    depthFirstSearch(node);
                }
            }
        }

    }

    public boolean hasCycle() {
        hasCycle = false;
        checkCycle();
        return hasCycle;
    }

    private void checkCycle() {
        resetDiscovered();
        parent = new int[numberOfNodes];

        for (int i = 0; i < numberOfNodes; ++i) {
            parent[i] = -1;
        }
        for (int i = 0; i < numberOfNodes; i++) {
            if (!discovered[i]) {
                dfsCycle(i);
            }
        }
    }

    private void dfsCycle(int i) {
        if (discovered[i]) {
            hasCycle = true;
            return;
        }
        discovered[i] = true;
        TreeSet<Integer> list = nodeConnections[i];
        if (list.size() != 0) {
            Iterator<Integer> iter = list.iterator();
            while (iter.hasNext()) {
                int neighbour = iter.next();
                if (parent[i] != neighbour) {
                    parent[neighbour] = i;
                    dfsCycle(neighbour);
                }
                if (hasCycle) {
                    break;
                }
            }
        }

    }

    private int calculateShortestPath(int start, int end) {
        distTo = new int[numberOfNodes];
        edgeTo = new int[numberOfNodes];

        int count = 0;

        if (start == end) {
            count = 0;
            return count;
        }
        if (nodeConnections[start].size() == 0 || nodeConnections[start].size() == 0) { // if start or end doesn't have any connections
            return -1;
        }
        if (nodeConnections[start].contains(end)) {
            return 1;
        }

        resetDiscovered();
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            distTo[i] = -1;
        }

        distTo[start] = 0;
        discovered[start] = true;
        q.add(start);

        while (!q.isEmpty()) {
            int v = q.remove();
            TreeSet<Integer> list = nodeConnections[v];
            if (list.size() != 0) {
                Iterator<Integer> iter = list.iterator();
                while (iter.hasNext()) {
                    int childNode = iter.next();
                    if (!discovered[childNode]) {
                        edgeTo[childNode] = v;
                        distTo[childNode] = distTo[v] + 1;
                        if (childNode == end) {
                            count = distTo[childNode];
                            return count;
                        }
                        discovered[childNode] = true;
                        q.add(childNode);
                    }
                }

            }
        }
        count = -1;
        return count;
    }


    public int minimalNumberOfConnections(int start, int end) {
        int count = calculateShortestPath(start, end);
        return count;
    }

    public List<Integer> criticalNodes() {
        critical = new LinkedList<>();
        for (int i = 0; i < numberOfNodes; ++i) {
            disct[i] = parent[i] = -1;
        }
        resetDiscovered();
        calculateCriticalNodes();


        return critical;
    }

    private void calculateCriticalNodes() {
        for (int i = 0; i < numberOfNodes; i++) {
            if (nodeConnections[i].size() == 0) {
                discovered[i] = true;
            }
            if (!discovered[i]) {
                dfs(i);
            }

        }
    }


    void dfs(int v) {
        int children = 0;
        discovered[v] = true;
        disct[v] = low[v] = ++dsfCounter;
        TreeSet<Integer> neighbours = nodeConnections[v];
        Iterator<Integer> iter = neighbours.iterator();
        while (iter.hasNext()) {
            int neighbour = iter.next();
            if (parent[v] == neighbour) {
                continue;
            }
            if (!discovered[neighbour]) {
                parent[neighbour] = v;
                dfs(neighbour);
                children++;

                low[v] = Math.min(low[v], low[neighbour]);
                if (!critical.contains(v)) {
                    if (children > 1) {
                        if (parent[v] == -1) {
                            critical.add(v);
                        }
                    }
                    if (parent[v] != -1) {
                        if (low[neighbour] >= disct[v]) {
                            critical.add(v);
                        }
                    }
                }

            } else if (neighbour != parent[v]) {
                low[v] = Math.min(low[v], disct[neighbour]);
            }
        }

    }

    private void resetDiscovered() {
        for (int i = 0; i < discovered.length; i++) {
            discovered[i] = false;
        }
    }

}