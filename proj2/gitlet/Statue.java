package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Statue implements Serializable {
    /** The branch currently pointed to */
    public String head;
    /** A set contains filenames in staging area. */
    public Set<String> stagingArea;
    /** A set contains names of files to be removed. */
    public Set<String> deletedArea;

    public Map<String, String> branchNameToCommit;

    public Statue() {
        head = "master";
        stagingArea = new HashSet<>();
        deletedArea = new HashSet<>();
        branchNameToCommit = new TreeMap<>();
    }

    public Commit getCurrentCommit() {
        return Utils.readObject(Utils.join(Repository.COMMIT_DIR, branchNameToCommit.get(head)), Commit.class);
    }

    public void updateHead(String commitHash) {
        branchNameToCommit.replace(head, commitHash);
    }
}
