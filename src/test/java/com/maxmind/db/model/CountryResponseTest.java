package com.maxmind.db.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class CountryResponseTest {

    @Test
    public void testCountryResponse() throws Exception {
        JsonElement json = readFile("/country-response.json");
        CountryResponse countryResponse = CountryResponse.of(json);

        //check country parsing
        Country country = countryResponse.getCountry();
        assertThat(country.getIsoCode(), is("US"));
        assertThat(country.getName(), is("United States"));
        assertThat(country.getGeoNameId(), is(6252001));

        //check continent parsing
        Continent continent = countryResponse.getContinent();
        assertThat(continent.getCode(), is("NA"));
        assertThat(continent.getGeoNameId(), is(6255149));
        assertThat(continent.getName(), is("North America"));
    }

    @Test
    public void testCountry() throws Exception {
        JsonElement json = readFile("/country.json");
        Country country = Country.of(json);

        //check country parsing
        assertThat(country.getIsoCode(), is("SE"));
        assertThat(country.getName(), is("Sweden"));
        assertThat(country.getGeoNameId(), is(2661886));
    }

    @Test
    public void testContinent() throws Exception {
        JsonElement json = readFile("/continent.json");
        Continent continent = Continent.of(json);

        //check continent parsing
        assertThat(continent.getCode(), is("EU"));
        assertThat(continent.getGeoNameId(), is(6255148));
        assertThat(continent.getName(), is("Europe"));
    }

    private JsonElement readFile(String s) throws URISyntaxException, IOException {
        Path stevePath = Paths.get(getClass().getResource(s).toURI());
        BufferedReader reader = Files.newBufferedReader(stevePath, StandardCharsets.UTF_8);

        //Read file
        return new Gson().fromJson(reader, JsonElement.class);
    }
}
