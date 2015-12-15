package blue.lapis.nocturne.transform.constpool.structure;

/**
 * Dummy structure used to fill gaps caused by double-width structures.
 */
public class DummyStructure extends ConstantStructure {

    public DummyStructure() {
        super(new byte[] {(byte) 0xFF});
    }

}
