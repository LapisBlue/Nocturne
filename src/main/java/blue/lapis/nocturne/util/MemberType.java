package blue.lapis.nocturne.util;

import com.google.common.base.Preconditions;

import java.util.HashMap;

/**
 * Represents a particular type of member.
 */
public enum MemberType {

    CLASS,
    FIELD,
    METHOD;

    private static HashMap<String, MemberType> index;

    private void addToIndex() {
        if (index == null) {
            index = new HashMap<>();
        }
        index.put(name(), this);
    }

    public static MemberType fromString(String name) {
        Preconditions.checkArgument(index.containsKey(name), "Invalid key for MemberType");
        return index.get(name);
    }

}
