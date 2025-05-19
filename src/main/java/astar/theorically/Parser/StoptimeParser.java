package astar.theorically.Parser;

import astar.theorically.Models.Neighbour;
import astar.theorically.Models.Stop;
import astar.theorically.Models.Trip;
import astar.theorically.Utils.Helper;
import java.util.*;

public class StoptimeParser {

    private static final List<String> PATHS = List.of(
        "GTFS/DELIJN/stop_times.csv",
        "GTFS/SNCB/stop_times.csv",
        "GTFS/STIB/stop_times.csv",
        "GTFS/TEC/stop_times.csv"
    );

    public static void parseAllStopTimes(
        Map<String, Trip> trips,
        Map<String, Stop> stops
    ) {
        Map<Trip, List<StopTime>> stopTimesByTrip = new HashMap<>();

        CsvLoader.loadFiles(PATHS, stopTimesByTrip, (row, map) -> {
            String tripId = row[0].trim();
            Trip trip = trips.get(tripId);
            int departure = Helper.convertTimeToSeconds(row[1].trim());
            String stopId = row[2].trim();
            Stop stop = stops.get(stopId);
            byte sequence = (byte) Integer.parseInt(row[3].trim());

            if (trip == null || stop == null) {
                System.err.printf(
                    "Missing trip or stop for tripId=%s, stopId=%s%n",
                    tripId,
                    stopId
                );
                return;
            }

            map
                .computeIfAbsent(trip, _ -> new ArrayList<>())
                .add(new StopTime(stop, departure, sequence));
        });

        if (!stopTimesByTrip.isEmpty()) {
            setConnexions(stopTimesByTrip);
        }

        System.out.println("Loaded " + stopTimesByTrip.size() + " stop times");
    }

    private static void setConnexions(
        Map<Trip, List<StopTime>> stopTimesByTrip
    ) {
        for (Map.Entry<
            Trip,
            List<StopTime>
        > entry : stopTimesByTrip.entrySet()) {
            Trip trip = entry.getKey();
            List<StopTime> stopTimes = entry.getValue();

            if (stopTimes.size() < 2) {
                continue;
            }

            stopTimes.sort(Comparator.comparingInt(StopTime::sequence));

            // Process sorted stop times
            for (int i = 0; i < stopTimes.size() - 1; i++) {
                StopTime current = stopTimes.get(i);
                StopTime next = stopTimes.get(i + 1);

                int cost = next.departure() - current.departure();
                if (cost < 0) {
                    System.err.printf(
                        "Negative cost for trip %s from %s to %s: %d seconds%n",
                        trip.id(),
                        current.stop().id(),
                        next.stop().id(),
                        cost
                    );
                    continue;
                }

                current
                    .stop()
                    .neighbours().add( new Neighbour(
                        next.stop(),
                        trip,
                        current.departure(),
                        cost,
                        false
                    ));
            }
        }
    }

    // Simple class to hold stop and time together
    private record StopTime(Stop stop, int departure, byte sequence) {}
}
