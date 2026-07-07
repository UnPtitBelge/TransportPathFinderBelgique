package com.ulb.astar;

import com.ulb.util.Position;

public class AStarHeuristic {

    private static final double MAX_SPEED_KMH = 130.0;

    public static double estimate(Position nextStop, Position goal) {
        double distKm = nextStop.distanceKm(goal);
        return (distKm / MAX_SPEED_KMH) * 3600.0;
    }
}
