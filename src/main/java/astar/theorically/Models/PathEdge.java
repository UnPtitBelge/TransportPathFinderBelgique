package astar.theorically.Models;

public record PathEdge(
    Stop fromStop,
    Stop toStop,
    Trip trip,
    int departureTime,
    int arrival
) {}
