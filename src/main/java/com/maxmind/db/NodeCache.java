package com.maxmind.db;

import com.google.gson.JsonElement;

import java.io.IOException;

public interface NodeCache {

    public interface Loader {
        JsonElement load(int key) throws IOException;
    }

    public JsonElement get(int key, Loader loader) throws IOException;

}
