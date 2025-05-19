package Models;

import java.util.*;

public record Stop(
        String id,
        String name,
        double lat,
        double lon,
        Set<Neighbour> neighbours,
        int index
) {
    public Stop(String id, String name, double lat, double lon, int index) {
        this(id, name, lat, lon, new HashSet<>(), index);
    }

    public void addNeighbours(Set<Neighbour> neighbours) {
        this.neighbours.addAll(neighbours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Stop stop = (Stop) obj;
        return id.equals(stop.id())
                || name.equalsIgnoreCase(stop.name())
                || (lat == stop.lat() && lon == stop.lon());
    }
}
