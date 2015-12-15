package blue.lapis.nocturne.util.helper;

import blue.lapis.nocturne.util.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Static utility class for byte manipulation.
 */
public final class ByteHelper {

    public static int asUshort(byte b1, byte b2) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(b1).put(b2);
        //noinspection PointlessBitwiseExpression (IDEA is wrong - omitting the bitmask produces a difference result)
        return (int) buffer.getShort(0) & Constants.SHORT_UNSIGNER;
    }

}
