package de.uni_marburg.schematch.utils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class GeoLocation {

    private double latitude;
    private double longitude;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoLocation(String coords) {
        try {
            String[] splits = coords.split(",");
            this.latitude = Double.parseDouble(splits[0]);
            this.longitude = Double.parseDouble(splits[1]);
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
        }
    }

    /**
     * Calculates the distance between two points on earth measured as walked across the globe
     *
     * @param target the target to calculate the distance to
     * @return the distance between two points
     */
    public double calculateDistance(GeoLocation target) {
        double phi_1 = toRad(this.latitude);
        double phi_2 = toRad(target.latitude);
        double lambda_1 = toRad(this.longitude);
        double lambda_2 = toRad(target.longitude);
        final double r = 6371; // Earths radius
        double h = hav(phi_2 - phi_1) + (1 - hav(phi_1 - phi_2) - hav(phi_1 + phi_2)) * hav(lambda_2 - lambda_1);
        if (h > 1) h = 1; // this is to avoid floating point error
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    private double hav(double angle) {
        return (1 - Math.cos(angle)) / 2;
    }

    private double toRad(double angle) {
        return angle * Math.PI / 180;
    }

}
