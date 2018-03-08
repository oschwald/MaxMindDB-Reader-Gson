package com.maxmind.db.cache;

import com.google.gson.JsonElement;

import java.io.IOException;

/**
 * A no-op cache singleton.
 */
public class NoCache implements NodeCache {

    private static final NoCache INSTANCE = new NoCache();

    private NoCache() {
    }

    @Override
    public JsonElement get(int key, Loader loader) throws IOException {
        return loader.load(key);
    }

    public static NoCache getInstance() {
        return INSTANCE;
    }

}
