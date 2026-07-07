package com.ulb.astar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ulb.model.gtfs.Stop;
import com.ulb.model.gtfs.StopTime;
import com.ulb.model.gtfs.Timetable;
import com.ulb.model.gtfs.Transfer;
import com.ulb.model.gtfs.Trip;
import com.ulb.util.Position;
import com.ulb.util.Profiler;
import com.ulb.util.Utils;

public class Astar {
    private static final Logger logger = LoggerFactory.getLogger(Astar.class);

    private final Timetable timetable;

    public Astar(Timetable timetable) {
        this.timetable = timetable;
    }

    public Optional<Solution> run(String departureName, String arrivalName, int time) {
        try (Profiler p = Profiler.start("astar.run[" + departureName + " -> " + arrivalName + "]")) {
            return this.search(departureName, arrivalName, time);
        }
    }

    private Optional<Solution> search(String departureName, String arrivalName, int startTime) {
        List<Stop> departureStops = this.timetable.findStopsByName(departureName);
        List<Stop> arrivalStops = this.timetable.findStopsByName(arrivalName);

        if (departureStops.isEmpty()) {
            logger.warn("Aucun arrêt de départ ne correspond à '{}'", departureName);
            return Optional.empty();
        }
        if (arrivalStops.isEmpty()) {
            logger.warn("Aucun arrêt d'arrivée ne correspond à '{}'", arrivalName);
            return Optional.empty();
        }

        logger.debug("Arrêts de départ candidats: {}, arrêts d'arrivée candidats: {}",
                departureStops.size(), arrivalStops.size());

        Set<String> arrivalStopIds = new HashSet<>();
        for (Stop stop : arrivalStops) {
            arrivalStopIds.add(stop.id());
        }
        Position arrivalPosition = arrivalStops.get(0).position();

        Frontier frontier = new Frontier();
        for (Stop stop : departureStops) {
            StopNode start = new StopNode(stop, null, null, 0, startTime, startTime);
            frontier.offer(stop.id(), start, AStarHeuristic.estimate(stop.position(), arrivalPosition));
        }

        long expanded = 0;

        while (frontier.hasNext()) {
            StopNode node = frontier.pollBest();

            if (arrivalStopIds.contains(node.stop().id())) {
                logger.info("Solution trouvée après exploration de {} noeuds (file restante: {})", expanded,
                        frontier.size());
                return Optional.of(Solution.fromNode(node));
            }

            frontier.close(node.stop().id());
            expanded++;

            if (expanded % 10_000 == 0) {
                logger.debug("{} noeuds explorés, file: {}, mémoire utilisée: {} MB", expanded, frontier.size(),
                        Profiler.usedMemoryBytes() / (1024 * 1024));
            }

            for (StopTime stopTime : this.timetable.stopTimesAt(node.stop().id())) {
                Optional<StopTime> nextStopTime = this.timetable.nextStopTime(stopTime);
                if (nextStopTime.isEmpty()) {
                    continue;
                }

                Trip trip = this.timetable.trip(stopTime.tripId());
                Stop nextStop = this.timetable.stop(nextStopTime.get().stopId());

                if (frontier.isClosed(nextStop.id())) {
                    continue;
                }

                // Temps d'attente et de trajet calculés à partir de l'heure réelle
                // d'arrivée à ce noeud (et non de l'heure de départ initiale) afin que
                // les correspondances s'enchaînent correctement.
                int waitingTime = Utils.calculateWaitingTime(node.arrivalTime(), stopTime.departure());
                int inVehicleTime = nextStopTime.get().departure() - stopTime.departure();
                int departureActual = node.arrivalTime() + waitingTime;
                int arrivalTime = departureActual + inVehicleTime;

                double cost = node.cost() + waitingTime + inVehicleTime;
                StopNode nextNode = new StopNode(nextStop, node, trip, cost, departureActual, arrivalTime);

                double priority = cost + AStarHeuristic.estimate(nextStop.position(), arrivalPosition);
                frontier.offer(nextStop.id(), nextNode, priority);
            }

            for (Transfer transfer : this.timetable.transfersAt(node.stop().id())) {
                Stop nextStop = this.timetable.stop(transfer.toStopId());

                if (frontier.isClosed(nextStop.id())) {
                    continue;
                }

                // Marche à pied : pas d'attente, on part dès l'arrivée au noeud courant.
                int departureActual = node.arrivalTime();
                int arrivalTime = departureActual + transfer.walkSeconds();
                double cost = node.cost() + transfer.walkSeconds();

                StopNode nextNode = new StopNode(nextStop, node, null, cost, departureActual, arrivalTime);

                double priority = cost + AStarHeuristic.estimate(nextStop.position(), arrivalPosition);
                frontier.offer(nextStop.id(), nextNode, priority);
            }
        }

        logger.warn("Aucune solution trouvée après exploration de {} noeuds", expanded);
        return Optional.empty();
    }

    /**
     * File de priorité "ouverte" doublée d'un index par arrêt : au plus un
     * candidat par arrêt y est présent à la fois, ce qui évite d'accumuler des
     * entrées obsolètes en mémoire et remplace la logique de decrease-key.
     */
    private static final class Frontier {
        private final PriorityQueue<PriorityNode> queue = new PriorityQueue<>();
        private final Map<String, PriorityNode> bestQueued = new HashMap<>();
        private final Set<String> closed = new HashSet<>();

        void offer(String stopId, StopNode node, double priority) {
            PriorityNode existing = this.bestQueued.get(stopId);
            if (existing != null) {
                if (priority >= existing.priority()) {
                    return;
                }
                this.queue.remove(existing);
            }

            PriorityNode prioNode = new PriorityNode(node, priority);
            this.queue.add(prioNode);
            this.bestQueued.put(stopId, prioNode);
        }

        boolean hasNext() {
            return !this.queue.isEmpty();
        }

        StopNode pollBest() {
            PriorityNode best = this.queue.poll();
            this.bestQueued.remove(best.node().stop().id());
            return best.node();
        }

        boolean isClosed(String stopId) {
            return this.closed.contains(stopId);
        }

        void close(String stopId) {
            this.closed.add(stopId);
        }

        int size() {
            return this.queue.size();
        }
    }
}
