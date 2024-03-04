package gitlet;
import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author 2580368016
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STATUE_FILE = join(GITLET_DIR, "statue");
    public static final File STAGING_DIR = join(GITLET_DIR, "temp");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static Statue currentStatue = new Statue();

    public static void loadStatue() {
        currentStatue = readObject(STATUE_FILE, Statue.class);
    }

    public static void saveStatue() {
        writeObject(STATUE_FILE, currentStatue);
    }

    public static void initRepository() {
        if (GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        STAGING_DIR.mkdir();

        Commit initCommit = new Commit();
        currentStatue.hashToCommit.put(sha1(initCommit), initCommit);
        currentStatue.branchNameToCommit.put(currentStatue.head, initCommit);

        saveStatue();
    }

    public static void add(String fileName) {
        loadStatue();

        File fileToBeAdded = join(CWD, fileName);
        File stagingFile = join(STAGING_DIR, fileName);

        Commit cur = currentStatue.branchNameToCommit.get(currentStatue.head);

        if (cur.FILENAME_TO_BLOBHASH.containsKey(fileName)) {
            if (stagingFile.exists()) {
                stagingFile.delete();
                currentStatue.StagingArea.remove(fileName);
            }

        } else {
            writeObject(stagingFile, readContents(fileToBeAdded));
            currentStatue.StagingArea.add(fileName);
        }

        saveStatue();
    }

    public static void commit(String message) {
        loadStatue();

        Commit cur = currentStatue.getCurrentCommit();
        Commit newCommit = new Commit(message, new Date(), new ArrayList<>(Collections.singletonList(cur)));
        for (String filename : currentStatue.StagingArea) {
            Blob blob = new Blob(join(Repository.CWD, filename));
            newCommit.FILENAME_TO_BLOBHASH.put(filename, blob.sha1HashCode);

        }


    }

    /* TODO: fill in the rest of this class. */
}
