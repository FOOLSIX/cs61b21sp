package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Statue implements Serializable {
    /** The branch currently pointed to */
    public String head;
    public Set<String> branches;
    public Set<String> StagingArea;
    public Map<String, Commit> branchNameToCommit;

    public Statue() {
        head = "master";
        branches = new TreeSet<>();
        StagingArea = new HashSet<>();
        branchNameToCommit = new TreeMap<>();
    }

    public Commit getCurrentCommit() {
        return this.branchNameToCommit.get(this.head);
    }
}
