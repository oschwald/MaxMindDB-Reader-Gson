package com.maxmind.db.model;

import java.util.Objects;

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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof AbstractRecord) {
            AbstractRecord that = (AbstractRecord) other;
            return Objects.equals(geoNameId, that.geoNameId);
        }

        return false;
    }

    @Override
    public int hashCode() {
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
