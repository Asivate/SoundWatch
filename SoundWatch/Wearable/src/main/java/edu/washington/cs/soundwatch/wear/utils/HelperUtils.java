package edu.washington.cs.soundwatch.wear.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;

public class HelperUtils {
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * Code to extract audio features
     */
    public static double db(short[] data) {
        double sum = 0.0;
        if (data.length == 0) {
            return sum;
        }
        for (short val : data) {
            sum += val * val;
        }
        double rms = Math.sqrt(sum / data.length);
        double db = 0;
        if (rms > 0) {
            db = 20 * Math.log10(rms);
        }
        return db;
    }

    public static double db(List<Short> soundBuffer) {
        double sum = 0.0;
        if (soundBuffer.size() == 0) {
            return sum;
        }
        for (short val : soundBuffer) {
            sum += val * val;
        }
        double rms = Math.sqrt(sum / soundBuffer.size());
        double db = 0;
        if (rms > 0) {
            db = 20 * Math.log10(rms);
        }
        return db;
    }

    /**
     *
     * @param bytes
     * @return
     */
    public static short[] convertByteArrayToShortArray(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static String convertSetToCommaSeparatedList(Set<String> connectedHostIds) {
        StringBuilder result = new StringBuilder();
        for (String connectedHostId: connectedHostIds) {
            result.append(connectedHostId);
            result.append(",");
        }
        if (connectedHostIds.isEmpty()) {
            return "";
        }
        return result.substring(0, result.length() - 1);
    }
}
