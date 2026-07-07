package com.ulb.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ulb.model.gtfs.Stop;
import com.ulb.model.gtfs.Transfer;
import com.ulb.util.Profiler;

/**
 * Construit les correspondances à pied entre arrêts physiquement proches
 * (ex: un arrêt de bus et une gare au même carrefour, mais avec des stop_id
 * différents car issus d'agences GTFS distinctes).
 *
 * Les arrêts sont indexés dans une grille dont la taille de cellule vaut le
 * rayon de correspondance maximal : toute paire d'arrêts à moins de ce rayon
 * se trouve donc forcément dans la même cellule ou une cellule adjacente
 * (voisinage 3x3), ce qui évite une comparaison quadratique sur l'ensemble
 * du réseau (67 000+ arrêts).
 */
public final class WalkingTransferBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WalkingTransferBuilder.class);

    private static final double MAX_WALK_DISTANCE_METERS = 500.0;
    private static final double WALKING_SPEED_KMH = 5.0;
    private static final double METERS_PER_DEGREE_LAT = 111_320.0;
    private static final double BELGIUM_AVERAGE_LATITUDE_DEG = 50.5;

    private final double cellSizeLat;
    private final double cellSizeLon;

    private WalkingTransferBuilder() {
        double lonScale = Math.cos(Math.toRadians(BELGIUM_AVERAGE_LATITUDE_DEG));
        this.cellSizeLat = MAX_WALK_DISTANCE_METERS / METERS_PER_DEGREE_LAT;
        this.cellSizeLon = MAX_WALK_DISTANCE_METERS / (METERS_PER_DEGREE_LAT * lonScale);
    }

    public static Map<String, List<Transfer>> build(Map<String, Stop> stops) {
        return new WalkingTransferBuilder().buildFor(stops);
    }

    private Map<String, List<Transfer>> buildFor(Map<String, Stop> stops) {
        try (Profiler p = Profiler.start("buildWalkingTransfers")) {
            Map<Long, List<Stop>> grid = new HashMap<>();
            for (Stop stop : stops.values()) {
                grid.computeIfAbsent(cellOf(stop), k -> new ArrayList<>()).add(stop);
            }

            Map<String, List<Transfer>> transfers = new HashMap<>();
            int created = 0;
            for (Stop stop : stops.values()) {
                for (Stop nearby : candidatesAround(stop, grid)) {
                    if (nearby.id().equals(stop.id())) {
                        continue;
                    }

                    double distanceKm = stop.position().distanceKm(nearby.position());
                    if (distanceKm * 1000.0 > MAX_WALK_DISTANCE_METERS) {
                        continue;
                    }

                    int walkSeconds = (int) Math.round(distanceKm / WALKING_SPEED_KMH * 3600.0);
                    transfers.computeIfAbsent(stop.id(), k -> new ArrayList<>())
                            .add(new Transfer(stop.id(), nearby.id(), walkSeconds));
                    created++;
                }
            }

            logger.info("Correspondances à pied générées: {} (rayon {} m, {} arrêts concernés)",
                    created, (int) MAX_WALK_DISTANCE_METERS, transfers.size());
            return transfers;
        }
    }

    private List<Stop> candidatesAround(Stop stop, Map<Long, List<Stop>> grid) {
        long cx = cellX(stop);
        long cy = cellY(stop);

        List<Stop> candidates = new ArrayList<>();
        for (long dx = -1; dx <= 1; dx++) {
            for (long dy = -1; dy <= 1; dy++) {
                List<Stop> cell = grid.get(pack(cx + dx, cy + dy));
                if (cell != null) {
                    candidates.addAll(cell);
                }
            }
        }
        return candidates;
    }

    private long cellOf(Stop stop) {
        return pack(cellX(stop), cellY(stop));
    }

    private long cellX(Stop stop) {
        return (long) Math.floor(stop.position().lat() / this.cellSizeLat);
    }

    private long cellY(Stop stop) {
        return (long) Math.floor(stop.position().lon() / this.cellSizeLon);
    }

    private static long pack(long cx, long cy) {
        return (cx << 32) ^ (cy & 0xFFFFFFFFL);
    }
}
