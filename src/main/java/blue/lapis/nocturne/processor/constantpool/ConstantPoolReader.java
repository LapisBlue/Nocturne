package blue.lapis.nocturne.processor.constantpool;

import static blue.lapis.nocturne.util.Constants.CLASS_FORMAT_CONSTANT_POOL_OFFSET;
import static blue.lapis.nocturne.util.helper.ByteHelper.asUshort;

import blue.lapis.nocturne.processor.ClassProcessor;
import blue.lapis.nocturne.processor.constantpool.model.ConstantPool;
import blue.lapis.nocturne.processor.constantpool.model.structure.ConstantStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.DummyStructure;
import blue.lapis.nocturne.processor.constantpool.model.structure.StructureType;

import com.google.common.collect.Sets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses the constant pool of a class given its bytecode.
 */
public class ConstantPoolReader extends ClassProcessor {

    protected ConstantPoolReader(String className, byte[] bytes) {
        super(className, bytes);
    }

    public ConstantPool read() {
        List<ConstantStructure> tempPool = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.get(new byte[CLASS_FORMAT_CONSTANT_POOL_OFFSET]);
        int constPoolCount = asUshort(buffer.getShort()) - 1;
        for (int i = 0; i < constPoolCount; i++) {
            StructureType sType = StructureType.fromTag(buffer.get());
            int length = sType.getLength();
            if (sType == StructureType.UTF_8) {
                length = asUshort(buffer.getShort()) + 2;
                buffer.position(buffer.position() - 2);
            }
            byte[] structBytes = new byte[length + 1];
            structBytes[0] = sType.getTag();
            buffer.get(structBytes, 1, length);
            tempPool.add(ConstantStructure.createConstantStructure(structBytes));

            if (sType == StructureType.DOUBLE || sType == StructureType.LONG) {
                tempPool.add(new DummyStructure());
                i++;
            }
        }
        return new ConstantPool(Sets.newHashSet(tempPool), buffer.position() - CLASS_FORMAT_CONSTANT_POOL_OFFSET);
    }

}
