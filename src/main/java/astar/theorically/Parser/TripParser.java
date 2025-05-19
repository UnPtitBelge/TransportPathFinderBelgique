package astar.theorically.Parser;

import astar.theorically.Models.Route;
import astar.theorically.Models.Trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripParser {

    private static final List<String> PATHS = List.of(
            "GTFS/DELIJN/trips.csv",
            "GTFS/SNCB/trips.csv",
            "GTFS/STIB/trips.csv",
            "GTFS/TEC/trips.csv"
    );

    public static Map<String, Trip> parseAllTrips(Map<String, Route> routes) {
        Map<String, Trip> trips = new HashMap<>();

        CsvLoader.loadFiles(PATHS, trips, (row, container) -> {
            String id = row[0].trim();
            String routeId = row[1].trim();
            Route route = routes.get(routeId);
            if (route != null) {
                container.put(id, new Trip(id, route));
            } else {
                System.err.println("Route not found for routeId: " + routeId);
            }
        });

        System.out.println("Loaded " + trips.size() + " trips");
        return trips;
    }
}
