package com.ulb.astar;

import java.util.List;
import java.util.Map;

import com.ulb.model.gtfs.Road;
import com.ulb.model.gtfs.Trip;

public class SolutionFormatter {
    private final Map<String, Road> roads;

    public SolutionFormatter(Map<String, Road> roads) {
        this.roads = roads;
    }

    /**
     * Affiche la solution formatée
     */
    public void printSolution(Solution solution) {
        if (solution == null) {
            System.out.println("No solution found");
            return;
        }

        List<Solution.SolutionEntry> entries = solution.getSolution();

        int i = 0;
        while (i < entries.size()) {
            Solution.SolutionEntry entry = entries.get(i);

            if (entry.trip() == null) {
                printWalkingSegment(entry);
                i++;
                continue;
            }

            // Regroupe les arrêts consécutifs de la même ligne (même agence, type
            // et numéro affichés) en un seul segment plutôt que d'imprimer chaque
            // arrêt intermédiaire, même si le GTFS découpe cette ligne en plusieurs
            // route_id/trip_id successifs (ex: SNCB déclare un route_id distinct
            // par trajet pour une même ligne "P").
            int last = i;
            while (last + 1 < entries.size() && sameLine(entries.get(last + 1), entry)) {
                last++;
            }

            printTransportSegment(entry, entries.get(last));
            i = last + 1;
        }
    }

    private boolean sameLine(Solution.SolutionEntry a, Solution.SolutionEntry b) {
        if (a.trip() == null || b.trip() == null) {
            return false;
        }

        Road roadA = roads.get(a.trip().roadId());
        Road roadB = roads.get(b.trip().roadId());
        if (roadA == null || roadB == null) {
            return false;
        }

        return extractAgency(a.trip().id()).equals(extractAgency(b.trip().id()))
                && roadA.type() == roadB.type()
                && roadA.number().equals(roadB.number());
    }

    /**
     * Affiche un segment en transport, de l'arrêt d'embarquement à l'arrêt de
     * débarquement (les arrêts intermédiaires du même trajet ne sont pas listés).
     */
    private void printTransportSegment(Solution.SolutionEntry boarding, Solution.SolutionEntry alighting) {
        Trip trip = boarding.trip();
        Road road = roads.get(trip.roadId());

        if (road == null) {
            System.out.println("ERROR: Road not found for trip " + trip.id());
            return;
        }

        String agency = extractAgency(trip.id());
        String transportType = road.type().name();
        String routeNumber = String.valueOf(road.number());

        System.out.printf("Take %s %s %s from %s (%s) to %s (%s)%n",
                agency,
                transportType,
                routeNumber,
                boarding.departure().name(),
                formatTime(boarding.departureTime()),
                alighting.arrival().name(),
                formatTime(alighting.arrivalTime()));
    }

    /**
     * Affiche un segment à pied
     */
    private void printWalkingSegment(Solution.SolutionEntry entry) {
        System.out.printf("Walk from %s (%s) to %s (%s)%n",
                entry.departure().name(),
                formatTime(entry.departureTime()),
                entry.arrival().name(),
                formatTime(entry.arrivalTime()));
    }

    /**
     * Extrait le nom de l'agence depuis l'ID du trip
     * Format: AGENCYA-123456789
     */
    private String extractAgency(String tripId) {
        int dashIndex = tripId.indexOf('-');
        if (dashIndex > 0) {
            return tripId.substring(0, dashIndex);
        }
        return "UNKNOWN";
    }

    /**
     * Formate les secondes en HH:MM:SS
     */
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
