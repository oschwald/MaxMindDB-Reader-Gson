package com.maxmind.db.model;

/**
 * <p>
 * Contains data for the continent record associated with an IP address.
 * </p>
 * <p>
 * This record is returned by all the end points.
 * </p>
 * <p>
 * Do not use any of the continent names as a database or map key. Use the
 * value returned by {@link #getGeoNameId} or {@link #getCode} instead.
 * </p>
 */
public class Continent extends AbstractRecord {

    private final String code;

    public Continent(String name, Integer geoNameId, String code) {
        super(name, geoNameId);

        this.code = code;
    }

    /**
     * @return A two character continent code like "NA" (North America) or "OC"
     * (Oceania). This attribute is returned by all end points.
     */
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Continent{" +
                "code='" + code + '\'' +
                "} " + super.toString();
    }
}
