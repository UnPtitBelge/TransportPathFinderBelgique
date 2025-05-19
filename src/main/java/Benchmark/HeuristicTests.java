package Benchmark;

import Algorithm.Astar;
import Models.PathEdge;
import Models.Stop;
import Models.Trip;
import Parser.Parser;
import Utils.Helper;
import Utils.Heuristic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HeuristicTests {

    public static void main(String[] args) {
        boolean dijkstra = false;

        Map<String, Stop> stops = Parser.parse();
        int departureTime = Helper.convertTimeToSeconds("08:00:00");

        if (dijkstra) Heuristic.setDijkstraMode(true);

        // Reste du code inchangé
        List<Stop> stopList = new ArrayList<>(stops.values());
        Collections.shuffle(stopList);
        int nbPairs = 20;
        if (stopList.size() < nbPairs * 2) {
            System.out.println("Pas assez d'arrêts pour le test.");
            return;
        }
        List<Stop> selectedStops = stopList.subList(0, nbPairs * 2);

        for (int i = 0; i < nbPairs; i++) {
            Stop departureStop = Helper.findStopByName(
                selectedStops.get(i * 2).name(),
                stops
            );
            Stop arrivalStop = Helper.findStopByName(
                selectedStops.get(i * 2 + 1).name(),
                stops
            );
            if (departureStop == null || arrivalStop == null) {
                System.out.println("Invalid stop names.");
                return;
            }
            List<PathEdge> result = validateHeuristic(
                departureStop,
                arrivalStop,
                departureTime,
                stops
            );
            printPathSegments(result, departureTime);
        }
    }

    public static boolean isAdmissibleHeuristic(
        Stop n,
        Stop d,
        int departureTime,
        Map<String, Stop> stops
    ) {
        double hN = Heuristic.heuristic(n, d);
        double realCost = calculateRealCost(n, d, departureTime, stops);
        System.out.println("Heuristic: " + hN + " vs " + realCost);
        return hN <= realCost;
    }

    public static double calculateRealCost(
        Stop start,
        Stop end,
        int departureTime,
        Map<String, Stop> stops
    ) {
        Astar astar = new Astar(stops.size(), stops);
        Heuristic.setDijkstraMode(true);
        List<PathEdge> result = astar.searchPath(start, end, departureTime);
        Heuristic.setDijkstraMode(false);
        if (!result.isEmpty()) {
            return result.getLast().arrival() - departureTime;
        }
        return Double.POSITIVE_INFINITY;
    }

    public static List<PathEdge> validateHeuristic(
        Stop start,
        Stop end,
        int departureTime,
        Map<String, Stop> stops
    ) {
        Astar astar = new Astar(stops.size(), stops);
        Heuristic.setDijkstraMode(true);
        List<PathEdge> optimalPath = astar.searchPath(
            start,
            end,
            departureTime
        );
        Heuristic.setDijkstraMode(false);
        if (optimalPath == null || optimalPath.isEmpty()) {
            System.out.println("No optimal path found");
            return optimalPath;
        }
        System.out.println(
            "Optimal stops expanded: " + astar.getStopsExpanded()
        );
        boolean isAdmissibleHeuristic = true;
        boolean isConsistentHeuristic = true;
        for (PathEdge node : optimalPath) {
            Stop from = node.fromStop();
            Stop to = node.toStop();
            int travelTime = node.arrival() - node.departureTime();
            double hN = Heuristic.heuristic(to, end);
            double hP = Heuristic.heuristic(from, end);
            double realCost = calculateRealCost(
                to,
                end,
                node.departureTime(),
                stops
            );

            if (hN > travelTime + hP) {
                isConsistentHeuristic = false;
            } else if (hN > realCost) {
                isAdmissibleHeuristic = false;
            }
        }
        System.out.println("isAdmissibleHeuristic: " + isAdmissibleHeuristic);
        System.out.println("isConsistentHeuristic: " + isConsistentHeuristic);
        return optimalPath;
    }

    public static void printPathSegments(
        List<PathEdge> result,
        int departureTime
    ) {
        if (result == null) {
            System.out.println("No path found.");
            return;
        }

        System.out.println("Path segments:");

        Trip currentTrip = null;
        Stop segmentStart = null;
        Stop segmentEnd = null;
        int segmentDepartureTime = 0;
        int segmentArrivalTime = 0;
        boolean first = true;

        for (PathEdge edge : result) {
            Trip trip = edge.trip();
            Stop fromStop = edge.fromStop();
            Stop toStop = edge.toStop();
            int edgeDepartureTime = edge.departureTime();
            int edgeArrivalTime = edge.arrival();
            boolean isWalking = trip.id().equals("WALK");

            if (first) {
                currentTrip = trip;
                segmentStart = fromStop;
                segmentDepartureTime = edgeDepartureTime;
                segmentEnd = toStop;
                segmentArrivalTime = edgeArrivalTime;
                first = false;
                continue;
            }
            if (
                !isSameTrip(currentTrip, trip) ||
                isCorrespondance(fromStop, toStop, isWalking)
            ) {
                printSegmentLine(
                    currentTrip,
                    segmentStart,
                    segmentEnd,
                    segmentDepartureTime,
                    segmentArrivalTime
                );
                currentTrip = trip;
                segmentStart = fromStop;
                segmentDepartureTime = edgeDepartureTime;
            }
            segmentEnd = toStop;
            segmentArrivalTime = edgeArrivalTime;
        }
        boolean isWalking =
            currentTrip != null && currentTrip.id().equals("WALK");
        if (!isCorrespondance(segmentStart, segmentEnd, isWalking)) {
            printSegmentLine(
                currentTrip,
                segmentStart,
                segmentEnd,
                segmentDepartureTime,
                segmentArrivalTime
            );
        }
        int totalJourneyTime = segmentArrivalTime - departureTime;
        System.out.println(
            "\nTotal journey time: " +
            Helper.convertSecondsToTime(totalJourneyTime) +
            " (" +
            (totalJourneyTime / 60) +
            " minutes)"
        );
    }

    private static void printSegmentLine(
        Trip trip,
        Stop from,
        Stop to,
        int departureTime,
        int arrivalTime
    ) {
        String transport = trip == null ? "WALK" : trip.route().transportType();
        String routeName = trip == null ? "" : trip.route().name();

        System.out.printf(
            "%s | %s | %s → %s | Depart: %s, Arrive: %s, Duration: %d min\n",
            transport,
            routeName,
            from.name(),
            to.name(),
            Helper.convertSecondsToTime(departureTime),
            Helper.convertSecondsToTime(arrivalTime),
            (arrivalTime - departureTime) / 60
        );
    }

    public static boolean isSameTrip(Trip trip1, Trip trip2) {
        // Handle null trips (walking segments)
        if (trip1 == null || trip2 == null) return false;
        return trip1.equals(trip2);
    }

    public static boolean isCorrespondance(
        Stop from,
        Stop to,
        boolean isWalking
    ) {
        if (isWalking && from.name().equalsIgnoreCase(to.name())) {
            return true;
        }
        return false;
    }
}
