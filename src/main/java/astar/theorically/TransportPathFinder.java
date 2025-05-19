package astar.theorically;

import astar.theorically.Algorithm.Astar;
import astar.theorically.Models.PathEdge;
import astar.theorically.Models.Stop;
import astar.theorically.Models.Trip;
import astar.theorically.Parser.Parser;
import astar.theorically.Utils.Helper;
import astar.theorically.Utils.Heuristic;
import astar.theorically.Utils.Profiles;
import java.util.*;

public class TransportPathFinder {

    public static void main(String[] args) {
        // Valeurs par défaut
        boolean dijkstra = false;
        double speed = Heuristic.getSpeed();
        double walkSpeed = Profiles.getWalkSpeed();
        String profile = Profiles.getCurrentProfile();
        boolean customProfile = false;
        double[] customMalus = new double[7];
        String departureName = null;
        String arrivalName = null;
        String departureTimeStr = null;

        // Lecture des arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--dijkstra":
                case "-d":
                    dijkstra = true;
                    break;
                case "--speed":
                case "-s":
                    if (i + 1 < args.length) {
                        speed = Double.parseDouble(args[++i]);
                    }
                    break;
                case "--walk-speed":
                case "-ws":
                    if (i + 1 < args.length) {
                        walkSpeed = Double.parseDouble(args[++i]);
                    }
                    break;
                case "--profile":
                case "-p":
                    if (i + 1 < args.length) {
                        profile = args[++i];
                    }
                    break;
                case "--custom-profile":
                    customProfile = true;
                    for (int j = 0; j < 7 && i + 1 < args.length; j++) {
                        customMalus[j] = Double.parseDouble(args[++i]);
                    }
                    break;
            }
        }

        Scanner scanner = new Scanner(System.in);
        if (departureName == null) {
            System.out.print("Departure stop name : ");
            departureName = scanner.nextLine();
        }
        if (arrivalName == null) {
            System.out.print("Arrival stop name : ");
            arrivalName = scanner.nextLine();
        }
        if (departureTimeStr == null) {
            System.out.print("Start trip at (HH:mm:ss) : ");
            departureTimeStr = scanner.nextLine();
        }
        scanner.close();

        int departureTime = Helper.convertTimeToSeconds(departureTimeStr);

        Map<String, Stop> stops = Parser.parse();

        // Appliquer les options
        if (dijkstra) {
            Heuristic.setDijkstraMode(true);
            System.out.println("Dijkstra mode activated.");
        } else {
            Heuristic.setDijkstraMode(false);
            Heuristic.setSpeed(speed);
            System.out.println("Heuristic speed = " + speed + " m/s");
        }
        if (walkSpeed > 0) {
            Profiles.setWalkSpeed(walkSpeed);
            System.out.println(
                "Walking speed defined as " + walkSpeed + " m/s"
            );
        }

        if (customProfile) {
            String customName = "CUSTOM";
            Profiles.addProfile(customName, customMalus);
            Profiles.setProfile(customName);
            System.out.println(
                "Custom profile selected : " + Arrays.toString(customMalus)
            );
        } else {
            Profiles.setProfile(profile);
            System.out.println("Current profile : " + profile);
        }

        // Recherche des arrêts
        Stop departureStop = Helper.findStopByName(departureName, stops);
        Stop arrivalStop = Helper.findStopByName(arrivalName, stops);
        if (departureStop == null || arrivalStop == null) {
            System.out.println("Invalid stop names.");
            return;
        }

        // Recherche de chemin
        Astar pathFinder = new Astar(stops.size(), stops);
        List<PathEdge> result = pathFinder.searchPath(
            departureStop,
            arrivalStop,
            departureTime
        );
        printPathSegments(result, departureTime);
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

        // Variables for segment grouping
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

            // If the trip is the same, extend the segment or is a correspondance
            if (
                !isSameTrip(currentTrip, trip) ||
                Helper.isCorrespondance(fromStop, toStop, isWalking)
            ) {
                // Print the previous segment
                printSegmentLine(
                    currentTrip,
                    segmentStart,
                    segmentEnd,
                    segmentDepartureTime,
                    segmentArrivalTime
                );
                // Start new segment
                currentTrip = trip;
                segmentStart = fromStop;
                segmentDepartureTime = edgeDepartureTime;
            }
            segmentEnd = toStop;
            segmentArrivalTime = edgeArrivalTime;
        }
        // Print the last segment
        boolean isWalking =
            currentTrip != null && currentTrip.id().equals("WALK");
        if (!Helper.isCorrespondance(segmentStart, segmentEnd, isWalking)) {
            printSegmentLine(
                currentTrip,
                segmentStart,
                segmentEnd,
                segmentDepartureTime,
                segmentArrivalTime
            );
        }

        // Print total journey time
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
}
