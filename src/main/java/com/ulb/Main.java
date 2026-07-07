package com.ulb;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ulb.astar.Astar;
import com.ulb.astar.Solution;
import com.ulb.astar.SolutionFormatter;
import com.ulb.model.gtfs.Road;
import com.ulb.model.gtfs.Timetable;
import com.ulb.parser.Parser;
import com.ulb.util.Profiler;
import com.ulb.util.Utils;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Profiler.logEnvironment();

        try (Profiler total = Profiler.start("total")) {
            Parser parser = new Parser();
            parser.parseAll();

            Map<String, Road> roads = parser.getRoads();
            Timetable timetable = parser.getTimetable();

            Astar astar = new Astar(timetable);
            Optional<Solution> solution = astar.run("Alveringem Nieuwe Herberg", "Aubange",
                    Utils.timeToSeconds("10:30:00"));

            if (solution.isPresent()) {
                SolutionFormatter formatter = new SolutionFormatter(roads);
                formatter.printSolution(solution.get());
            } else {
                logger.warn("No solution found");
            }
        }
    }
}
