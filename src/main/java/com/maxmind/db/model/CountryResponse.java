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

        JsonObject countryJson = response.getAsJsonObject("country");
        int countryGeoName = countryJson.getAsJsonPrimitive("geoname_id").getAsInt();
        String isoCode = countryJson.getAsJsonPrimitive("iso_code").getAsString();
        String countryName = countryJson.getAsJsonObject("names").get("en").getAsString();
        Country country = new Country(isoCode, countryGeoName, countryName);

        JsonObject contigentJson = response.getAsJsonObject("continent");
        int geoNameId = contigentJson.getAsJsonPrimitive("geoname_id").getAsInt();
        String code = contigentJson.getAsJsonPrimitive("code").getAsString();
        String continentName = countryJson.getAsJsonObject("names").get("en").getAsString();
        Continent continent = new Continent(continentName, geoNameId, code);

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
