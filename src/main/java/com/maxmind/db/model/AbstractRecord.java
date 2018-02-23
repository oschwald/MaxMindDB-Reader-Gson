package com.maxmind.db.model;

/**
 * Abstract class for records with name maps.
 */
public abstract class AbstractRecord {

    private final String name;
    private final Integer geoNameId;

    protected AbstractRecord(String name, Integer geoNameId) {
        this.name = name;
        this.geoNameId = geoNameId;
    }

    /**
     * @return The name of this record (i.e. country name, city name). This attribute is returned by all end points.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The GeoName ID for this record. This attribute is returned by all end points.
     */
    public Integer getGeoNameId() {
        return geoNameId;
    }

    @Override
    public String toString() {
        return "AbstractRecord{" +
                "name='" + name + '\'' +
                ", geoNameId=" + geoNameId +
                '}';
    }
}
