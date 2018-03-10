package com.maxmind.db.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    public static Continent of(JsonElement jsonElement) {
        JsonObject continentJson = jsonElement.getAsJsonObject();

        int geoNameId = continentJson.getAsJsonPrimitive("geoname_id").getAsInt();
        String code = continentJson.getAsJsonPrimitive("code").getAsString();
        String continentName = continentJson.getAsJsonObject("names").get("en").getAsString();
        return new Continent(continentName, geoNameId, code);
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
