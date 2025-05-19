package Utils;

import Models.Stop;

public class Heuristic {

    private static double speed = 18.5; // m/s
    private static boolean dijkstraMode = false;

    public static double getSpeed() {
        return speed;
    }

    public static void setSpeed(double speed) {
        Heuristic.speed = speed;
    }

    public static void setDijkstraMode(boolean dijkstraMode) {
        Heuristic.dijkstraMode = dijkstraMode;
    }

    public static double heuristic(Stop stop1, Stop stop2) {
        if (dijkstraMode) return 0;
        //        double speed = 4.63*Math.pow(Helper.distance(stop1, stop2),0.39);
        return Helper.distance(stop1, stop2) / speed;
    }

    public static double consistentHeuristic(Stop n, Stop p, Stop d, int cost) {
        double hN = heuristic(n, d);
        double hP = heuristic(p, d);
        if (hN > cost + hP) {
            return -1;
        }
        return hN;
    }
}
