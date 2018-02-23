package com.maxmind.db;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.maxmind.db.cache.NodeCache;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/*
 * Decoder for MaxMind DB data.
 *
 * This class CANNOT be shared between threads
 */
final class Decoder {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final Gson objectmapper = new Gson();

    private static final int[] POINTER_VALUE_OFFSETS = { 0, 0, 1 << 11, (1 << 19) + ((1) << 11), 0 };

    // XXX - This is only for unit testings. We should possibly make a
    // constructor to set this
    boolean POINTER_TEST_HACK;

    private final NodeCache cache;

    private final long pointerBase;

    private final CharsetDecoder utfDecoder = UTF_8.newDecoder();

    private final ByteBuffer buffer;

    static enum Type {
        EXTENDED, POINTER, UTF8_STRING, DOUBLE, BYTES, UINT16, UINT32, MAP, INT32, UINT64, UINT128, ARRAY, CONTAINER, END_MARKER, BOOLEAN, FLOAT;

        // Java clones the array when you call values(). Caching it increased
        // the speed by about 5000 requests per second on my machine.
        static final Type[] values = Type.values();

        public static Type get(int i) {
            return Type.values[i];
        }

        private static Type get(byte b) {
            // bytes are signed, but we want to treat them as unsigned here
            return Type.get(b & 0xFF);
        }

        public static Type fromControlByte(int b) {
            // The type is encoded in the first 3 bits of the byte.
            return Type.get((byte) ((0xFF & b) >>> 5));
        }
    }

    Decoder(NodeCache cache, ByteBuffer buffer, long pointerBase) {
        this.cache = cache;
        this.pointerBase = pointerBase;
        this.buffer = buffer;
    }

    private final NodeCache.Loader cacheLoader = new NodeCache.Loader() {
        @Override
        public JsonElement load(int key) throws IOException {
            return decode(key);
        }
    };

    JsonElement decode(int offset) throws IOException {
        if (offset >= this.buffer.capacity()) {
            throw new InvalidDatabaseException(
                    "The MaxMind DB file's data section contains bad data: "
                            + "pointer larger than the database.");
        }

        this.buffer.position(offset);
        return decode();
    }

    JsonElement decode() throws IOException {
        int ctrlByte = 0xFF & this.buffer.get();

        Type type = Type.fromControlByte(ctrlByte);

        // Pointers are a special case, we don't read the next 'size' bytes, we
        // use the size to determine the length of the pointer and then follow
        // it.
        if (type == Type.POINTER) {
            int pointerSize = ((ctrlByte >>> 3) & 0x3) + 1;
            int base = pointerSize == 4 ? (byte) 0 : (byte) (ctrlByte & 0x7);
            int packed = this.decodeInteger(base, pointerSize);
            long pointer = packed + this.pointerBase + POINTER_VALUE_OFFSETS[pointerSize];

            // for unit testing
            if (this.POINTER_TEST_HACK) {
                return new JsonPrimitive(pointer);
            }

            int targetOffset = (int) pointer;
            int position = buffer.position();
            JsonElement node = cache.get(targetOffset, cacheLoader);
            buffer.position(position);
            return node;
        }

        if (type == Type.EXTENDED) {
            int nextByte = this.buffer.get();

            int typeNum = nextByte + 7;

            if (typeNum < 8) {
                throw new InvalidDatabaseException(
                        "Something went horribly wrong in the decoder. An extended type "
                                + "resolved to a type number < 8 (" + typeNum
                                + ')');
            }

            type = Type.get(typeNum);
        }

        int size = ctrlByte & 0x1f;
        if (size >= 29) {
            int bytesToRead = size - 28;
            int i = this.decodeInteger(bytesToRead);
            switch (size) {
            case 29:
                size = 29 + i;
                break;
            case 30:
                size = 285 + i;
                break;
            default:
                size = 65821 + (i & (0x0FFFFFFF >>> 32 - 8 * bytesToRead));
            }
        }

        return this.decodeByType(type, size);
    }

