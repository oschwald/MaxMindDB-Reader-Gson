package com.maxmind.db;

import com.google.gson.JsonElement;
import com.maxmind.db.model.CountryResponse;

import java.io.IOException;
import java.net.InetAddress;

public interface GeoIp2Provider {

    /**
     * Looks up the {@code address} in the MaxMind DB.
     *
     * @param address the IP address to look up.
     * @return the record for the IP address.
     * @throws IOException if a file I/O error occurs.
     */
    JsonElement get(InetAddress address) throws IOException;

    /**
     * @param address IPv4 or IPv6 address to lookup.
     * @return A Country model for the requested IP address.
     * @throws IOException     if there is an IO error
     */
    CountryResponse getCountry(InetAddress address) throws IOException;

    /**
     * @return the metadata for the MaxMind DB file.
     */
    Metadata getMetadata();
}
