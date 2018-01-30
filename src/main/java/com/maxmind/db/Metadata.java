package com.maxmind.db;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class Metadata {
    private final int binaryFormatMajorVersion;
    private final int binaryFormatMinorVersion;

    private final long buildEpoch;

    private final String databaseType;

    private final JsonElement description;

    private final int ipVersion;

    private final JsonElement languages;

    private final int nodeByteSize;

    private final int nodeCount;

    private final int recordSize;

    private final int searchTreeSize;

    Metadata(JsonObject metadata) {
        this.binaryFormatMajorVersion = metadata.get(
                "binary_format_major_version").getAsInt();
        this.binaryFormatMinorVersion = metadata.get(
                "binary_format_minor_version").getAsInt();
        this.buildEpoch = metadata.get("build_epoch").getAsLong();
        this.databaseType = metadata.get("database_type").getAsString();
        this.languages = metadata.get("languages");
        this.description = metadata.get("description");
        this.ipVersion = metadata.get("ip_version").getAsInt();
        this.nodeCount = metadata.get("node_count").getAsInt();
        this.recordSize = metadata.get("record_size").getAsInt();
        this.nodeByteSize = this.recordSize / 4;
        this.searchTreeSize = this.nodeCount * this.nodeByteSize;
    }

    /**
     * @return the major version number for the database's binary format.
     */
    public int getBinaryFormatMajorVersion() {
        return this.binaryFormatMajorVersion;
    }

    /**
     * @return the minor version number for the database's binary format.
     */
    public int getBinaryFormatMinorVersion() {
        return this.binaryFormatMinorVersion;
    }

    /**
     * @return the date of the database build.
     */
    public Date getBuildDate() {
        return new Date(this.buildEpoch * 1000);
    }

    /**
     * @return a string that indicates the structure of each data record
     * associated with an IP address. The actual definition of these
     * structures is left up to the database creator.
     */
    public String getDatabaseType() {
        return this.databaseType;
    }

    /**
     * @return map from language code to description in that language.
     */
    public Map<String, String> getDescription() {
        return new Gson().fromJson(this.description, new TypeToken<Map<String, String>>(){}.getType());
    }

    /**
     * @return whether the database contains IPv4 or IPv6 address data. The only
     * possible values are 4 and 6.
     */
    public int getIpVersion() {
        return this.ipVersion;
    }

    /**
     * @return list of languages supported by the database.
     */
    public List<String> getLanguages() {
        return new Gson().fromJson(this.languages, new TypeToken<List<String>>(){}.getType());
    }

    /**
     * @return the nodeByteSize
     */
    int getNodeByteSize() {
        return this.nodeByteSize;
    }

    /**
     * @return the number of nodes in the search tree.
     */
    int getNodeCount() {
        return this.nodeCount;
    }

    /**
     * @return the number of bits in a record in the search tree. Note that each
     * node consists of two records.
     */
    int getRecordSize() {
        return this.recordSize;
    }

    /**
     * @return the searchTreeSize
     */
    int getSearchTreeSize() {
        return this.searchTreeSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Metadata [binaryFormatMajorVersion="
                + this.binaryFormatMajorVersion + ", binaryFormatMinorVersion="
                + this.binaryFormatMinorVersion + ", buildEpoch="
                + this.buildEpoch + ", databaseType=" + this.databaseType
                + ", description=" + this.description + ", ipVersion="
                + this.ipVersion + ", nodeCount=" + this.nodeCount
                + ", recordSize=" + this.recordSize + ']';
    }
}
