package com.ulb.parser;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ulb.model.Agency;
import com.ulb.model.Transport;
import com.ulb.model.gtfs.Road;
import com.ulb.model.gtfs.Stop;
import com.ulb.model.gtfs.StopTime;
import com.ulb.model.gtfs.Timetable;
import com.ulb.model.gtfs.Transfer;
import com.ulb.model.gtfs.Trip;
import com.ulb.util.Position;
import com.ulb.util.Profiler;
import com.ulb.util.Utils;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final Map<String, Road> roads = new HashMap<>();
    private final Map<String, Stop> stops = new HashMap<>();
    private final Map<String, List<StopTime>> tripStopTimes = new HashMap<>();
    private final Map<String, List<StopTime>> stopStopTimes = new HashMap<>();
    private final Map<String, Trip> trips = new HashMap<>();

    public void parseAll() {
        try (Profiler p = Profiler.start("parseAll")) {
            parseRoads();
            parseStops();
            parseTrips();
            parseStopTimes();
        }
    }

    public void parseRoads() {
        try (Profiler p = Profiler.start("parseRoads")) {
            for (Agency agency : Agency.values()) {
                int countBefore = roads.size();
                try (CSVParser parser = GTFSLoader.open(agency.name() + "/routes.csv")) {
                    for (CSVRecord record : parser) {
                        String id = record.get("route_id");
                        String number = record.get("route_short_name");
                        String name = record.get("route_long_name");
                        Transport type = Transport.valueOf(record.get("route_type"));

                        roads.put(id, new Road(id, number, name, type));
                    }
                } catch (IOException ioe) {
                    throw new IOError(ioe);
                }
                logger.debug("{}: {} routes chargées", agency, roads.size() - countBefore);
            }
            logger.info("Total routes chargées: {}", roads.size());
        }
    }

    public void parseStops() {
        try (Profiler p = Profiler.start("parseStops")) {
            for (Agency agency : Agency.values()) {
                int countBefore = stops.size();
                try (CSVParser parser = GTFSLoader.open(agency.name() + "/stops.csv")) {
                    for (CSVRecord record : parser) {
                        String id = record.get("stop_id");
                        String name = record.get("stop_name");
                        double lat = Double.parseDouble(record.get("stop_lat"));
                        double lon = Double.parseDouble(record.get("stop_lon"));

                        stops.put(id, new Stop(id, name, new Position(lat, lon)));
                    }
                } catch (IOException ioe) {
                    throw new IOError(ioe);
                }
                logger.debug("{}: {} arrêts chargés", agency, stops.size() - countBefore);
            }
            logger.info("Total arrêts chargés: {}", stops.size());
        }
    }

    public void parseTrips() {
        try (Profiler p = Profiler.start("parseTrips")) {
            for (Agency agency : Agency.values()) {
                int countBefore = trips.size();
                try (CSVParser parser = GTFSLoader.open(agency.name() + "/trips.csv")) {
                    for (CSVRecord record : parser) {
                        String tripId = record.get("trip_id");
                        String routeId = record.get("route_id");

                        trips.put(tripId, new Trip(tripId, routeId));
                    }
                } catch (IOException ioe) {
                    throw new IOError(ioe);
                }
                logger.debug("{}: {} trajets chargés", agency, trips.size() - countBefore);
            }
            logger.info("Total trajets chargés: {}", trips.size());
        }
    }

    public void parseStopTimes() {
        try (Profiler p = Profiler.start("parseStopTimes")) {
            int totalCount = 0;
            for (Agency agency : Agency.values()) {
                int countBefore = totalCount;
                try (CSVParser parser = GTFSLoader.open(agency.name() + "/stop_times.csv")) {
                    for (CSVRecord record : parser) {
                        String tripId = record.get("trip_id");
                        String departureTimeStr = record.get("departure_time");
                        String stopId = record.get("stop_id");
                        int stopSequence = Integer.parseInt(record.get("stop_sequence"));

                        int departure = Utils.timeToSeconds(departureTimeStr);

                        StopTime stopTime = new StopTime(tripId, departure, stopId, stopSequence);
                        tripStopTimes.computeIfAbsent(tripId, k -> new ArrayList<>()).add(stopTime);
                        stopStopTimes.computeIfAbsent(stopId, k -> new ArrayList<>()).add(stopTime);
                        totalCount++;
                    }
                } catch (IOException ioe) {
                    throw new IOError(ioe);
                }
                logger.debug("{}: {} horaires chargés", agency, totalCount - countBefore);
            }

            // Le fichier GTFS n'est pas toujours trié par stop_sequence (ex: DELIJN) :
            // on trie chaque horaire de trajet une bonne fois pour toutes ici, ce qui
            // permet ensuite de résoudre l'arrêt suivant d'un trajet de façon fiable.
            tripStopTimes.values().forEach(schedule -> schedule.sort(Comparator.comparingInt(StopTime::index)));

            logger.info("Total horaires chargés: {}", totalCount);
        }
    }

    public Map<String, Road> getRoads() {
        return Collections.unmodifiableMap(this.roads);
    }

    public Map<String, Stop> getStops() {
        return Collections.unmodifiableMap(this.stops);
    }

    public Map<String, Trip> getTrips() {
        return Collections.unmodifiableMap(this.trips);
    }

    public Map<String, List<StopTime>> getTripStopTimes() {
        return Collections.unmodifiableMap(this.tripStopTimes);
    }

    public Map<String, List<StopTime>> getStopStopTimes() {
        return Collections.unmodifiableMap(this.stopStopTimes);
    }

    public Timetable getTimetable() {
        Map<String, List<Transfer>> transfers = WalkingTransferBuilder.build(getStops());
        return new Timetable(getStops(), getTrips(), getTripStopTimes(), getStopStopTimes(), transfers);
    }
}
