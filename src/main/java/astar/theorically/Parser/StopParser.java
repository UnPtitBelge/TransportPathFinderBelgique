package astar.theorically.Parser;

import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import astar.theorically.Models.Neighbour;
import astar.theorically.Models.Route;
import astar.theorically.Models.Stop;
import astar.theorically.Models.Trip;
import astar.theorically.Utils.Helper;
import astar.theorically.Utils.Profiles;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StopParser {

    private static final List<String> PATHS = List.of(
        "GTFS/DELIJN/stops.csv",
        "GTFS/SNCB/stops.csv",
        "GTFS/STIB/stops.csv",
        "GTFS/TEC/stops.csv"
    );

    private static final Trip WALK_TRIP = new Trip(
        "",
        new Route("WALK", "WALK", "", "WALK")
    );

    private static RTree<Stop, Point> rStarTree = null;

    public static Map<String, Stop> parseAllStops() {
        Map<String, Stop> stops = new HashMap<>();

        AtomicInteger counter = new AtomicInteger(0);
        CsvLoader.loadFiles(PATHS, stops, (row, container) -> {
            String stopId = row[0].trim();
            String stopName = row[1].trim();
            double lat = Double.parseDouble(row[2].trim());
            double lon = Double.parseDouble(row[3].trim());

            Stop stop = new Stop(stopId, stopName, lat, lon, counter.getAndIncrement());
            container.put(stopId, stop);
        });

        List<Entry<Stop, Point>> entries = new ArrayList<>();
        for (Stop stop : stops.values()) {
            entries.add(
                Entries.entry(stop, Geometries.point(stop.lon(), stop.lat()))
            );
        }

        // Create the R*-Tree with the entries (bulk insert)
        rStarTree = RTree.star().create(entries);

        System.out.println("Loaded " + stops.size() + " stops");

        buildWalkableConnections(stops.values());

        return stops;
    }

    private static void buildWalkableConnections(
        Collection<Stop> allStops
    ) {
        int count = 0;
        for (Stop stop : allStops) {
            String operator = Profiles.getOperatorFromStopId(stop.id());
            double radius = Profiles.getRadiusByOperator(operator);

            double searchRadiusLat = radius / 111000;
            double searchRadiusLon =
                radius / (111000 * Math.cos(Math.toRadians(stop.lat())));

            Iterable<Entry<Stop, Point>> results = rStarTree
                .search(
                    Geometries.rectangle(
                        stop.lon() - searchRadiusLon,
                        stop.lat() - searchRadiusLat,
                        stop.lon() + searchRadiusLon,
                        stop.lat() + searchRadiusLat
                    )
                )
                .toBlocking()
                .toIterable();

            for (Entry<Stop, Point> entry : results) {
                Stop candidate = entry.value();
                if (!candidate.equals(stop)) {
                    double distance = Helper.distance(stop, candidate);
                    if (distance <= radius) {
                        double travelTime = distance / Profiles.getWalkSpeed();
                        stop.neighbours().add( new Neighbour(
                            candidate,
                            WALK_TRIP,
                            -1,
                            (int) travelTime,
                                true
                        ));
                        count++;
                    }
                }
            }
        }
        System.out.println("Connected " + count + " walkable connections.");
    }
}
