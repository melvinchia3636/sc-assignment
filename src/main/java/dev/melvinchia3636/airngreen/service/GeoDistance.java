package dev.melvinchia3636.airngreen.service;

/** Great-circle distance helper shared by parks and station ordering. */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final double HALF = 0.5;

    private GeoDistance() {
    }

    /**
     * Great-circle distance between two points.
     *
     * @param lat1 first latitude in degrees
     * @param lon1 first longitude in degrees
     * @param lat2 second latitude in degrees
     * @param lon2 second longitude in degrees
     * @return distance in kilometres
     */
    public static double kmBetween(
        double lat1,
        double lon1,
        double lat2,
        double lon2
    ) {
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(deltaLat * HALF), 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(deltaLon * HALF), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }
}