    private JsonElement decodeByType(Type type, int size)
            throws IOException {
        switch (type) {
            case MAP:
                return this.decodeMap(size);
            case ARRAY:
                return this.decodeArray(size);
            case BOOLEAN:
                return decodeBoolean(size);
            case UTF8_STRING:
                return new JsonPrimitive(this.decodeString(size));
            case DOUBLE:
                return this.decodeDouble(size);
            case FLOAT:
                return this.decodeFloat(size);
            case BYTES:
                return objectmapper.toJsonTree(getByteArray(size));
            case UINT16:
                return this.decodeUint16(size);
            case UINT32:
                return this.decodeUint32(size);
            case INT32:
                return this.decodeInt32(size);
            case UINT64:
                return this.decodeBigInteger(size);
            case UINT128:
                return this.decodeBigInteger(size);
            default:
                throw new InvalidDatabaseException(
                        "Unknown or unexpected type: " + type.name());
        }
    }

    private String decodeString(int size) throws CharacterCodingException {
        int oldLimit = buffer.limit();
        buffer.limit(buffer.position() + size);
        String s = utfDecoder.decode(buffer).toString();
        buffer.limit(oldLimit);
        return s;
    }

    private JsonPrimitive decodeUint16(int size) {
        return new JsonPrimitive(this.decodeInteger(size));
    }

    private JsonPrimitive decodeInt32(int size) {
        return new JsonPrimitive(this.decodeInteger(size));
    }

    private long decodeLong(int size) {
        long integer = 0;
        for (int i = 0; i < size; i++) {
            integer = (integer << 8) | (this.buffer.get() & 0xFF);
        }
        return integer;
    }

    private JsonPrimitive decodeUint32(int size) {
        return new JsonPrimitive(this.decodeLong(size));
    }

    private int decodeInteger(int size) {
        return this.decodeInteger(0, size);
    }

    private int decodeInteger(int base, int size) {
        return Decoder.decodeInteger(this.buffer, base, size);
    }

    static int decodeInteger(ByteBuffer buffer, int base, int size) {
        int integer = base;
        for (int i = 0; i < size; i++) {
            integer = (integer << 8) | (buffer.get() & 0xFF);
        }
        return integer;
    }

    private JsonPrimitive decodeBigInteger(int size) {
        byte[] bytes = this.getByteArray(size);
        return new JsonPrimitive(new BigInteger(1, bytes));
    }

    private JsonPrimitive decodeDouble(int size) throws InvalidDatabaseException {
        if (size != 8) {
            throw new InvalidDatabaseException(
                    "The MaxMind DB file's data section contains bad data: "
                            + "invalid size of double.");
        }
        return new JsonPrimitive(this.buffer.getDouble());
    }

    private JsonPrimitive decodeFloat(int size) throws InvalidDatabaseException {
        if (size != 4) {
            throw new InvalidDatabaseException(
                    "The MaxMind DB file's data section contains bad data: "
                            + "invalid size of float.");
        }
        return new JsonPrimitive(this.buffer.getFloat());
    }

    private static JsonPrimitive decodeBoolean(int size)
            throws InvalidDatabaseException {
        switch (size) {
            case 0:
                return new JsonPrimitive(false);
            case 1:
                return new JsonPrimitive(true);
            default:
                throw new InvalidDatabaseException(
                        "The MaxMind DB file's data section contains bad data: "
                                + "invalid size of boolean.");
        }
    }

    private JsonArray decodeArray(int size) throws IOException {
        JsonArray array = new JsonArray();
        for (int i = 0; i < size; i++) {
            JsonElement r = this.decode();
            array.add(r);
        }

        return array;
    }

    private JsonObject decodeMap(int size) throws IOException {
        JsonObject object = new JsonObject();
        for (int i = 0; i < size; i++) {
            String key = this.decode().getAsString();
            JsonElement value = this.decode();
            object.add(key, value);
        }

        return object;
    }

    private byte[] getByteArray(int length) {
        return getByteArray(this.buffer, length);
    }

    private static byte[] getByteArray(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }
}
