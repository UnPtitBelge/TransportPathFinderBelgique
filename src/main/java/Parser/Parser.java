package Parser;

import Models.Route;
import Models.Stop;
import Models.Trip;
import java.util.Map;

public class Parser {

    public static Map<String, Stop> parse() {
        System.out.println("Parsing GTFS...");
        Map<String, Route> routes = RouteParser.parseAllRoutes();
        Map<String, Trip> trips = TripParser.parseAllTrips(routes);
        Map<String, Stop> stops = StopParser.parseAllStops();
        StoptimeParser.parseAllStopTimes(trips, stops);
        System.out.println("Parsing complete");

        return stops;
    }
}
