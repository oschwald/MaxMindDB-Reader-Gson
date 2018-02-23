package com.maxmind.db.model;

/**
 * <p>
 * Contains data for the country record associated with an IP address.
 * </p>
 * <p>
 * This record is returned by all the end points.
 * </p>
 * <p>
 * Do not use any of the country names as a database or map key. Use the value
 * returned by {@link #getGeoNameId} or {@link #getIsoCode} instead.
 * </p>
 */
public class Country extends AbstractRecord {

    private final String isoCode;
    private final Integer geoNameId;

    public Country(String isoCode, Integer geoNameId, String name) {
        super(name, geoNameId);

        this.isoCode = isoCode;
        this.geoNameId = geoNameId;
    }

    /**
     * @return The <a href="http://en.wikipedia.org/wiki/ISO_3166-1">two-character ISO 3166-1 alpha code</a> for the
     * country. This attribute is returned by all end points.
     */
    public String getIsoCode() {
        return isoCode;
    }

    @Override
    public String toString() {
        return "Country{" +
                "isoCode='" + isoCode + '\'' +
                ", geoNameId=" + geoNameId +
                "} " + super.toString();
    }
}
