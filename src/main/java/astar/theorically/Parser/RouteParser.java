package astar.theorically.Parser;

import astar.theorically.Models.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteParser {

    private static final List<String> PATHS = List.of(
            "GTFS/DELIJN/routes.csv",
            "GTFS/SNCB/routes.csv",
            "GTFS/STIB/routes.csv",
            "GTFS/TEC/routes.csv"
    );

    public static Map<String, Route> parseAllRoutes() {
        Map<String, Route> routes = new HashMap<>();

        CsvLoader.loadFiles(PATHS, routes, (row, map) -> {
            String routeId = row[0].trim();
            String shortName = row[1].trim();
            String longName = row[2].trim();
            String type = row[3].trim();
            map.put(routeId, new Route(routeId, shortName, longName, type));
        });


        System.out.println("Loaded " + routes.size() + " routes");
        return routes;
    }
}
