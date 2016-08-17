package blue.lapis.nocturne.mapping.io;

public enum MappingFormatType {
    SRG("srg"),
    ENIGMA("*");

    private final String extension;

    MappingFormatType(String extension) {
        this.extension = extension;
    }

    public String getFileExtension() {
        return this.extension;
    }
}
