package com.maxmind.db.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class provides a model for the data returned by the GeoIP2 Precision:
 * Country end point.
 */
public class CountryResponse {

    private final Country country;
    private final Continent continent;

    public CountryResponse(Country country, Continent continent) {
        this.country = country;
        this.continent = continent;
    }

    public static CountryResponse of(JsonElement jsonElement) {
        JsonObject response = jsonElement.getAsJsonObject();

        Country country = Country.of(response.get("country"));
        Continent continent = Continent.of(response.get("continent"));
        return new CountryResponse(country, continent);
    }

    /**
     * @return Country record for the requested IP address. This object represents the country where MaxMind believes
     * the end user is located.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @return Continent record for the requested IP address.
     */
    public Continent getContinent() {
        return continent;
    }
}
