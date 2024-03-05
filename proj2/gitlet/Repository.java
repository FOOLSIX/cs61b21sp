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
    public static final File STAGED_DIR = join(GITLET_DIR, "staged");
    public static final File REMOVED_DIR = join(GITLET_DIR, "removed");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File COMMIT_DIR = join(OBJECT_DIR, "commits");
    public static final File BLOB_DIR = join(OBJECT_DIR, "blobs");
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
        STAGED_DIR.mkdir();
        REMOVED_DIR.mkdir();
        OBJECT_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();

        Commit initCommit = new Commit();
        initCommit.save();
        currentStatue.branchNameToCommit.put(currentStatue.head, initCommit.SHA1_HASHCODE);

        saveStatue();
    }

    public static void add(String fileName) {
        loadStatue();

        File fileToBeAdded = join(CWD, fileName);
        File stagingFile = join(STAGED_DIR, fileName);
        Blob blob = new Blob(fileToBeAdded);
        Commit cur = currentStatue.getCurrentCommit();

        if (cur.FILENAME_TO_BLOBHASH.containsKey(fileName) && Objects.equals(cur.FILENAME_TO_BLOBHASH.get(fileName), blob.SHA1_HASHCODE)) {

            if (stagingFile.exists()) {
                stagingFile.delete();
                currentStatue.stagingArea.remove(fileName);
            }

        } else {
            writeContents(stagingFile, readContents(fileToBeAdded));
            currentStatue.stagingArea.add(fileName);
        }
        currentStatue.deletedArea.remove(fileName);
        saveStatue();
    }

    public static void commit(String message) {
        loadStatue();
        Commit lastCommit = currentStatue.getCurrentCommit();
        if (currentStatue.stagingArea.size() == 0 && currentStatue.deletedArea.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message, new ArrayList<>(Collections.singletonList(lastCommit.SHA1_HASHCODE)));
        currentStatue.updateHead(newCommit.SHA1_HASHCODE);


        for (String removedFile : currentStatue.deletedArea) {
            if (!join(STAGED_DIR, removedFile).exists()) {
                newCommit.FILENAME_TO_BLOBHASH.remove(removedFile);
            }
        }
        currentStatue.deletedArea.clear();

        List<String> stagedDir = Utils.plainFilenamesIn(STAGED_DIR);
        if (stagedDir != null) {
            for (String filename : stagedDir) {
                File blobFile = join(Repository.STAGED_DIR, filename);
                Blob blob = new Blob(blobFile);
                newCommit.FILENAME_TO_BLOBHASH.put(filename, blob.SHA1_HASHCODE);
                blob.save();
                join(Repository.STAGED_DIR, filename).delete();
            }
        }
        currentStatue.stagingArea.clear();

        newCommit.save();
        saveStatue();
    }

    public static void rm(String filename) {
        loadStatue();
        File fileToBeRemoved = join(CWD, filename);
        File stagedFile = join(STAGED_DIR, filename);
        Commit cur = currentStatue.getCurrentCommit();
        if (stagedFile.exists()) {
            stagedFile.delete();
            currentStatue.stagingArea.remove(filename);
        } else if (cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
            currentStatue.deletedArea.add(filename);
            writeContents(stagedFile, readContents(fileToBeRemoved));
            join(CWD, filename).delete();
        } else {
            System.out.println("No reason to remove the file.\n");
        }
        saveStatue();
    }

    public static void log() {
        loadStatue();
        Commit curCommit = currentStatue.getCurrentCommit();
        while (true) {
            curCommit.printCommit();
            if (curCommit.FATHER == null)
                break;
            curCommit = Commit.getCommit(curCommit.FATHER.get(0));
        }
    }

    public static void global_log() {
        loadStatue();
        List<String> filenames= plainFilenamesIn(COMMIT_DIR);
        if (filenames == null)
            return;
        for (String filename : filenames) {
            Commit commit = readObject(join(COMMIT_DIR, filename), Commit.class);
            commit.printCommit();
        }
    }

    public static void find(String msg) {
        loadStatue();
        List<String> filenames = plainFilenamesIn(COMMIT_DIR);
        if (filenames == null) {
            System.out.println("Found no commit with that message.");
            return;
        }
        boolean find = false;
        for (String filename : filenames) {
            Commit commit = readObject(join(COMMIT_DIR, filename), Commit.class);
            if (Objects.equals(commit.MESSAGE, msg)) {
                find = true;
                System.out.println(commit.SHA1_HASHCODE);
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        loadStatue();

        System.out.println("=== Branches ===");
        for (String branch: currentStatue.branchNameToCommit.keySet()) {
            if (branch == currentStatue.head)
                System.out.print('*');
            System.out.println(branch);
        }
        System.out.print("\n");

        System.out.println("=== Staged Files ===");
        for (String stagedFile: currentStatue.stagingArea)
            System.out.println(stagedFile);
        System.out.print("\n");

        System.out.println("=== Removed Files ===");
        for (String removedFile: currentStatue.deletedArea)
            System.out.println(removedFile);
        System.out.print("\n");
        //TODO: extra part
        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===\n");
    }

    public static void checkout1(String filename) {
        loadStatue();
        checkout2(currentStatue.getCurrentCommit().SHA1_HASHCODE, filename);

    }

    public static void  checkout2(String commitID, String filename) {//TODO: short id
        File file = join(COMMIT_DIR, commitID);
        if (!file.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit cur = readObject(file, Commit.class);

        if (!cur.FILENAME_TO_BLOBHASH.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            Blob blob = readObject(join(BLOB_DIR, cur.FILENAME_TO_BLOBHASH.get(filename)), Blob.class);
            writeContents(join(CWD, blob.FILE_NAME), blob.CONTENT);
        }
    }

    public static void checkout3(String branchName) {
        loadStatue();

        if (!currentStatue.branchNameToCommit.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (Objects.equals(branchName, currentStatue.head)){
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit cur = currentStatue.getCurrentCommit();
            Commit branch = readObject(join(COMMIT_DIR, currentStatue.branchNameToCommit.get(branchName)), Commit.class);

            List<String> cwdFiles = plainFilenamesIn(CWD);
            if (cwdFiles != null) {
                for (String file : cwdFiles) {
                    if (branch.FILENAME_TO_BLOBHASH.containsKey(file) && !cur.FILENAME_TO_BLOBHASH.containsKey(file)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
            }

            for (var e : branch.FILENAME_TO_BLOBHASH.entrySet()) {
                Blob blob = readObject(join(BLOB_DIR, e.getValue()), Blob.class);
                writeContents(join(CWD, e.getKey()), blob.CONTENT);
            }

            for (var e : cur.FILENAME_TO_BLOBHASH.entrySet()) {
                File file = join(CWD, e.getKey());
                if (file.exists() && !branch.FILENAME_TO_BLOBHASH.containsKey(e.getKey())) {
                    file.delete();
                }
            }

            currentStatue.head = branchName;
            currentStatue.branchNameToCommit.put(branchName, branch.SHA1_HASHCODE);
            saveStatue();
        }
    }

    public static void branch(String branchName) {
        loadStatue();
        if (currentStatue.branchNameToCommit.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        currentStatue.branchNameToCommit.put(branchName, currentStatue.getCurrentCommit().SHA1_HASHCODE);
    }

    /* TODO: fill in the rest of this class. */
}
