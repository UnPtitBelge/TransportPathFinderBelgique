package com.ulb.util;

public record Position(double lat, double lon) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Distance à vol d'oiseau (formule de haversine), en kilomètres. */
    public double distanceKm(Position other) {
        double dLat = Math.toRadians(other.lat - this.lat);
        double dLon = Math.toRadians(other.lon - this.lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.lat))
                        * Math.cos(Math.toRadians(other.lat))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
