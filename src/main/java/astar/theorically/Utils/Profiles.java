package astar.theorically.Utils;

import astar.theorically.Models.Trip;
import java.util.HashMap;
import java.util.Map;

public class Profiles {

    private static String currentPenaltyFunction = "NONE";

    private static double walkSpeed = 1.0; // m/s
    private static final Map<String, Double> radiusByOperator = Map.of(
        "DELIJN",
        500.0,
        "SNCB",
        500.0,
        "STIB",
        500.0,
        "TEC",
        500.0
    );

    public static double getWalkSpeed() {
        return walkSpeed;
    }

    public static void setWalkSpeed(double walkSpeed) {
        Profiles.walkSpeed = walkSpeed;
    }

    public static double getRadiusByOperator(String operator) {
        return radiusByOperator.getOrDefault(operator, 5000.0);
    }

    public static void setRadiusByOperator(String operator, double radius) {
        radiusByOperator.replace(operator, radius);
    }

    public static String getOperatorFromStopId(String stopId) {
        if (stopId.startsWith("STIB")) return "STIB";
        if (stopId.startsWith("TEC")) return "TEC";
        if (stopId.startsWith("DELIJN")) return "DELIJN";
        if (stopId.startsWith("SNCB")) return "SNCB";
        return "UNKNOWN";
    }

    // Configuration maps
    private static final Map<String, Map<String, Double>> transportMalus =
        new HashMap<>();

    static {
        // Profile: NONE (no penalties)
        transportMalus.put("NONE", defaultMalusMap(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0));

        // Profile: COMFORTABLE (favor TRAIN/METRO, penalize BUS/TRAM)
        transportMalus.put("COMFORTABLE", defaultMalusMap(2.0, 1.5, 1.0, 0.8, 5.0, 1.0, 5.0));

        // Profile: WALKING_FRIENDLY (favor WALK, penalize others)
        transportMalus.put("WALKING_FRIENDLY", defaultMalusMap(1.2, 1.2, 1.2, 1.2, 0.2, 1.5, 1.0));

        // Profile: FAST (favor TRAIN/METRO, penalize WALK)
        transportMalus.put("FAST", defaultMalusMap(0.9, 0.9, 0.8, 0.7, 1.2, 1.5, 1.0));
    }

    // Order: BUS, TRAM, METRO, TRAIN, WALK, WAIT
    private static Map<String, Double> defaultMalusMap(
            double bus,
            double tram,
            double metro,
            double train,
            double walk,
            double wait,
            double transfers
    ) {
        Map<String, Double> malus = new HashMap<>();
        malus.put("BUS", bus);
        malus.put("TRAM", tram);
        malus.put("METRO", metro);
        malus.put("TRAIN", train);
        malus.put("WALK", walk);
        malus.put("WAIT", wait);
        malus.put("TRANSFERS", transfers);
        return malus;
    }

    public static void addProfile(String name, double[] malus) {
        transportMalus.put(name, defaultMalusMap(malus[0], malus[1], malus[2], malus[3], malus[4],malus[5], malus[6]));
    }

    public static void setProfile(String name) {
        if (transportMalus.containsKey(name)) {
            currentPenaltyFunction = name;
        }
    }

    public static String[] getAvailableProfiles() {
        return transportMalus.keySet().toArray(new String[0]);
    }

    public static String getCurrentProfile() {
        return currentPenaltyFunction;
    }

    public static int costModifier(Trip currentTrip, int travelTime, int waitTime, boolean isTransfer) {
        Map<String, Double> malusMap = transportMalus.getOrDefault(currentPenaltyFunction, Map.of());
        double transportPenalty = malusMap.getOrDefault(currentTrip.route().transportType(), 1.0);
        double waitPenalty = malusMap.getOrDefault("WAIT", 1.0);
        double transferPenalty = malusMap.getOrDefault("TRANSFERS", 1.0);
        double penalty = travelTime * transportPenalty + waitTime * waitPenalty;
        return isTransfer ? (int) (penalty * transferPenalty) : (int) penalty;
    }
}
