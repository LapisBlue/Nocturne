package blue.lapis.nocturne.transform.constpool.structure;

import blue.lapis.nocturne.util.Constants;

import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Represents a Utf8 structure.
 */
public class Utf8Structure extends ConstantStructure {

    private String str;

    public Utf8Structure(byte[] bytes) {
        super(bytes);
        assert bytes.length >= 3;
        int length = ShortBuffer.allocate(Short.BYTES).put(bytes[1], bytes[2]).get() & Constants.SHORT_UNSIGNER;
        assert bytes.length == length + 3;
        byte[] strBytes = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, strBytes, 0, strBytes.length);
        str = new String(strBytes, StandardCharsets.UTF_8);
    }

    public String asString() {
        return str;
    }

}
