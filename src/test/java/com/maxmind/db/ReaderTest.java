package com.maxmind.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReaderTest {

    private Reader testReader;

    @Before
    public void setupReader() {
        this.testReader = null;
    }

    @After
    public void teardownReader() throws IOException {
        if (this.testReader != null) {
            this.testReader.close();
        }
    }

    @Test
    public void test() throws IOException {
        for (long recordSize : new long[]{24, 28, 32}) {
            for (int ipVersion : new int[]{4, 6}) {
                File file = getFile("MaxMind-DB-test-ipv" + ipVersion + "-" + recordSize + ".mmdb");
                try (Reader reader = new Reader(file)) {
                    this.testMetadata(reader, ipVersion, recordSize);
                    if (ipVersion == 4) {
                        this.testIpV4(reader, file);
                    } else {
                        this.testIpV6(reader, file);
                    }
                }
            }
        }
    }

    static class GetRecordTest {
        InetAddress ip;
        File db;
        String network;
        boolean hasRecord;

        GetRecordTest(String ip, String file, String network, boolean hasRecord) throws UnknownHostException {
            this.ip = InetAddress.getByName(ip);
            db = getFile(file);
            this.network = network;
            this.hasRecord = hasRecord;
        }
    }

    @Test
    public void testGetRecord() throws IOException {
        GetRecordTest[] tests = {
                new GetRecordTest("1.1.1.1", "MaxMind-DB-test-ipv6-32.mmdb", "1.0.0.0/8", false),
                new GetRecordTest("::1:ffff:ffff", "MaxMind-DB-test-ipv6-24.mmdb", "0:0:0:0:0:1:ffff:ffff/128", true),
                new GetRecordTest("::2:0:1", "MaxMind-DB-test-ipv6-24.mmdb", "0:0:0:0:0:2:0:0/122", true),
                new GetRecordTest("1.1.1.1", "MaxMind-DB-test-ipv4-24.mmdb", "1.1.1.1/32", true),
                new GetRecordTest("1.1.1.3", "MaxMind-DB-test-ipv4-24.mmdb", "1.1.1.2/31", true),
                new GetRecordTest("1.1.1.3", "MaxMind-DB-test-decoder.mmdb", "1.1.1.0/24", true),
                new GetRecordTest("::ffff:1.1.1.128", "MaxMind-DB-test-decoder.mmdb", "1.1.1.0/24", true),
                new GetRecordTest("::1.1.1.128", "MaxMind-DB-test-decoder.mmdb", "0:0:0:0:0:0:101:100/120", true),
                new GetRecordTest("200.0.2.1", "MaxMind-DB-no-ipv4-search-tree.mmdb", "0.0.0.0/0", true),
                new GetRecordTest("::200.0.2.1", "MaxMind-DB-no-ipv4-search-tree.mmdb", "0:0:0:0:0:0:0:0/64", true),
                new GetRecordTest("0:0:0:0:ffff:ffff:ffff:ffff", "MaxMind-DB-no-ipv4-search-tree.mmdb", "0:0:0:0:0:0:0:0/64", true),
                new GetRecordTest("ef00::", "MaxMind-DB-no-ipv4-search-tree.mmdb", "8000:0:0:0:0:0:0:0/1", false)
        };
        for (GetRecordTest test : tests) {
            try (Reader reader = new Reader(test.db)) {
                Record record = reader.getRecord(test.ip);

                assertEquals(test.network, record.getNetwork().toString());

                if (test.hasRecord) {
                    assertNotNull(record.getData());
                } else {
                    assertNull(record.getData());
                }
            }
        }
    }


    @Test
    public void testNoIpV4SearchTreeFile() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-no-ipv4-search-tree.mmdb"));
        this.testNoIpV4SearchTree(this.testReader);
    }

    @Test
    public void testNoIpV4SearchTreeStream() throws IOException {
        this.testReader = new Reader(getStream("MaxMind-DB-no-ipv4-search-tree.mmdb"));
        this.testNoIpV4SearchTree(this.testReader);
    }

    private void testNoIpV4SearchTree(Reader reader) throws IOException {

        assertEquals("::0/64", reader.get(InetAddress.getByName("1.1.1.1"))
                .getAsString());
        assertEquals("::0/64", reader.get(InetAddress.getByName("192.1.1.1"))
                .getAsString());
    }

    @Test
    public void testDecodingTypesFile() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-test-decoder.mmdb"));
        this.testDecodingTypes(this.testReader);
    }

    @Test
    public void testDecodingTypesStream() throws IOException {
        this.testReader = new Reader(getStream("MaxMind-DB-test-decoder.mmdb"));
        this.testDecodingTypes(this.testReader);
    }

    private void testDecodingTypes(Reader reader) throws IOException {
        JsonObject record = (JsonObject) reader.get(InetAddress.getByName("::1.1.1.0"));

        assertTrue(record.get("boolean").getAsBoolean());

        JsonArray bytesArray = record.get("bytes").getAsJsonArray();
        assertArrayEquals(new byte[]{0, 0, 0, (byte) 42},  toByteArray(bytesArray));

        assertEquals("unicode! ☯ - ♫", record.get("utf8_string").getAsString());

        assertTrue(record.get("array").isJsonArray());
        JsonArray array = (JsonArray) record.get("array");
        assertEquals(3, array.size());
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals(2, array.get(1).getAsInt());
        assertEquals(3, array.get(2).getAsInt());

        assertTrue(record.get("map").isJsonObject());
        assertEquals(1, ((JsonObject) record.get("map")).size());

        JsonObject mapX = (JsonObject) ((JsonObject) record.get("map")).get("mapX");
        assertEquals(2, mapX.size());

        JsonArray arrayX = (JsonArray) mapX.get("arrayX");
        assertEquals(3, arrayX.size());
        assertEquals(7, arrayX.get(0).getAsInt());
        assertEquals(8, arrayX.get(1).getAsInt());
        assertEquals(9, arrayX.get(2).getAsInt());

        assertEquals("hello", mapX.get("utf8_stringX").getAsString());

        assertEquals(42.123456, record.get("double").getAsDouble(), 0.000000001);
        assertEquals(1.1, record.get("float").getAsFloat(), 0.000001);
        assertEquals(-268435456, record.get("int32").getAsInt());
        assertEquals(100, record.get("uint16").getAsInt());
        assertEquals(268435456, record.get("uint32").getAsInt());
        assertEquals(new BigInteger("1152921504606846976"), record
                .get("uint64").getAsBigInteger());
        assertEquals(new BigInteger("1329227995784915872903807060280344576"),
                record.get("uint128").getAsBigInteger());
    }

    public static byte[] toByteArray(JsonArray bytesArray) {
        byte[] bytes = new byte[bytesArray.size()];
        for (int i = 0; i < bytesArray.size(); i++) {
            bytes[i] = bytesArray.get(i).getAsByte();
        }
        return bytes;
    }

    @Test
    public void testZerosFile() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-test-decoder.mmdb"));
        this.testZeros(this.testReader);
    }

    @Test
    public void testZerosStream() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-test-decoder.mmdb"));
        this.testZeros(this.testReader);
    }

    private void testZeros(Reader reader) throws IOException {
        JsonObject record = (JsonObject) reader.get(InetAddress.getByName("::"));

        assertFalse(record.get("boolean").getAsBoolean());

        assertArrayEquals(new byte[0], toByteArray((JsonArray) record.get("bytes")));

        assertEquals("", record.get("utf8_string").getAsString());

        assertTrue(record.get("array").isJsonArray());
        assertEquals(0, ((JsonArray) record.get("array")).size());

        assertTrue(record.get("map").isJsonObject());
        assertEquals(0, ((JsonObject) record.get("map")).size());

        assertEquals(0, record.get("double").getAsDouble(), 0.000000001);
        assertEquals(0, record.get("float").getAsFloat(), 0.000001);
        assertEquals(0, record.get("int32").getAsInt());
        assertEquals(0, record.get("uint16").getAsInt());
        assertEquals(0, record.get("uint32").getAsInt());
        assertEquals(BigInteger.ZERO, record.get("uint64").getAsBigInteger());
        assertEquals(BigInteger.ZERO, record.get("uint128").getAsBigInteger());
    }

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBrokenDatabaseFile() throws IOException {
        this.testReader = new Reader(getFile("GeoIP2-City-Test-Broken-Double-Format.mmdb"));
        this.testBrokenDatabase(this.testReader);
    }

    @Test
    public void testBrokenDatabaseStream() throws IOException {
        this.testReader = new Reader(getStream("GeoIP2-City-Test-Broken-Double-Format.mmdb"));
        this.testBrokenDatabase(this.testReader);
    }

    private void testBrokenDatabase(Reader reader) throws IOException {

        this.thrown.expect(InvalidDatabaseException.class);
        this.thrown
                .expectMessage(containsString("The MaxMind DB file's data section contains bad data"));

        reader.get(InetAddress.getByName("2001:220::"));
    }

    @Test
    public void testBrokenSearchTreePointerFile() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-test-broken-pointers-24.mmdb"));
        this.testBrokenSearchTreePointer(this.testReader);
    }

    @Test
    public void testBrokenSearchTreePointerStream() throws IOException {
        this.testReader = new Reader(getStream("MaxMind-DB-test-broken-pointers-24.mmdb"));
        this.testBrokenSearchTreePointer(this.testReader);
    }

    private void testBrokenSearchTreePointer(Reader reader)
            throws IOException {

        this.thrown.expect(InvalidDatabaseException.class);
        this.thrown
                .expectMessage(containsString("The MaxMind DB file's search tree is corrupt"));

        reader.get(InetAddress.getByName("1.1.1.32"));
    }

    @Test
    public void testBrokenDataPointerFile() throws IOException {
        this.testReader = new Reader(getFile("MaxMind-DB-test-broken-pointers-24.mmdb"));
        this.testBrokenDataPointer(this.testReader);
    }

    @Test
    public void testBrokenDataPointerStream() throws IOException {
        this.testReader = new Reader(getStream("MaxMind-DB-test-broken-pointers-24.mmdb"));
        this.testBrokenDataPointer(this.testReader);
    }

    private void testBrokenDataPointer(Reader reader) throws IOException {

        this.thrown.expect(InvalidDatabaseException.class);
        this.thrown
                .expectMessage(containsString("The MaxMind DB file's data section contains bad data"));

        reader.get(InetAddress.getByName("1.1.1.16"));
    }

    @Test
    public void testClosedReaderThrowsException() throws IOException {
        Reader reader = new Reader(getFile("MaxMind-DB-test-decoder.mmdb"));

        this.thrown.expect(ClosedDatabaseException.class);
        this.thrown.expectMessage("The MaxMind DB has been closed.");

        reader.close();
        reader.get(InetAddress.getByName("1.1.1.16"));
    }

    private void testMetadata(Reader reader, int ipVersion, long recordSize) {

        Metadata metadata = reader.getMetadata();

        assertEquals("major version", 2, metadata.getBinaryFormatMajorVersion());
        assertEquals(0, metadata.getBinaryFormatMinorVersion());
        assertEquals(ipVersion, metadata.getIpVersion());
        assertEquals("Test", metadata.getDatabaseType());

        List<String> languages = new ArrayList<>(Arrays.asList("en", "zh"));

        assertEquals(languages, metadata.getLanguages());

        Map<String, String> description = new HashMap<>();
        description.put("en", "Test Database");
        description.put("zh", "Test Database Chinese");

        assertEquals(description, metadata.getDescription());
        assertEquals(recordSize, metadata.getRecordSize());

        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.JANUARY, 1);

        assertTrue(metadata.getBuildDate().compareTo(cal.getTime()) > 0);
    }

    private void testIpV4(Reader reader, File file) throws IOException {

        for (int i = 0; i <= 5; i++) {
            String address = "1.1.1." + (int) Math.pow(2, i);
            JsonObject data = new JsonObject();
            data.addProperty("ip", address);

            assertEquals("found expected data record for " + address + " in "
                    + file, data, reader.get(InetAddress.getByName(address)));
        }

        Map<String, String> pairs = new HashMap<>();
        pairs.put("1.1.1.3", "1.1.1.2");
        pairs.put("1.1.1.5", "1.1.1.4");
        pairs.put("1.1.1.7", "1.1.1.4");
        pairs.put("1.1.1.9", "1.1.1.8");
        pairs.put("1.1.1.15", "1.1.1.8");
        pairs.put("1.1.1.17", "1.1.1.16");
        pairs.put("1.1.1.31", "1.1.1.16");
        for (Entry<String, String> stringStringEntry : pairs.entrySet()) {
            JsonObject data = new JsonObject();
            data.addProperty("ip", stringStringEntry.getValue());

            assertEquals("found expected data record for " + stringStringEntry.getKey() + " in "
                    + file, data, reader.get(InetAddress.getByName(stringStringEntry.getKey())));
        }

        for (String ip : new String[]{"1.1.1.33", "255.254.253.123"}) {
            assertNull(reader.get(InetAddress.getByName(ip)));
        }
    }

    // XXX - logic could be combined with above
    private void testIpV6(Reader reader, File file) throws IOException {
        String[] subnets = new String[]{"::1:ffff:ffff", "::2:0:0",
                "::2:0:40", "::2:0:50", "::2:0:58"};

        for (String address : subnets) {
            JsonObject data = new JsonObject();
            data.addProperty("ip", address);

            assertEquals("found expected data record for " + address + " in "
                    + file, data, reader.get(InetAddress.getByName(address)));
        }

        Map<String, String> pairs = new HashMap<>();
        pairs.put("::2:0:1", "::2:0:0");
        pairs.put("::2:0:33", "::2:0:0");
        pairs.put("::2:0:39", "::2:0:0");
        pairs.put("::2:0:41", "::2:0:40");
        pairs.put("::2:0:49", "::2:0:40");
        pairs.put("::2:0:52", "::2:0:50");
        pairs.put("::2:0:57", "::2:0:50");
        pairs.put("::2:0:59", "::2:0:58");

        for (Entry<String, String> stringStringEntry : pairs.entrySet()) {
            JsonObject data = new JsonObject();
            data.addProperty("ip", stringStringEntry.getValue());

            assertEquals("found expected data record for " + stringStringEntry.getKey() + " in "
                    + file, data, reader.get(InetAddress.getByName(stringStringEntry.getKey())));
        }

        for (String ip : new String[]{"1.1.1.33", "255.254.253.123", "89fa::"}) {
            assertNull(reader.get(InetAddress.getByName(ip)));
        }
    }

    static File getFile(String name) {
        return new File(ReaderTest.class.getResource("/maxmind-db/test-data/" + name).getFile());
    }

    static InputStream getStream(String name) {
        return ReaderTest.class.getResourceAsStream("/maxmind-db/test-data/" + name);
    }

}
