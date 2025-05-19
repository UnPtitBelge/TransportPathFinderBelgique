package astar.theorically.Utils;

import astar.theorically.Models.Stop;
import astar.theorically.Models.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Helper {

    public static double distance(Stop stop1, Stop stop2) {
        return haversine(stop1.lat(), stop1.lon(), stop2.lat(), stop2.lon());
    }

    public static double haversine(
        double lat1,
        double lon1,
        double lat2,
        double lon2
    ) {
        final double R = 6_371_000; // Earth's radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) *
            Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static Stop findStopByName(String name, Map<String, Stop> stops) {
        List<Stop> candidates = new ArrayList<>();
        for (Stop stop : stops.values()) {
            if (stop.name().equals(name)) {
                candidates.add(stop);
            } else if (stop.name().equalsIgnoreCase(name)) {
                candidates.add(stop);
            } else if (stop.name().toLowerCase().contains(name.toLowerCase()) ||
                    name.toLowerCase().toLowerCase().contains(stop.name())) {
                candidates.add(stop);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }
        Stop firstCandidate = candidates.getFirst();
        Stop superStop = new Stop(
                firstCandidate.id() + "-SUPER",
                name,
                firstCandidate.lat(),
                firstCandidate.lon(),
                stops.size()
        );
        for (Stop candidateStop : candidates) {
            superStop.addNeighbours(
                    candidateStop.neighbours()
            );
        }
        stops.put(superStop.id(), superStop);
        return superStop;
    }

    public static String convertSecondsToTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public static int convertTimeToSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }

    public static boolean isCorrespondance(
            Stop from,
            Stop to,
            boolean isWalking
    ) {
        return isWalking && from.name().equalsIgnoreCase(to.name());
    }

    public static boolean isTransfer(Stop from, Stop to, Trip trip, Trip lastTrip, boolean isWalking) {
        String routeName = trip != null ? trip.route().name() : "";
        String lastRouteName =  lastTrip != null ? lastTrip.route().name() : "";
        return isCorrespondance(from, to, isWalking) || !routeName.equals(lastRouteName);
    }
}
