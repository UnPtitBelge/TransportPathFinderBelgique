package com.ulb.astar;

import com.ulb.model.gtfs.Stop;
import com.ulb.model.gtfs.Trip;

public record StopNode(Stop stop, StopNode parent, Trip previousTrip, double cost,
        int departureTime, int arrivalTime) {
}
