package astar.theorically.Models;

import java.util.Objects;

/**
 * @param id trip attributes
 */
public record Trip(String id, Route route) {

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Trip trip = (Trip) obj;
        return id.equals(trip.id()) || route.equals(trip.route());
    }
}
