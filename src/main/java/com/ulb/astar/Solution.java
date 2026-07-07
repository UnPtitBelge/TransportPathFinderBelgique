package com.ulb.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ulb.model.gtfs.Stop;
import com.ulb.model.gtfs.Trip;

public class Solution {

    private final List<SolutionEntry> solution;

    public Solution(List<SolutionEntry> solution) {
        this.solution = solution;
    }

    public static Solution fromNode(StopNode node) {
        List<SolutionEntry> entries = new ArrayList<>();
        StopNode current = node;

        // Reconstruct path from end to start
        while (current.parent() != null) {
            entries.add(0, new SolutionEntry(
                    current.parent().stop(),
                    current.departureTime(),
                    current.stop(),
                    current.arrivalTime(),
                    current.previousTrip()));
            current = current.parent();
        }

        return new Solution(entries);
    }

    public List<SolutionEntry> getSolution() {
        return Collections.unmodifiableList(this.solution);
    }

    public record SolutionEntry(Stop departure, int departureTime, Stop arrival, int arrivalTime, Trip trip) {
    }
}
