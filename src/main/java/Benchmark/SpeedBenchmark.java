package Benchmark;

import Algorithm.Astar;
import Models.PathEdge;
import Models.Stop;
import Parser.Parser;
import Utils.Helper;
import Utils.Heuristic;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SpeedBenchmark {

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
        boolean random = false;
        String staticCsvPath =
            "src/test/resources/benchmark/speed_benchmark.csv";
        String randomCsvPath =
            "src/test/resources/benchmark/speed_benchmark_random.csv";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--random":
                case "-r":
                    random = true;
                    break;
                case "--csv":
                case "-c":
                    if (i + 1 < args.length) {
                        staticCsvPath = args[++i];
                    }
                    break;
            }
        }

        Map<String, Stop> stops = Parser.parse();
        try {
            if (random) {
                exportRandomSpeedBenchmarks(
                    stops,
                    randomCsvPath,
                    staticCsvPath
                );
            } else {
                exportSpeedBenchmarks(stops, staticCsvPath);
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public static void exportSpeedBenchmarks(
        Map<String, Stop> allStops,
        String path
    ) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(
                "trip,speed(m/s),distance(km),travel_time,stops_extended\n"
            );

            int departureTime = Helper.convertTimeToSeconds("08:00:00");
            Heuristic.setDijkstraMode(true);

            for (String[] pair : paths) {
                Stop from = Helper.findStopByName(pair[0], allStops);
                Stop to = Helper.findStopByName(pair[1], allStops);
                Astar astar = new Astar(allStops.size(), allStops);
                if (from == null || to == null) continue;

                for (int speed = 10; speed <= 28; speed += 2) {
                    Heuristic.setSpeed(speed);
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
                                "\"%s\" -> \"%s\",%d,\"%.1f\",%s,%d\n",
                                from.name(),
                                to.name(),
                                speed,
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
        System.out.println("Exported speed benchmark data to CSV.");
    }

    public static void exportRandomSpeedBenchmarks(
        Map<String, Stop> allStops,
        String pathRandom,
        String pathStatic
    ) throws IOException {
        List<Stop> stopList = new ArrayList<>(allStops.values());
        Collections.shuffle(stopList);
        if (stopList.size() < 120) {
            throw new IllegalArgumentException(
                "Il faut au moins 120 arrêts pour ce benchmark."
            );
        }
        List<Stop> selectedStops = stopList.subList(0, 120);

        try (
            BufferedWriter writerRandom = new BufferedWriter(
                new FileWriter(pathRandom)
            );
            BufferedWriter writerStatic = new BufferedWriter(
                new FileWriter(pathStatic)
            )
        ) {
            writerRandom.write(
                "trip,speed(m/s),distance(km),travel_time, stops_extended\n"
            );
            writerStatic.write(
                "trip,speed(m/s),distance(km),travel_time, stops_extended\n"
            );
            int departureTime = Helper.convertTimeToSeconds("08:00:00");

            for (int i = 0; i < 60; i++) {
                Stop from = selectedStops.get(i * 2);
                Stop to = selectedStops.get(i * 2 + 1);
                from = Helper.findStopByName(from.name(), allStops);
                to = Helper.findStopByName(to.name(), allStops);
                Astar astar = new Astar(allStops.size(), allStops);

                for (int speed = 2; speed <= 26; speed += 2) {
                    Heuristic.setSpeed(speed);
                    List<PathEdge> pathResult = astar.searchPath(
                        from,
                        to,
                        departureTime
                    );

                    if (pathResult != null && !pathResult.isEmpty()) {
                        int arrival = pathResult.getLast().arrival();
                        int stopsExpanded = astar.getStopsExpanded();
                        writerRandom.write(
                            String.format(
                                Locale.FRANCE,
                                "\"%s\" -> \"%s\",%d,\"%.1f\",%s,%d\n",
                                from.name(),
                                to.name(),
                                speed,
                                Helper.distance(from, to) / 1000.0,
                                Helper.convertSecondsToTime(
                                    arrival - departureTime
                                ),
                                stopsExpanded
                            )
                        );
                    }
                }
                Heuristic.setSpeed(24.5);
                List<PathEdge> pathResult = astar.searchPath(
                    from,
                    to,
                    departureTime
                );

                if (pathResult != null && !pathResult.isEmpty()) {
                    int arrival = pathResult.getLast().arrival();
                    int stopsExpanded = astar.getStopsExpanded();
                    writerStatic.write(
                        String.format(
                            Locale.FRANCE,
                            "\"%s\" -> \"%s\",%.1f,\"%.1f\",%s,%d\n",
                            from.name(),
                            to.name(),
                            24.5,
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
        System.out.println("Exported random speed benchmark data to CSV.");
    }
}
