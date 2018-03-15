package com.maxmind.db.cache;

import com.google.gson.JsonElement;

import java.io.IOException;

public interface NodeCache {

    interface Loader {
        JsonElement load(int key) throws IOException;
    }

    JsonElement get(int key, Loader loader) throws IOException;

}
