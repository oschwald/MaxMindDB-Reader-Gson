package com.maxmind.db;

import com.google.gson.JsonObject;
import com.maxmind.db.Reader.FileMode;
import com.maxmind.db.cache.NoCache;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointerTest {
    @SuppressWarnings("static-method")
    @Test
    public void testWithPointers() throws IOException {
        File file = ReaderTest.getFile("maps-with-pointers.raw");
        BufferHolder ptf = new BufferHolder(file, FileMode.MEMORY);
        Decoder decoder = new Decoder(NoCache.getInstance(), ptf.get(), 0);

        JsonObject map = new JsonObject();
        map.addProperty("long_key", "long_value1");
        assertEquals(map, decoder.decode(0));

        map = new JsonObject();
        map.addProperty("long_key", "long_value2");
        assertEquals(map, decoder.decode(22));

        map = new JsonObject();
        map.addProperty("long_key2", "long_value1");
        assertEquals(map, decoder.decode(37));

        map = new JsonObject();
        map.addProperty("long_key2", "long_value2");
        assertEquals(map, decoder.decode(50));

        map = new JsonObject();
        map.addProperty("long_key", "long_value1");
        assertEquals(map, decoder.decode(55));

        map = new JsonObject();
        map.addProperty("long_key2", "long_value2");
        assertEquals(map, decoder.decode(57));
    }
}
