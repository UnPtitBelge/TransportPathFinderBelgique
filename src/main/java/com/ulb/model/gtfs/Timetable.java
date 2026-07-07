package com.ulb.model.gtfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vue en lecture seule des données GTFS nécessaires au calcul d'itinéraire :
 * arrêts, trajets, horaires et correspondances à pied. Les listes de
 * {@link StopTime} par trajet sont triées par {@code stop_sequence}, ce qui
 * permet de résoudre l'arrêt suivant d'un trajet sans dépendre de l'ordre des
 * lignes dans le fichier GTFS source.
 */
public class Timetable {
    private final Map<String, Stop> stops;
    private final Map<String, Trip> trips;
    private final Map<String, List<StopTime>> tripStopTimes;
    private final Map<String, List<StopTime>> stopStopTimes;
    private final Map<String, List<Transfer>> transfers;

    public Timetable(Map<String, Stop> stops, Map<String, Trip> trips,
            Map<String, List<StopTime>> tripStopTimes, Map<String, List<StopTime>> stopStopTimes,
            Map<String, List<Transfer>> transfers) {
        this.stops = stops;
        this.trips = trips;
        this.tripStopTimes = tripStopTimes;
        this.stopStopTimes = stopStopTimes;
        this.transfers = transfers;
    }

    public Trip trip(String tripId) {
        return trips.get(tripId);
    }

    public Stop stop(String stopId) {
        return stops.get(stopId);
    }

    /** Tous les passages (tous trajets confondus) à un arrêt donné. */
    public List<StopTime> stopTimesAt(String stopId) {
        return stopStopTimes.getOrDefault(stopId, Collections.emptyList());
    }

    /** Correspondances à pied vers les arrêts proches d'un arrêt donné. */
    public List<Transfer> transfersAt(String stopId) {
        return transfers.getOrDefault(stopId, Collections.emptyList());
    }

    /** Arrêts dont le nom contient {@code query} (insensible à la casse). */
    public List<Stop> findStopsByName(String query) {
        String needle = query.toLowerCase();
        List<Stop> found = new ArrayList<>();
        for (Stop stop : stops.values()) {
            if (stop.name().toLowerCase().contains(needle)) {
                found.add(stop);
            }
        }
        return found;
    }

    /**
     * Arrêt suivant desservi par le même trajet que {@code current}, s'il existe
     * (absent si {@code current} est le dernier arrêt du trajet).
     */
    public Optional<StopTime> nextStopTime(StopTime current) {
        List<StopTime> schedule = tripStopTimes.get(current.tripId());
        int position = Collections.binarySearch(schedule, current, Comparator.comparingInt(StopTime::index));
        if (position < 0 || position + 1 >= schedule.size()) {
            return Optional.empty();
        }
        return Optional.of(schedule.get(position + 1));
    }
}
