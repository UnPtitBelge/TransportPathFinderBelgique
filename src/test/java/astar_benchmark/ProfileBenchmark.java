package astar_benchmark;

import astar.theorically.Algorithm.Astar;
import astar.theorically.Models.PathEdge;
import astar.theorically.Models.Stop;
import astar.theorically.Parser.Parser;
import astar.theorically.Utils.Helper;
import astar.theorically.Utils.Heuristic;
import astar.theorically.Utils.Profiles;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ProfileBenchmark {

    private static final String[][] paths = {
        { "Alveringem Nieuwe Herberg", "Aubange" },
        { "Brugge", "Brussel" },
        { "Antwerpen", "Gent" },
        { "Bruxelles-Midi", "Bruxelles-Central" },
        { "Paris Nord", "Amsterdam Cs" },
        { "schuman", "simonis" },
        { "etterbeek", "schaerbeek" },
        { "genk", "ostende" },
        { "liege", "namur" },
        { "delta", "aubange" },
        { "ulb", "rogier" },
    };

    public static void main(String[] args) {
        boolean dijkstra = false;
        boolean random = false;
        String csvPath = "src/test/resources/profile_benchmark.csv";
        String staticCsvPath = "src/test/resources/profile_benchmark_static.csv";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--dijkstra":
                case "-d":
                    dijkstra = true;
                    break;
                case "--random":
                case "-r":
                    random = true;
                    break;
                case "--csv":
                case "-c":
                    if (i + 1 < args.length) {
                        csvPath = args[++i];
                    }
                    break;
                case "--static":
                case "-s":
                    random = false;
                    break;
            }
        }

        Map<String, Stop> stops = Parser.parse();
        try {
            if (dijkstra) Heuristic.setDijkstraMode(true);
            if (random) {
                exportRandomProfileBenchmarks(stops, csvPath);
            } else {
                exportProfileBenchmarks(stops, staticCsvPath);
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public static void exportProfileBenchmarks(
        Map<String, Stop> allStops,
        String path
    ) throws IOException {
        String[] profiles = Profiles.getAvailableProfiles();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(
                "trip,profile,distance(km),travel_time,execution_time(ms)\n"
            );

            for (String[] pair : paths) {
                Stop from = Helper.findStopByName(pair[0], allStops);
                Stop to = Helper.findStopByName(pair[1], allStops);
                if (from == null || to == null) continue;

                Astar astar = new Astar(allStops.size(), allStops);

                for (String profile : profiles) {
                    Profiles.setProfile(profile);
                    int departureTime = Helper.convertTimeToSeconds("08:00:00");

                    long startTime = System.currentTimeMillis();
                    List<PathEdge> pathResult = astar.searchPath(
                        from,
                        to,
                        departureTime
                    );
                    long endTime = System.currentTimeMillis();
                    long executionTime = endTime - startTime;

                    if (pathResult != null && !pathResult.isEmpty()) {
                        int arrival = pathResult.getLast().arrival();
                        writer.write(
                            String.format(
                                Locale.FRANCE,
                                "\"%s\" -> \"%s\",%s,\"%.1f\",%s,%d\n",
                                from.name(),
                                to.name(),
                                profile,
                                Helper.distance(from, to) / 1000.0,
                                Helper.convertSecondsToTime(
                                    arrival - departureTime
                                ),
                                executionTime
                            )
                        );
                    }
                }
            }
        }
        System.out.println("Exported profile benchmark data to CSV.");
    }

    public static void exportRandomProfileBenchmarks(
        Map<String, Stop> allStops,
        String path
    ) throws IOException {
        String[] profiles = Profiles.getAvailableProfiles();
        List<Stop> stopList = new ArrayList<>(allStops.values());
        Collections.shuffle(stopList);
        if (stopList.size() < 120) {
            throw new IllegalArgumentException("Il faut au moins 120 arrêts pour ce benchmark.");
        }
        List<Stop> selectedStops = stopList.subList(0, 120);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(
                "trip,profile,distance(km),travel_time,stops_expanded\n"
            );
            int departureTime = Helper.convertTimeToSeconds("08:00:00");

            for (int i = 0; i < 60; i++) {
                Stop from = selectedStops.get(i * 2);
                Stop to = selectedStops.get(i * 2 + 1);
                from = Helper.findStopByName(from.name(), allStops);
                to = Helper.findStopByName(to.name(), allStops);
                Astar astar = new Astar(allStops.size(), allStops);

                for (String profile : profiles) {
                    Profiles.setProfile(profile);

                    List<PathEdge> pathResult = astar.searchPath(
                        from,
                        to,
                        departureTime
                    );

                    if (pathResult != null && !pathResult.isEmpty()) {
                        int arrival = pathResult.getLast().arrival();
                        int stopsExpanded = astar.getStopsExpanded();
                        writer.write(
                            String.format(
                                Locale.FRANCE,
                                "\"%s\" -> \"%s\",%s,\"%.1f\",%s,%d\n",
                                from.name(),
                                to.name(),
                                profile,
                                Helper.distance(from, to) / 1000.0,
                                Helper.convertSecondsToTime(
                                    arrival - departureTime
                                ),
                                stopsExpanded
                            )
                        );
                    }
                }
            }
        }
        System.out.println("Exported random profile benchmark data to CSV.");
    }
}
