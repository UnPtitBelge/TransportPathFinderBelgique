package Algorithm;

import algs4.IndexMinPQ;
import java.util.*;
import Models.Neighbour;
import Models.PathEdge;
import Models.Stop;
import Models.Trip;
import Utils.Helper;
import Utils.Heuristic;
import Utils.Profiles;

/**
 * Implements the A* pathfinding algorithm for a transit map.
 * Uses stops and trips to find the optimal path between two stops,
 * considering travel time, waiting time, and transfer penalties.
 */
public class Astar {

    private final int queueSize;

    private final Map<Integer, Stop> indexToStopMap;

    private IndexMinPQ<Double> openQueue;
    private final Set<Stop> closedSet;

    private final Map<Stop, Integer> costSoFar;
    private final Map<Stop, PathEdge> edgeData;

    private Stop destination;

    private int stopsExpanded;

    /**
     * Constructs an Astar instance with the given trips by stop.
     *
     * @param queueSize The number of stops available
     */
    public Astar(int queueSize, Map<String, Stop> stops) {
        this.queueSize = queueSize;
        this.openQueue = new IndexMinPQ<>(queueSize);
        this.closedSet = new HashSet<>();
        this.costSoFar = new HashMap<>();
        this.edgeData = new HashMap<>();
        this.indexToStopMap = new HashMap<>();
    }

    /**
     * Resets all search-related data structures for a new search.
     */
    private void resetSearch() {
        this.openQueue = new IndexMinPQ<>(queueSize);
        this.closedSet.clear();
        this.costSoFar.clear();
        this.edgeData.clear();
        this.indexToStopMap.clear();
    }

    /**
     * Finds the optimal path from departure to destination using A*.
     *
     * @param departure     The starting stop.
     * @param destination   The destination stop.
     * @param departureTime The time of departure.
     * @return The result of the path search, including the path and arrival times.
     */
    public List<PathEdge> searchPath(
        Stop departure,
        Stop destination,
        int departureTime
    ) {
        resetSearch();
        this.destination = destination;
        stopsExpanded = 0;

        costSoFar.put(departure, 0);
        openQueue.insert(departure.index(), 0.0);
        indexToStopMap.put(departure.index(), departure);

        PathEdge startNode = new PathEdge(
            null,
            departure,
            null,
            0,
            departureTime
        );
        edgeData.put(departure, startNode);

        while (!openQueue.isEmpty()) {
            int currentIndex = openQueue.delMin();
            Stop current = indexToStopMap.get(currentIndex);

            if (closedSet.contains(current)) continue;
            closedSet.add(current);

            if (current.equals(destination)) {
                return reconstructPath(current);
            }

            processNeighbours(current, edgeData.get(current));
            stopsExpanded++;
        }

        System.out.println("No path found. Expanded Nodes: " + stopsExpanded);
        return null;
    }

    private void processNeighbours(Stop current, PathEdge currentEdge) {
        int currentArrival = currentEdge.arrival();

        for (Neighbour neighbour : current.neighbours()) {
            Stop neighbourStop = neighbour.stop();
            Trip transportTrip = neighbour.trip();

            boolean isWalking = neighbour.isWalking();
            int departureTime = isWalking
                ? currentArrival
                : neighbour.departureTime();

            if (departureTime < currentArrival) continue;

            int waitTime = departureTime - currentArrival;
            int travelTime = neighbour.travelTime();
            int arrivalTime = departureTime + travelTime;

            boolean isTransfer = Helper.isTransfer(
                current,
                neighbourStop,
                currentEdge.trip(),
                transportTrip,
                isWalking
            );
            int penalty = Profiles.costModifier(
                transportTrip,
                travelTime,
                waitTime,
                isTransfer
            );

            tryToExpand(
                current,
                neighbourStop,
                transportTrip,
                departureTime,
                arrivalTime,
                penalty
            );
        }
    }

    private void tryToExpand(
        Stop current,
        Stop neighbourStop,
        Trip neighbourTrip,
        int departureTime,
        int arrivalTime,
        int tripCost
    ) {
        int gCost = costSoFar.get(current) + tripCost;

        Integer existing = costSoFar.get(neighbourStop);
        if (existing == null || gCost < existing) {
            createEdge(
                current,
                neighbourStop,
                neighbourTrip,
                gCost,
                departureTime,
                arrivalTime,
                tripCost
            );
        }
    }

    private void createEdge(
        Stop fromStop,
        Stop toStop,
        Trip trip,
        int gCost,
        int departureTime,
        int arrivalTime,
        int tripCost
    ) {
        double heuristic = Heuristic.heuristic(toStop, destination);
        double priority = gCost + heuristic;

        costSoFar.put(toStop, gCost);
        PathEdge newEdge = new PathEdge(
            fromStop,
            toStop,
            trip,
            departureTime,
            arrivalTime
        );
        edgeData.put(toStop, newEdge);

        int stopIndex = toStop.index();
        indexToStopMap.computeIfAbsent(stopIndex, _ -> toStop);
        if (openQueue.contains(stopIndex)) {
            // Update the priority if the new one is lower
            if (priority < openQueue.keyOf(stopIndex)) {
                openQueue.decreaseKey(stopIndex, priority);
            }
        } else {
            openQueue.insert(stopIndex, priority);
        }
    }

    /**
     * Reconstructs the path from the destination stop back to the departure stop.
     *
     * @param stop The destination stop.
     * @return The list of edges representing the path.
     */
    private List<PathEdge> reconstructPath(Stop stop) {
        List<PathEdge> path = new ArrayList<>();
        PathEdge node = edgeData.get(stop);
        while (node.fromStop() != null) {
            path.add(node);
            node = edgeData.get(node.fromStop());
        }
        Collections.reverse(path);
        return path;
    }

    public int getStopsExpanded() {
        return stopsExpanded;
    }
}
