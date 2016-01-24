package blue.lapis.nocturne.jar.model.hierarchy;

import java.util.Objects;

/**
 * Created by Max on 1/24/2016.
 */
public class NodeKey {

    private final String name;
    private final boolean terminal;

    public NodeKey(String name, boolean terminal) {
        this.name = name;
        this.terminal = terminal;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodeKey)) {
            return false;
        }
        NodeKey nk = (NodeKey) obj;
        return nk.name.equals(name) && nk.terminal == terminal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, terminal);
    }

}
