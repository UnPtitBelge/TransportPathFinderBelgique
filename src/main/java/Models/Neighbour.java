package Models;

public record Neighbour(Stop stop, Trip trip, int departureTime, int travelTime, boolean isWalking) {
}
