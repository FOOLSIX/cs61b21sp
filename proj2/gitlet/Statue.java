package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Statue implements Serializable {
    /** The branch currently pointed to */
    public String head;
    public Set<String> StagingArea;
    public Map<String, String> branchNameToCommit;

    public Statue() {
        head = "master";
        StagingArea = new HashSet<>();
        branchNameToCommit = new TreeMap<>();
    }

    public Commit getCurrentCommit() {
        return Utils.readObject(Utils.join(Repository.OBJECT_DIR, branchNameToCommit.get(head)), Commit.class);
    }

    public void updateHead(String commitHash) {
        branchNameToCommit.replace(head, commitHash);
    }
}
