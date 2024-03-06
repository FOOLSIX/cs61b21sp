package gitlet;
import java.io.Serializable;
import java.util.*;

public class Status implements Serializable {
    /** The branch currently pointed to */
    String head;
    /** A set contains filenames in staging area. */
    Set<String> stagingArea;
    /** A set contains names of files to be removed. */
    Set<String> deletedArea;

    Map<String, String> branchNameToCommit;

    public Status() {
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
